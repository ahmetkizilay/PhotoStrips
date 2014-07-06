package com.ahmetkizilay.image.photostrips;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/***
 * Another piece of code I found online to prepare camera preview
 * @author ahmetkizilay
 *
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;

		mHolder = getHolder();
		mHolder.addCallback(this);

		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d("CamPrev", "Error setting camera preview: " + e.getMessage());
		} catch (NullPointerException npe) {
			Log.d("CamPrev", "Error setting camera prev:" + npe.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {

		if (mCamera != null) {
			try {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
			} catch (Exception exp) {
				Log.d("CamPrev", "Error Surface Destroyed:" + exp.getMessage());
			}
			mCamera = null;
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mHolder.getSurface() == null) {
			return;
		}

		try {
			mCamera.stopPreview();
		} catch (Exception e) {
		}


		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();

		} catch (Exception e) {
			Log.d("CamPrev", "Error starting camera preview: " + e.getMessage());
		}
	}
}