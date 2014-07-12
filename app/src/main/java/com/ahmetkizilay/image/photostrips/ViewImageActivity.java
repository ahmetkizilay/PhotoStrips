package com.ahmetkizilay.image.photostrips;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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
import com.ahmetkizilay.modules.listapps.AppListerViewGroup;

import java.io.File;

public class ViewImageActivity extends FragmentActivity {
    // using this for faking an action bar for earlier versions of android.
    final ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);

    private AppListerViewGroup wgSharePanel;
    private Uri mPicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBarHelper.onCreate(savedInstanceState);

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

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("confirm-delete");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                ConfirmDialogFragment deleteConfirmDialog = ConfirmDialogFragment.newInstance("Are you sure to delete this picture?", "Delete", "Cancel");
                deleteConfirmDialog.setConfirmDialogResultListener(new ConfirmDialogFragment.ConfirmDialogResultListener() {
                    public void onPositiveSelected() {
                        File file = new File(mPicture.getPath());
                        file.delete();

                        setResult(RESULT_CANCELED, new Intent());
                        finish();
                    }

                    public void onNegativeSelected() {
                        // do nothing
                    }
                });

                deleteConfirmDialog.show(ft, "confirm-delete");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                showAboutMe();
                return true;
            case R.id.menu_gallery:
                Intent galleryIntent = new Intent(this, GalleryActivity.class);
                galleryIntent.setAction("com.ahmetkizilay.image.photostrips.GalleryActivity");
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
        DialogFragment newFragment = AboutMeDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "dialog");
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
