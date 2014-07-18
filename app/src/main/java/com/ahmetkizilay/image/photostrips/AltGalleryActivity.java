package com.ahmetkizilay.image.photostrips;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.ahmetkizilay.image.photostrips.compat.ActionBarHelper;
import com.ahmetkizilay.image.photostrips.dialogs.AboutMeDialogFragment;
import com.ahmetkizilay.image.photostrips.utils.GalleryItemAdapter;
import com.ahmetkizilay.modules.donations.PaymentDialogFragment;
import com.ahmetkizilay.modules.donations.ThankYouDialogFragment;

import java.io.File;
import java.util.Locale;

public class AltGalleryActivity extends FragmentActivity {
	// using this for faking an action bar for earlier versions of android.
	final ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);

	private int disp_height;
	private int disp_width;
	
	private String home_directory_string = "";

    private GridView lvGallery;
    private GalleryItemAdapter mGalleryItemAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBarHelper.onCreate(savedInstanceState);

		setContentView(R.layout.alt_gallery);

        Configuration config = getResources().getConfiguration();
        if (config.locale == null)
            config.locale = Locale.getDefault();

        this.home_directory_string = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "perisonic" + File.separator + "image" + File.separator + "photostrips";
        this.mGalleryItemAdapter = new GalleryItemAdapter(this, this.home_directory_string);

        this.lvGallery = (GridView) findViewById(R.id.lvGallery);
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
