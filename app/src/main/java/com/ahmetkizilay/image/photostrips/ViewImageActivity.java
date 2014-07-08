package com.ahmetkizilay.image.photostrips;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ahmetkizilay.image.photostrips.compat.ActionBarHelper;
import com.ahmetkizilay.image.photostrips.dialogs.AboutMeDialogFragment;
import com.ahmetkizilay.image.photostrips.utils.TouchImageView;

public class ViewImageActivity extends FragmentActivity {
    // using this for faking an action bar for earlier versions of android.
    final ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBarHelper.onCreate(savedInstanceState);

        Intent intent = getIntent();

        setContentView(R.layout.display_image);

        TouchImageView touchImageView = (TouchImageView) findViewById(R.id.img);

        touchImageView.setImageURI(Uri.parse(intent.getDataString()));
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
