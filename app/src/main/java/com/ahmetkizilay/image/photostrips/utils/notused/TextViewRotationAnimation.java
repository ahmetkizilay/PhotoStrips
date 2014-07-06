package com.ahmetkizilay.image.photostrips.utils.notused;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

public class TextViewRotationAnimation extends RotateAnimation implements android.view.animation.Animation.AnimationListener {
	
	private TextView view;
	private float finish;
	public TextViewRotationAnimation(final TextView view, float start, float finish, long duration) {
		super(start, finish, view.getWidth() * 0.5f, view.getHeight() * 0.5f);
				
		this.view = view;
		this.finish = start;
		
	    setDuration(duration);             
	    setRepeatCount(0);               
	    setFillAfter(true); 
	    
	    
	    setAnimationListener(this);
	}

	public void onAnimationEnd(Animation animation) {
		//view.setRotation(finish);
	}

	public void onAnimationRepeat(Animation animation) {
		
	}

	public void onAnimationStart(Animation animation) {
		
	}
	
	
	
}
