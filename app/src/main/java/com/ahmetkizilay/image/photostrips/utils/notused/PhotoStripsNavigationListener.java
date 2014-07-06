package com.ahmetkizilay.image.photostrips.utils.notused;

import com.ahmetkizilay.image.photostrips.GalleryActivity;
import com.ahmetkizilay.image.photostrips.PhotoBoothActivity;

import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;

public class PhotoStripsNavigationListener implements OnNavigationListener{
	
	private Activity parentActivity;
	
	public PhotoStripsNavigationListener(Activity parentActivity) {
		this.parentActivity = parentActivity;
	}
	
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		
		if(itemPosition != 0 && !(parentActivity instanceof PhotoBoothActivity)) {
			Intent swithViewIntent = new Intent(
					parentActivity, PhotoBoothActivity.class);
			swithViewIntent
					.setAction("com.ahmetkizilay.image.photostrips.PhotoBoothActivity");
			parentActivity.startActivity(swithViewIntent);
			return true;
		}
		
		if(itemPosition != 0 && !(parentActivity instanceof GalleryActivity)) {
			Intent swithViewIntent = new Intent(
					parentActivity, GalleryActivity.class);
			swithViewIntent
					.setAction("com.ahmetkizilay.image.photostrips.GalleryActivity");
			parentActivity.startActivity(swithViewIntent);
			return true;
		}
		
		return false;
		
	}

}
