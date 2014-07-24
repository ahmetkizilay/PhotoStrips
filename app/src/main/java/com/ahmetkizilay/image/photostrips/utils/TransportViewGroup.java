package com.ahmetkizilay.image.photostrips.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ahmetkizilay.image.photostrips.R;

/**
 * This is a view group wrapper for the bottom panel. It shows the number of photos taken in a
 * session. On double click on the camera previes, shrink/expand animation is toggled.
 * Created by ahmetkizilay on 22.07.2014.
 */
public class TransportViewGroup extends TableLayout{

    private final int numSize = 5;
    private TextView[] tvNumbers;

    private boolean isShrunk = false;

    public TransportViewGroup(Context context) {
        super(context);
        init();
    }

    public TransportViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        inflater.inflate(R.layout.transport, this, true);

        this.tvNumbers = new TextView[numSize];
        this.tvNumbers[0] = (TextView) findViewById(R.id.tw1);
        this.tvNumbers[1] = (TextView) findViewById(R.id.tw2);
        this.tvNumbers[2] = (TextView) findViewById(R.id.tw3);
        this.tvNumbers[3] = (TextView) findViewById(R.id.tw4);
        // this view will always be invisible
        this.tvNumbers[4] = (TextView) findViewById(R.id.tw0); // this view will always be invisible
    }

    /***
     * Called from PhotoBoothActivity when CameraPreview is double tapped.
     * @param duration of the animation
     */
    public void toggle(int duration) {
        if(this.isShrunk) {
            expand(duration);
        }
        else {
            shrink(duration);
        }
    }

    private void shrink(int duration) {
        Animation anim = new ShrinkExpandAnimation(0.5f);
        anim.setDuration(duration);
        TransportViewGroup.this.startAnimation(anim);
        this.isShrunk = true;
    }

    private void expand(int duration) {
        Animation anim = new ShrinkExpandAnimation(2.0f);
        anim.setDuration(duration);
        TransportViewGroup.this.startAnimation(anim);
        this.isShrunk = false;
    }

    /***
     * changes the color of the text view on photo capture and at the end of photo session.
     *
     * @param index of the textView item
     * @param color to be set
     */
    public void setNumberColor(int index, int color) {
        if(index >= numSize) return;
        this.tvNumbers[index].setTextColor(color);
    }

    private class ShrinkExpandAnimation extends Animation {
        private float mShrinkBy;

        private float startHeight;
        private float endHeight;

        private float startTextSize;
        private float endTextSize;

        public ShrinkExpandAnimation(float shrinkBy) {
            this.mShrinkBy = shrinkBy;

            init();
        }

        private void init() {
            this.startHeight = (float) TransportViewGroup.this.getMeasuredHeight();
            this.endHeight =  this.startHeight * this.mShrinkBy;

            this.startTextSize = pixelsToSp(TransportViewGroup.this.getContext(), tvNumbers[0].getTextSize());
            this.endTextSize = this.startTextSize * this.mShrinkBy;
        }

        private float pixelsToSp(Context context, float px) {
            float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
            return px/scaledDensity;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int currentHeight = (int) (startHeight + ((endHeight - startHeight) * interpolatedTime));
            int currentTextSize = (int) (startTextSize + ((endTextSize - startTextSize) * interpolatedTime));

            for(int i = 0; i < tvNumbers.length; i += 1) {
                tvNumbers[i].setTextSize(currentTextSize);
            }

            // this is the only way I could change the TableRow view that is the direct descendant
            // of the TableLayout element...
            for(int i = 0; i < TransportViewGroup.this.getChildCount(); i += 1) {
                TransportViewGroup.this.getChildAt(i).getLayoutParams().height = (int) currentHeight;
            }

            TransportViewGroup.this.getLayoutParams().height = (int) currentHeight;
            TransportViewGroup.this.requestLayout();

        }
    }
}
