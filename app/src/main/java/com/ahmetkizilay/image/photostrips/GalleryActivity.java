package com.ahmetkizilay.image.photostrips;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.ahmetkizilay.image.photostrips.compat.ActionBarHelper;
import com.ahmetkizilay.image.photostrips.dialogs.AboutMeDialogFragment;
import com.ahmetkizilay.image.photostrips.utils.CustomHorizontalScrollView;
import com.ahmetkizilay.image.photostrips.utils.CustomHorizontalScrollView.OnScrollStoppedListener;
import com.ahmetkizilay.image.photostrips.utils.CustomHorizontalScrollView.ScrollStoppedEvent;

public class GalleryActivity extends FragmentActivity {
	// using this for faking an action bar for earlier versions of android.
	final ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);

	private int disp_height;
	private int disp_width;
	
	private String home_directory_string = "";
	private int numberOfPhotosDisplayed = 0;
	private int totalNumberOfPhotos;

	
	private LinearLayout myGallery;
	private LinearLayout loWait;
	private CustomHorizontalScrollView hsw;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBarHelper.onCreate(savedInstanceState);

		setContentView(R.layout.gallery);

		DisplayMetrics dispMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);

		disp_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dispMetrics.heightPixels, dispMetrics);
		disp_width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dispMetrics.widthPixels, dispMetrics);

		hsw = (CustomHorizontalScrollView) findViewById(R.id.hswGallery);

		hsw.setOnScrollStoppedListener(new OnScrollStoppedListener() {

			public void onScrollStopped(ScrollStoppedEvent evt) {

				if (evt.getAction() == ScrollStoppedEvent.SCROLL_END) {
					if (numberOfPhotosDisplayed < totalNumberOfPhotos) {
						showWaitPanel();
						Thread t = new Thread(new InsertPhotoRunnable(myGallery, numberOfPhotosDisplayed, 6));
						t.start();
					}
				}
			}
		});

		this.myGallery = (LinearLayout) findViewById(R.id.mygallery);

		this.loWait = (LinearLayout) findViewById(R.id.loGalleryWaitScreen);

		this.home_directory_string = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "perisonic" + File.separator + "image" + File.separator + "photostrips";

		final File[] photos = getFiles();

		if (photos == null) {
			return;
		}

		this.totalNumberOfPhotos = photos.length;
		this.numberOfPhotosDisplayed = 0;

		final int initPhotoCount = Math.min((int) Math.ceil((float) disp_width / 150) * 2, totalNumberOfPhotos);
		Thread t = new Thread(new InsertPhotoRunnable(myGallery, 0, initPhotoCount));
		t.start();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			showAboutMe();
			return true;
		case R.id.menu_booth:
			Intent swithViewIntent = new Intent(this, PhotoBoothActivity.class);
			swithViewIntent.setAction("com.ahmetkizilay.image.photostrips.PhotoBoothActivity");
			this.startActivity(swithViewIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showAboutMe() {
		DialogFragment newFragment = AboutMeDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	private void showWaitPanel() {
		runOnUiThread(new Runnable() {
			public void run() {
				loWait.setVisibility(View.VISIBLE);
			}
		});
	}

	private void hideWaitPanel() {
		try {
			Thread.sleep(500);
		} catch (Exception exp) {
		}
		runOnUiThread(new Runnable() {
			public void run() {
				loWait.setVisibility(View.GONE);
				hsw.smoothScrollBy(100, 0);
			}
		});

	}
	
	/***
	 * Creates a layout in which the photo is displayed. the layout is then passed into the parent layout.
	 * @param photo file to be added to the gallery
	 * @return the view created to be added into the gallery
	 */
	private View insertPhoto(final File photo) {

		String thumbLocation = PhotoCreator.getThumbnailLocation(photo);
		File thumbFile = new File(thumbLocation);
		if (!thumbFile.exists()) {
			if (PhotoCreator.createThumbnail(photo, disp_height + 50)) {
				thumbFile = new File(thumbLocation);
			} else {
				// TODO take care of this
			}
		}

		Bitmap thumbBM = BitmapFactory.decodeFile(thumbLocation);

		ScrollView layout = new ScrollView(getApplicationContext());

		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout lo = (LinearLayout) inflater.inflate(R.layout.gallery_item, null);
		ImageView imageView = (ImageView) lo.getChildAt(0);
		imageView.setImageBitmap(thumbBM);

		imageView.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				String selectedFile = photo.getAbsolutePath();
				Intent showPicIntent = new Intent(Intent.ACTION_VIEW);
				showPicIntent.setDataAndType(Uri.parse("file://" + selectedFile), "image/*");
				startActivity(showPicIntent);
			}
		});

		layout.addView(lo);
		return layout;
	}

	/***
	 * retrives the image files from directory, orders them by last modified date
	 * @return list of images in the gallery folder
	 */
	private File[] getFiles() {
		File[] photos = new File(home_directory_string).listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().startsWith("IMG_");
			}
		});

		if (photos == null || photos.length < 1) {
			return null;
		}

		Arrays.sort(photos, new Comparator<File>() {
			public int compare(File o1, File o2) {

				if (((File) o1).lastModified() > ((File) o2).lastModified()) {
					return -1;
				} else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
					return 1;
				} else {
					return 0;
				}
			}

		});

		return photos;
	}
	
	/***
	 * Adds images to the gallery in a separate thread for a smoother, more user friendly user experience 
	 * @author ahmetkizilay
	 *
	 */
	private class InsertPhotoRunnable implements Runnable {

		private LinearLayout view;
		private int start;
		private int length;

		public InsertPhotoRunnable(LinearLayout view, int start, int length) {
			this.view = view;
			this.start = start;
			this.length = length;
		}

		public void run() {

			final File[] photos = getFiles();

			for (int i = 0; i < length; i++) {
				try {
					Thread.sleep(10);
				} catch (Exception exp) {
				}
				if (numberOfPhotosDisplayed == photos.length) {
					hideWaitPanel();
					return;
				} else {
					numberOfPhotosDisplayed++;
				}

				final int index = i + start;
				runOnUiThread(new Runnable() {
					public void run() {
						view.addView(insertPhoto(photos[index]));
					}
				});
			}

			hideWaitPanel();

		}
	}
	
	/* ********* BEGIN METHODS RELATED TO THE ACTION BAR ********************** */
	protected ActionBarHelper getActionBarHelper() {
		return mActionBarHelper;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mActionBarHelper.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onTitleChanged(CharSequence title, int color) {
		mActionBarHelper.onTitleChanged(title, color);
		super.onTitleChanged(title, color);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		boolean retValue = false;
		retValue |= mActionBarHelper.onCreateOptionsMenu(menu);
		retValue |= super.onCreateOptionsMenu(menu);
		return retValue;
	}
	
	@Override
	public MenuInflater getMenuInflater() {
		return mActionBarHelper.getMenuInflater(super.getMenuInflater());
	}
	/* ********* END METHODS RELATED TO THE ACTION BAR ********************** */

}
