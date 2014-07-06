package com.ahmetkizilay.image.photostrips;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmetkizilay.image.photostrips.compat.ActionBarHelper;
import com.ahmetkizilay.image.photostrips.dialogs.AboutMeDialogFragment;
import com.ahmetkizilay.image.photostrips.dialogs.CompletionDialogFragment;
import com.ahmetkizilay.image.photostrips.dialogs.PhotoCreationDialogFragment;
import com.ahmetkizilay.image.photostrips.utils.OnDoubleTapListener;

public class PhotoBoothActivity extends FragmentActivity {

	// using this for faking an action bar for earlier versions of android.
	final ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);

	private int disp_height, disp_width;

	private boolean isCapturing = false;
	private boolean isSoundOn = true;
	private boolean isPortaitView = true;

	private int photoCount = 0;
	List<String> photoParts = new ArrayList<String>();
	private String finalImagePath = null;
	private String home_directory_string = "";
	
	private int camId = 0;
	private boolean isCameraFrontFacing = true;
	Camera mCamera;
	CameraPreview mPreview;
	Button captureButton;

	TextView[] twPhotoCount;

	private ImageButton btnSound;
	private MediaPlayer mpOne;
	
	private ImageButton btnRotatePortrait;
	private ImageButton btnRotateLandscape;

	private DialogFragment photoCreationFragment;


	/***
	 *  callback function that runs after a photo is taken.
	 *  saves the captured data as a tmp image file.
	 */
	private PictureCallback pictureTakenCallback = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {
			File pictureFile = getFileForImage(true);
			if (pictureFile == null) {
				Log.d("", "Error creating file");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (Exception exp) {
				Log.d("", "Error: " + exp.getMessage());
			}

			photoParts.add(pictureFile.getAbsolutePath());
			camera.startPreview();

			takeMyPicture();
		}
	};

	/***
	 * runnable instance that is called after camera is initialized.
	 * camera initialization takes place in a separate thread. so the UI modifications should be run 
	 * with runOnUIThread method.
	 */
	final Runnable postCameraInit = new Runnable() {

		public void run() {
			mPreview = new CameraPreview(PhotoBoothActivity.this, mCamera);
			mPreview.setOnTouchListener(null);
			mPreview.setOnTouchListener(new OnDoubleTapListener() {
				public void onDoubleTap() {

					PhotoBoothActivity.this.runOnUiThread(new Runnable() {

						public void run() {
							toggleActionBar();
							toggleSettingsBar();
						}
					});
				}
			});

			FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
			preview.addView(mPreview, 0);
		}
	};
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBarHelper.onCreate(savedInstanceState);

		setContentView(R.layout.booth);

		try {
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				Log.d("", "External SD Card Is Not Detected!");
				throw new Exception("External SD Card Is Not Detected!");
			}

			home_directory_string = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "perisonic"; 
			
			File home_directory = new File(home_directory_string);
			if (!home_directory.exists() && !home_directory.mkdirs()) {
				Log.d("", "home directory could not be created!");
				throw new Exception("External SD Card Is Not Detected!");

			}

			home_directory_string += File.separator + "image";
			home_directory = new File(home_directory_string);
			if (!home_directory.exists() && !home_directory.mkdirs()) {
				Log.d("", "home directory could not be created!");
				throw new Exception("Could Not Create Directory!");
			}

			home_directory_string += File.separator + "photostrips";
			home_directory = new File(home_directory_string);
			if (!home_directory.exists() && !home_directory.mkdirs()) {
				Log.d("", "home directory could not be created!");
				throw new Exception("Could Not Create Directory!");
			}

			String thumb_directory_string = home_directory_string + File.separator + ".thumb";
			File thumb_directory = new File(thumb_directory_string);
			if (!thumb_directory.exists() && !thumb_directory.mkdirs()) {
				Log.d("", "home directory could not be created!");
				throw new Exception("Could Not Create Directory!");
			}

		} catch (Exception exp) {
			Toast.makeText(this, "Error During Initialization", Toast.LENGTH_LONG).show();
		}

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		boolean hasActionBar = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

		DisplayMetrics dispMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);

		disp_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dispMetrics.heightPixels, dispMetrics);
		disp_width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dispMetrics.widthPixels, dispMetrics);

		int actionBarCompensation = hasActionBar ? 0 : (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, dispMetrics);
		int marg8dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, dispMetrics);
		int loHeight = (int) ((float) (disp_height - disp_width - actionBarCompensation) * 0.5);

		// Setting the positions for the views dynamically for better visual design
		TableLayout loTransport = (TableLayout) findViewById(R.id.loTransport);
		FrameLayout.LayoutParams loTransportParams = new FrameLayout.LayoutParams(loTransport.getLayoutParams());
		loTransportParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, dispMetrics);
		loTransportParams.gravity = Gravity.BOTTOM;
		loTransportParams.setMargins(marg8dp, 0, marg8dp, Math.max(marg8dp, (int) ((float) (loHeight - loTransportParams.height) * 0.5)));
		loTransport.setLayoutParams(loTransportParams);

		RelativeLayout loSettings = (RelativeLayout) findViewById(R.id.loSettings);
		FrameLayout.LayoutParams loSettingsParams = new FrameLayout.LayoutParams(loTransport.getLayoutParams());
		loSettingsParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, dispMetrics);
		loSettingsParams.gravity = Gravity.TOP;
		loSettingsParams.setMargins(marg8dp, (int) ((float) (loHeight - actionBarCompensation) * 0.5), marg8dp, 0);
		loSettings.setLayoutParams(loSettingsParams);

		twPhotoCount = new TextView[4];
		twPhotoCount[0] = (TextView) findViewById(R.id.tw1);
		twPhotoCount[1] = (TextView) findViewById(R.id.tw2);
		twPhotoCount[2] = (TextView) findViewById(R.id.tw3);
		twPhotoCount[3] = (TextView) findViewById(R.id.tw4);

		captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (!isCapturing) {
					isCapturing = true;
					photoCount = 0;
					photoParts.clear();
					finalImagePath = null;
					
					captureButton.setBackgroundResource(R.drawable.roundedbutton_capturing);
					
					takeMyPicture();

				} else {
					revertControls(true);
				}
			}
		});

		btnSound = (ImageButton) findViewById(R.id.btnSound);		
		
		btnSound.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (isCapturing) {
					return;
				}

				if (isSoundOn) {
					btnSound.setImageResource(R.drawable.sound_off);
					isSoundOn = false;
									
				} else {
					btnSound.setImageResource(R.drawable.sound_on);
					isSoundOn = true;
				}
			}
		});

		btnRotatePortrait = (ImageButton) findViewById(R.id.btnRotatePortrait);
		btnRotateLandscape = (ImageButton) findViewById(R.id.btnRotateLandscape);

		btnRotatePortrait.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if(isCapturing) return;
				
				isPortaitView = true;
				btnRotatePortrait.setBackgroundResource(R.drawable.selectedbutton);
				btnRotateLandscape.setBackgroundResource(R.drawable.unselectedbutton);
			}
		});

		btnRotateLandscape.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if(isCapturing) return;
				
				isPortaitView = false;
				btnRotateLandscape.setBackgroundResource(R.drawable.selectedbutton);
				btnRotatePortrait.setBackgroundResource(R.drawable.unselectedbutton);
			}
		});

		final ImageButton btnSwitchCam = (ImageButton) findViewById(R.id.btnSwitchCam);
		btnSwitchCam.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (Camera.getNumberOfCameras() > 1) {
					camId = (camId + 1) % Camera.getNumberOfCameras();
					runOnUiThread(new Runnable() {
						public void run() {
							initCamera();
						}
					});
				}
			}

		});

		RelativeLayout relLayout = (RelativeLayout) findViewById(R.id.loSettings);
		relLayout.setVisibility(View.VISIBLE);

		mpOne = MediaPlayer.create(getApplicationContext(), R.raw.one_wav);

		LinearLayout topBlur = (LinearLayout) findViewById(R.id.loTopBlur);
		ViewGroup.LayoutParams loParams = topBlur.getLayoutParams();
		loParams.height = loHeight;
		topBlur.setLayoutParams(loParams);

		LinearLayout bottomBlur = (LinearLayout) findViewById(R.id.loBottomBlur);
		loParams = bottomBlur.getLayoutParams();
		loParams.height = loHeight;
		bottomBlur.setLayoutParams(loParams);

		// Thread.setDefaultUncaughtExceptionHandler(new
		// CustomUncaughtExceptionHandler());

	}
	
	/***
	 * camera is initialized here.
	 */
	@Override	
	protected void onResume() {
		super.onResume();
		
		if (mCamera == null) {
			initCamera();
		}

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean("isPortraitView", isPortaitView);
		outState.putBoolean("isSoundOn", isSoundOn);
		outState.putInt("camId", camId);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {	
		super.onRestoreInstanceState(savedInstanceState);
		
		this.isPortaitView  = savedInstanceState.getBoolean("isPortraitView");
		this.isSoundOn = savedInstanceState.getBoolean("isSoundOn");
		this.camId = savedInstanceState.getInt("camId");
		
		if (isSoundOn) {
			btnSound.setImageResource(R.drawable.sound_on);
		} else {
			btnSound.setImageResource(R.drawable.sound_off);			
		}
		
		if(this.isPortaitView) {
			btnRotatePortrait.setBackgroundResource(R.drawable.selectedbutton);
			btnRotateLandscape.setBackgroundResource(R.drawable.unselectedbutton);
		}
		else {
			btnRotateLandscape.setBackgroundResource(R.drawable.selectedbutton);
			btnRotatePortrait.setBackgroundResource(R.drawable.unselectedbutton);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
		if (mpOne != null) {
			mpOne.release();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
		if (isCapturing) {
			revertControls(true);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			showAboutMe();
			return true;
		case R.id.menu_gallery:
			if(isCapturing) revertControls(true);
			
			Intent switchViewIntent = new Intent(this, GalleryActivity.class);
			switchViewIntent.setAction("com.ahmetkizilay.image.photostrips.GalleryActivity");			
			this.startActivity(switchViewIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
			revertControls(isCapturing);
		}

		return super.onKeyDown(keyCode, event);
	}


	/***
	 * Method for initializing the camera, and the camera preview.
	 * Initialization takes place in a separate thread to prevent UI blocking.
	 */
	private void initCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}

		if (mPreview != null) {
			mPreview.setOnTouchListener(null);
			mPreview.clearAnimation();
			FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
			preview.removeView(mPreview);
			mPreview = null;
		}

		Thread t = new Thread(new Runnable() {

			public void run() {
				if (mCamera == null) {
					mCamera = getCameraInstance();

					int result = getCameraRotation();

					Parameters params = mCamera.getParameters();

					Size previewSize = getMostSuitablePreviewSize(params.getSupportedPreviewSizes());

					if (result == 90 || result == 270) {
						params.setPreviewSize(previewSize.width, previewSize.height);
					} else {
						params.setPreviewSize(previewSize.height, previewSize.width);
					}

					mCamera.setParameters(params);
					mCamera.setDisplayOrientation(result);

					// UI modifications should run on UI thread
					PhotoBoothActivity.this.runOnUiThread(postCameraInit);
				}
			}
			
			/***
			 * Simply opens the camera, and returns the camera instance
			 * @return the currently used camera
			 */
			private Camera getCameraInstance() {
				Camera c = null;
				try {
					c = Camera.open(camId);
				} catch (Exception exp) {
					PhotoBoothActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(PhotoBoothActivity.this, "Could Not Open Camera", Toast.LENGTH_LONG).show();
						}
					});
				}
				return c;
			}

			/***
			 * Calculates the camera rotation for camera preview.
			 * I basically copied this code from the Android docs.
			 * @return rotation angle
			 */
			private int getCameraRotation() {
				android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
				android.hardware.Camera.getCameraInfo(0, info);

				int degrees = 0;
				switch (getWindowManager().getDefaultDisplay().getRotation()) {
				case Surface.ROTATION_0:
					degrees = 0;
					break;
				case Surface.ROTATION_90:
					degrees = 90;
					break;
				case Surface.ROTATION_180:
					degrees = 180;
					break;
				case Surface.ROTATION_270:
					degrees = 270;
					break;
				}

				int result;
				isCameraFrontFacing = info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
				if (isCameraFrontFacing) {
					result = (info.orientation + degrees) % 360;
					result = (360 - result) % 360; // compensate the mirror
				} else { // back-facing
					result = (info.orientation - degrees + 360) % 360;
				}
				return result;
			}
			
			/***
			 * selects the most suitable preview size, based on the available preview sizes and the screen size
			 * @param sizes
			 * @return the most suitable preview size
			 */
			private Size getMostSuitablePreviewSize(List<Size> sizes) {
				float ratio = (float) disp_height / (float) disp_width;

				Size resultSize = sizes.get(0);
				float closestRatio = (float) resultSize.height / (float) resultSize.height;
				for (int i = 1, iLen = sizes.size(); i < iLen; i++) {
					Size thisSize = sizes.get(i);
					float thisRatio = (float) thisSize.height / (float) thisSize.height;
					if (Math.abs(thisRatio - ratio) < closestRatio && thisSize.height > resultSize.height) {
						closestRatio = thisRatio;
						resultSize = thisSize;
					}
				}

				return resultSize;
			}

		});
		
		t.start();
	}
	
	/***
	 * method for taking pictures. starts the process as a separate thread.
	 * takes a picture and the camera callback recursively triggers this method again for the next picture.
	 */
	private void takeMyPicture() {
		if (!isCapturing) {
			return;
		}

		if (++photoCount > 4) {
			showPhotoCreationDialog();
			return;
		}

		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(1000);

					if (!isCapturing)
						return;

					changePhotoCountFontColor(photoCount - 1, Color.RED);

					changeCaptureButtonText("3");
					if (isSoundOn)
						mpOne.start();
					Thread.sleep(1000);

					if (!isCapturing)
						return;
					changeCaptureButtonText("2");

					if (isSoundOn)
						mpOne.start();
					Thread.sleep(1000);

					if (!isCapturing)
						return;
					changeCaptureButtonText("1");
					if (isSoundOn)
						mpOne.start();
					Thread.sleep(1000);

					if (!isCapturing)
						return;

					changeCaptureButtonText(":)");
					if (!isCapturing)
						return;

					mCamera.takePicture(isSoundOn ? new Camera.ShutterCallback() {
						public void onShutter() {
						}
					} : null, null, pictureTakenCallback);
				} catch (Exception exp) {
					exp.printStackTrace();
				}
			}

		});
		t.start();
	}
	
	/***
	 * called after 4 pictures are taken. calls the method to combine 4 pictures.
	 */
	private void prepareFinalImage() {

		Thread t = new Thread(new Runnable() {
			public void run() {				
				String newFilePath = getFileForImage(false).getAbsolutePath();
				boolean isSuccess = PhotoCreator.prepareFinalImage(photoParts, isCameraFrontFacing, isPortaitView, newFilePath);
				if(isSuccess) finalImagePath = newFilePath;
				revertControls(false);
				hidePhotoCreationDialog(isSuccess);
			}
		});
		t.start();

	}
	
	/***
	 * Double-Tapping on the screens toggles the action bar and the settings layout
	 */
	private void toggleSettingsBar() {
		Animation anim = null;
		final RelativeLayout rel = (RelativeLayout) findViewById(R.id.loSettings);
		if (rel.getVisibility() == View.VISIBLE) {
			anim = outToLeftAnimation();
			anim.setAnimationListener(new Animation.AnimationListener() {

				public void onAnimationStart(Animation arg0) {
				}

				public void onAnimationRepeat(Animation arg0) {
				}

				public void onAnimationEnd(Animation arg0) {
					rel.setVisibility(View.INVISIBLE);
				}
			});
		} else {
			anim = inFromRightAnimation();
			anim.setAnimationListener(new Animation.AnimationListener() {

				public void onAnimationStart(Animation arg0) {
					rel.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation arg0) {
				}

				public void onAnimationEnd(Animation arg0) {
				}
			});
		}
		anim.setDuration(400);
		rel.startAnimation(anim);

	}
	
	
	/* ******************BEGIN SORT OF UTILITY FUNCTIONS *************** */
	
	/***
	 * reverts Views to their default states.
	 * @param true if photo capture is cancelled
	 */
	private void revertControls(boolean cancelled) {
		isCapturing = false;

		changePhotoCountFontColor(0, Color.WHITE);
		changePhotoCountFontColor(1, Color.WHITE);
		changePhotoCountFontColor(2, Color.WHITE);
		changePhotoCountFontColor(3, Color.WHITE);

		changeCaptureButtonText("");
		
		runOnUiThread(new Runnable() { public void run() { captureButton.setBackgroundResource(R.drawable.roundedbutton); }});

		deletePartPhotoFiles();

		if (cancelled) {
			Toast.makeText(this, "Photo Cancelled", Toast.LENGTH_SHORT).show();
		}
	}
	
	/***
	 * Deletes temp photo parts that make up the final image
	 */
	private void deletePartPhotoFiles() {
		for (int i = 0; i < photoParts.size(); i++) {
			File thisFile = new File(photoParts.get(i));
			if (thisFile.exists())
				thisFile.delete();
		}
		photoParts.clear();
	}

	private void changeCaptureButtonText(final String val) {
		runOnUiThread(new Runnable() {
			public void run() {
				captureButton.setText(val);
			}
		});
	}

	private void changePhotoCountFontColor(final int index, final int color) {

		runOnUiThread(new Runnable() {
			public void run() {
				twPhotoCount[index].setTextColor(color);
			}
		});
	}
	
	/***
	 * Creates a file name for the photos taken.
	 * for part photos, tmp names creates, because they will be deleted at the end of the process.
	 * @param forPart
	 * @return the file instance created
	 */
	private File getFileForImage(boolean forPart) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		File output = new File(home_directory_string + (forPart ? "/TMP_" : "/IMG_") + timeStamp + ".JPG");
		return output;
	}
	
	class CustomUncaughtExceptionHandler implements UncaughtExceptionHandler {
		public void uncaughtException(Thread thread, Throwable ex) {
			String message = ex.getMessage();
			System.out.println(message);
		}
	}

	/* ****************** END SORT OF UTILITY FUNCTIONS *************** */

	
	/* ****************** BEGIN METHODS FOR DIALOGS ************************ */
	private void showCompletionFragment() {
		DialogFragment newFragment = CompletionDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	public void positiveCompletionCallback() {
		showPicture(finalImagePath);
	}
	
	public void onPhotoCreationDialogCreated() {
		prepareFinalImage();
	}

	private void showAboutMe() {
		DialogFragment newFragment = AboutMeDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), "dialog");
	}
		
	private void showPhotoCreationDialog() {

		FragmentManager fm = getSupportFragmentManager();
		photoCreationFragment = PhotoCreationDialogFragment.newInstance();
		photoCreationFragment.show(fm, "photo create");

	}

	private void hidePhotoCreationDialog(final boolean isSuccess) {
		photoCreationFragment.dismissAllowingStateLoss();
		if (isSuccess)
			showCompletionFragment();
	}
	
	/***
	 * The intent to view images
	 * @param file
	 */
	private void showPicture(String file) {
		String selectedFile = file.replace("/mnt/", "/");
		Intent showPicIntent = new Intent(Intent.ACTION_VIEW);
		showPicIntent.setDataAndType(Uri.parse("file://" + selectedFile), "image/*");
		startActivity(showPicIntent);
	}
	
	/* ****************** END METHODS FOR DIALOGS ************************ */
	


	
	/* ********* BEGIN METHODS RELATED TO THE ACTION BAR ********************** */
	
	@SuppressLint("NewApi")
	private void toggleActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (getActionBar().isShowing()) {
				getActionBar().hide();
			} else {
				getActionBar().show();
			}
		}
	}

	@Override
	public MenuInflater getMenuInflater() {
		return mActionBarHelper.getMenuInflater(super.getMenuInflater());
	}
	
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
	
	/********** END METHODS RELATED TO THE ACTION BAR ********************** */
	
	
	
	
	/********** BEGIN ANIMATION DEFINITIONS ********************** */
	
	public static Animation outToLeftAnimation() {
		Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(400);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	public static Animation inFromRightAnimation() {

		Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(400);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}
	
	/********** BEGIN ANIMATION DEFINITIONS ********************** */
}