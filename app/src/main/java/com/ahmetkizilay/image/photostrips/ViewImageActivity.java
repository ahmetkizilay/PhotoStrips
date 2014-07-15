package com.ahmetkizilay.image.photostrips;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

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
    private Bitmap bmPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBarHelper.onCreate(savedInstanceState);

        Configuration config = getResources().getConfiguration();
        if (config.locale == null)
            config.locale = Locale.getDefault();

        Intent intent = getIntent();
        this.mPicture = Uri.parse(intent.getDataString());
        this.bmPicture = BitmapFactory.decodeFile(this.mPicture.getPath());

        setContentView(R.layout.display_image);


        final TouchImageView touchImageView = (TouchImageView) findViewById(R.id.img);
        touchImageView.setImageBitmap(this.bmPicture);
        touchImageView.setMaxZoom(5.0f);

        ImageButton btnRotate = (ImageButton) findViewById(R.id.btnRotateRight);
        btnRotate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Matrix matrix = new Matrix();
                matrix.postRotate(90);

                bmPicture = Bitmap.createScaledBitmap(bmPicture, bmPicture.getWidth(), bmPicture.getHeight(),true);
                bmPicture = Bitmap.createBitmap(bmPicture, 0, 0, bmPicture.getWidth(), bmPicture.getHeight(), matrix, true);

                touchImageView.setImageBitmap(bmPicture);
            }
        });

        ImageButton btnRotateLeft = (ImageButton) findViewById(R.id.btnRotateLeft);
        btnRotateLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Matrix matrix = new Matrix();
                matrix.postRotate(-90);

                bmPicture = Bitmap.createScaledBitmap(bmPicture, bmPicture.getWidth(), bmPicture.getHeight(),true);
                bmPicture = Bitmap.createBitmap(bmPicture, 0, 0, bmPicture.getWidth(), bmPicture.getHeight(), matrix, true);

                touchImageView.setImageBitmap(bmPicture);
            }
        });

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

    private float getScreenRatio() {
        Point size = new Point();
        WindowManager w = getWindowManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2){
            w.getDefaultDisplay().getSize(size);
            return (float)size.y / (float)size.x;
        }else{
            Display d = w.getDefaultDisplay();
            //noinspection deprecation
            return (float)d.getHeight() / (float)d.getWidth();
        }
    }
}
