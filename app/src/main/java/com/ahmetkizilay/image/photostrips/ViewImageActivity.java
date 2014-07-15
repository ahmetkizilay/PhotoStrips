package com.ahmetkizilay.image.photostrips;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.ahmetkizilay.image.photostrips.compat.ActionBarHelper;
import com.ahmetkizilay.image.photostrips.dialogs.AboutMeDialogFragment;
import com.ahmetkizilay.image.photostrips.dialogs.ConfirmDialogFragment;
import com.ahmetkizilay.image.photostrips.utils.TouchImageView;
import com.ahmetkizilay.modules.donations.PaymentDialogFragment;
import com.ahmetkizilay.modules.donations.ThankYouDialogFragment;
import com.ahmetkizilay.modules.listapps.AppListerViewGroup;

import java.io.File;
import java.util.Locale;

public class ViewImageActivity extends FragmentActivity {
    // using this for faking an action bar for earlier versions of android.
    final ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);

    private AppListerViewGroup wgSharePanel;
    private Uri mPicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBarHelper.onCreate(savedInstanceState);

        Configuration config = getResources().getConfiguration();
        if (config.locale == null)
            config.locale = Locale.getDefault();

        Intent intent = getIntent();
        this.mPicture = Uri.parse(intent.getDataString());

        setContentView(R.layout.display_image);

        final TouchImageView touchImageView = (TouchImageView) findViewById(R.id.img);
        touchImageView.setImageURI(this.mPicture);

        this.wgSharePanel = (AppListerViewGroup) findViewById(R.id.wgAppListerBottom);
        this.wgSharePanel.setListItemClickedListener(new AppListerViewGroup.ListItemClickedListener() {
            public void onListItemClicked(String packageName, String appName) {
                wgSharePanel.setVisibility(View.INVISIBLE);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);

                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                shareIntent.putExtra(Intent.EXTRA_STREAM, mPicture);
                shareIntent.setClassName(packageName, appName);

                startActivity(shareIntent);
            }
        });

        ImageButton btnToggleBottom = (ImageButton) findViewById(R.id.btnShare);
        btnToggleBottom.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (ViewImageActivity.this.wgSharePanel.getVisibility() == View.VISIBLE) {
                    ViewImageActivity.this.wgSharePanel.setVisibility(View.INVISIBLE);
                } else {
                    ViewImageActivity.this.wgSharePanel.setMinimalMode();
                    ViewImageActivity.this.wgSharePanel.setVisibility(View.VISIBLE);
                }
            }
        });

        ImageButton btnDelete = (ImageButton) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                ConfirmDialogFragment deleteConfirmDialog = ConfirmDialogFragment.newInstance("Are you sure to delete this picture?", "Delete", "Cancel");
                deleteConfirmDialog.setConfirmDialogResultListener(new ConfirmDialogFragment.ConfirmDialogResultListener() {
                    public void onPositiveSelected() {
                        File file = new File(mPicture.getPath());
                        file.delete();

                        Intent resultIntent = new Intent();
                        resultIntent.setData(mPicture);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }

                    public void onNegativeSelected() {
                        // do nothing
                    }
                });

                pushToStack(deleteConfirmDialog, "confirm-delete");
            }
        });
    }

    private void pushToStack(DialogFragment frag, String label) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(label);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        frag.show(ft, label);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                showAboutMe();
                return true;
            case R.id.menu_gallery:
                Intent galleryIntent = new Intent(this, AltGalleryActivity.class);
                galleryIntent.setAction("com.ahmetkizilay.image.photostrips.AltGalleryActivity");
                this.startActivity(galleryIntent);
                return true;
            case R.id.menu_booth:
                Intent boothIntent = new Intent(this, PhotoBoothActivity.class);
                boothIntent.setAction("com.ahmetkizilay.image.photostrips.PhotoBoothActivity");
                this.startActivity(boothIntent);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
