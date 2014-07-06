package com.ahmetkizilay.image.photostrips.utils.notused;

import android.hardware.Camera.Size;

public class SpinnerItem {
	private String display;
	private Size value;
	
	public SpinnerItem(Size size) {
		this.display = size.width + "x" + size.height;
		this.value = size;
	}
	
	public SpinnerItem() {
		
	}

	public String getDisplay() {
		return display;
	}

	public Size getValue() {
		return this.value;
	}
}
