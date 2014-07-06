package com.ahmetkizilay.image.photostrips.utils;

import java.util.Timer;
import java.util.TimerTask;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/***
 * Detects number of taps on the View.
 * I know this is not really implemented like a listener. I will most likely go back and modify it.
 * But it works.
 * @author ahmetkizilay
 *
 */
public abstract class OnMultiTapListener implements OnTouchListener{
	
	private final int MAX_TAP_DELAY = 500;
	
	private int tapCount = 0;	
	private long prevTapTime;
	
	private Timer timer = new Timer("multitap_timer");
	public OnMultiTapListener() {
		
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() != MotionEvent.ACTION_DOWN) {
			return false;
		}
		
		long thisTapTime = SystemClock.uptimeMillis();
		if((thisTapTime - prevTapTime) > MAX_TAP_DELAY) {
			tapCount = 1;
			timer.cancel();
			
			(timer = new Timer("multitap_timer")).schedule(new TimerTask() {

				public void run() {
					onSingleTap();
					tapCount = 0;
				}
			}, MAX_TAP_DELAY);	
		}
		else {
			
			// increase tapCount;
			// cancel previous timer,
			// initialize timer for the next one
			
			tapCount++;
			timer.cancel();
			(timer = new Timer("multitap_timer")).schedule(new TimerTask() {

				public void run() {
					switch(tapCount) {
					case 2:
						onDoubleTap();
						break;
					default:
						onMultiTap(tapCount);
						break;
					}
					
					tapCount = 0;
				}
			}, MAX_TAP_DELAY);	
		}
				
		prevTapTime = thisTapTime;
		return false;
	}
	
	public abstract void onSingleTap();
	public abstract void onDoubleTap();
	public abstract void onMultiTap(int tapCount);
	
}
