package com.ahmetkizilay.image.photostrips;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ahmetkizilay.image.photostrips.compat.ActionBarHelper;
import com.ahmetkizilay.image.photostrips.dialogs.AboutMeDialogFragment;
import com.ahmetkizilay.image.photostrips.utils.CustomHorizontalScrollView;
import com.ahmetkizilay.image.photostrips.utils.CustomHorizontalScrollView.OnScrollStoppedListener;
import com.ahmetkizilay.image.photostrips.utils.CustomHorizontalScrollView.ScrollStoppedEvent;
import com.ahmetkizilay.image.photostrips.utils.GalleryItemAdapter;
import com.ahmetkizilay.modules.donations.PaymentDialogFragment;
import com.ahmetkizilay.modules.donations.ThankYouDialogFragment;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class AltGalleryActivity extends FragmentActivity {
	// using this for faking an action bar for earlier versions of android.
	final ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);

	private int disp_height;
	private int disp_width;
	
	private String home_directory_string = "";

    private ListView lvGallery;
    private GalleryItemAdapter mGalleryItemAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBarHelper.onCreate(savedInstanceState);

		setContentView(R.layout.port_gallery);

        Configuration config = getResources().getConfiguration();
        if (config.locale == null)
            config.locale = Locale.getDefault();

        this.home_directory_string = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "perisonic" + File.separator + "image" + File.separator + "photostrips";
        this.lvGallery = (ListView) findViewById(R.id.lvGallery);
        this.mGalleryItemAdapter = new GalleryItemAdapter(this, this.home_directory_string);
        this.lvGallery.setAdapter(this.mGalleryItemAdapter);

        TextView twEmptyGallery = (TextView) findViewById(R.id.twEmptyGallery);
        if(this.mGalleryItemAdapter.getCount() != 0) {
            twEmptyGallery.setVisibility(View.GONE);
        }
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
        AboutMeDialogFragment newFragment = AboutMeDialogFragment.newInstance();
        newFragment.setRequestListener(new AboutMeDialogFragment.RequestListener() {
            public void onDonationsRequested() {
                showDonationDialog();
            }
        });
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    private void showDonationDialog() {
        final PaymentDialogFragment newFragment = PaymentDialogFragment.getInstance(R.array.product_ids);
        newFragment.setPaymentCompletedListener(new PaymentDialogFragment.PaymentCompletedListener() {
            public void onPaymentCompleted() {
                newFragment.dismiss();
                showThankYouDialog();
            }
        });
        newFragment.show(getSupportFragmentManager(), "frag-donations");
    }

    private void showThankYouDialog() {
        final ThankYouDialogFragment newFragment = ThankYouDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "frag-thanks");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0) {
            if(resultCode == RESULT_OK) {
                this.mGalleryItemAdapter.removeItem(data.getData().getPath());
                this.mGalleryItemAdapter.notifyDataSetChanged();
            }
        }

        // pass the request back to the fragment
        if(requestCode == PaymentDialogFragment.PAYMENT_RESULT_CODE) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag("frag-donations");
            if (fragment != null)
            {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
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
