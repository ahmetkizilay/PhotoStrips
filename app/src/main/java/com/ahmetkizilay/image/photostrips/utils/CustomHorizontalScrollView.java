package com.ahmetkizilay.image.photostrips.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

/***
 * I found this code somewhere on stack overflow and modified it according to my needs.
 * Listens to the end of horizontal scroll view scroll actions and triggers an event 
 * 
 * @author ahmetkizilay
 *
 */
public class CustomHorizontalScrollView extends HorizontalScrollView {
	
	private boolean occupied = false;
	private boolean toBeTriggered = false;
	private ScrollStoppedEvent eventToTrigger;
	
	private Runnable scrollerTask;
	private int initialPosition;

	private int newCheck = 100;
	
	private OnScrollStoppedListener onScrollStoppedListener;

	public final class ScrollStoppedEvent {
		public static final int SCROLL_START = 0;
		public static final int SCROLL_END = 1;

		private int action;

		private ScrollStoppedEvent(int action) {
			this.action = action;
		}

		public int getAction() {
			return this.action;
		}
	}

	public interface OnScrollStoppedListener {
		void onScrollStopped(ScrollStoppedEvent evt);
	}

	public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				

				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					startScrollerTask();
				}
				
				if (event.getAction() == MotionEvent.ACTION_UP) {
					endScrollTask();
				}
				
				
				return false;
			}
		});

		scrollerTask = new Runnable() {

			public void run() {

				int newPosition = getScrollX();
				if (initialPosition - newPosition == 0) {// has stopped

					if (onScrollStoppedListener != null) {

						eventToTrigger = new ScrollStoppedEvent(
								newPosition == 0 ? ScrollStoppedEvent.SCROLL_START
										: ScrollStoppedEvent.SCROLL_END);
						
						toBeTriggered = true;						
					}
				} else {
					occupied = false;
				}				
			}
		};
	}


	public void endScrollTask() {
		occupied = false;
		if(toBeTriggered) {
			toBeTriggered = false;
			onScrollStoppedListener.onScrollStopped(eventToTrigger);			
		}
	}
	
	public void setOnScrollStoppedListener(
			CustomHorizontalScrollView.OnScrollStoppedListener listener) {
		onScrollStoppedListener = listener;
	}

	public void startScrollerTask() {

		if(!occupied) {
			occupied = true;
			toBeTriggered = false;
			
			initialPosition = getScrollX();
			CustomHorizontalScrollView.this.postDelayed(scrollerTask, newCheck);
		}
	}


}