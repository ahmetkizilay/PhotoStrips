package com.ahmetkizilay.image.photostrips;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ahmetkizilay.image.photostrips.dialogs.AboutMeDialogFragment;
import com.ahmetkizilay.image.photostrips.dialogs.CompletionDialogFragment;
import com.ahmetkizilay.image.photostrips.dialogs.PhotoCreationDialogFragment;
import com.ahmetkizilay.image.photostrips.utils.OnDoubleTapListener;
import com.ahmetkizilay.image.photostrips.utils.TransportViewGroup;
import com.ahmetkizilay.modules.donations.PaymentDialogFragment;
import com.ahmetkizilay.modules.donations.ThankYouDialogFragment;

public class PhotoBoothActivity extends ActionBarActivity implements GestureDetector.OnGestureListener {

	private int disp_height, disp_width;

	private boolean isCapturing = false;
	private boolean isSoundOn = true;
	private boolean isPortaitView = false;

	private int photoCount = 0;
	List<String> photoParts = new ArrayList<String>();
	private String finalImagePath = null;
	private String home_directory_string = "";

    /***
     * The bottom panel
     */
    private TransportViewGroup loTransport;
	
	private int camId = 0;
	private boolean isCameraFrontFacing = true;
	Camera mCamera;
	CameraPreview mPreview;
    ImageButton captureButton;

	private ImageButton btnSound;
	private MediaPlayer mpOne;
	
	private ImageButton btnRotatePortrait;
	private ImageButton btnRotateLandscape;

	private DialogFragment photoCreationFragment;

    // timer mode vs demand mode
    // timer mode is when capturing is done automatically with a timer
    // demand mode is when user taps the capture button to take photographs
    private boolean mIsTimerMode = true;

    /***
     * This method is called after taking a picture in onDemand mode.
     * Stores the image and holds a reference to its location in photoParts array
     */
    private PictureCallback demandModePictureTakenCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getFileForImage(true);
            if (pictureFile == null) {
                Log.d("", "Error creating file");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (Exception exp) {
                Log.d("", "Error: " + exp.getMessage());
            }

            photoParts.add(pictureFile.getAbsolutePath());
            camera.startPreview();

            if(photoCount == 4) {
                showPhotoCreationDialog();
            }
        }
    };

	/***
	 *  callback function that runs after a photo is taken.
	 *  saves the captured data as a tmp image file.
	 */
	private PictureCallback pictureTakenCallback = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {
			File pictureFile = getFileForImage(true);
			if (pictureFile == null) {
				Log.d("", "Error creating file");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (Exception exp) {
				Log.d("", "Error: " + exp.getMessage());
			}

			photoParts.add(pictureFile.getAbsolutePath());
			camera.startPreview();

			takeMyPicture();
		}
	};

	/***
	 * runnable instance that is called after camera is initialized.
	 * camera initialization takes place in a separate thread. so the UI modifications should be run 
	 * with runOnUIThread method.
	 */
	final Runnable postCameraInit = new Runnable() {

		public void run() {

            removePreview();

            mPreview = new CameraPreview(PhotoBoothActivity.this, mCamera);
            mPreview.setOnTouchListener(null);
            mPreview.setOnTouchListener(new OnDoubleTapListener() {
                public void onDoubleTap() {

                    PhotoBoothActivity.this.runOnUiThread(new Runnable() {

                        public void run() {
                            toggleActionBar();
                            toggleSettingsBar();
                            loTransport.toggle(400);
                        }
                    });
                }
            });

			FrameLayout preview = (FrameLayout) findViewById(R.id.mainLayout);
			preview.addView(mPreview, 0);
		}
	};

    /**
     * Fling right or left to apply supported color effects
     */
    private GestureDetectorCompat mGestureDetector;
    private List<String> mColorEffects;
    private int numColorEffects = 0;
    private int currentColorEffectIndex = 0;


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// mActionBarHelper.onCreate(savedInstanceState);

		setContentView(R.layout.booth);

        Configuration config = getResources().getConfiguration();
        if (config.locale == null)
            config.locale = Locale.getDefault();

		try {
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				Log.d("", "External SD Card Is Not Detected!");
				throw new Exception("External SD Card Is Not Detected!");
			}

			home_directory_string = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "perisonic"; 
			
			File home_directory = new File(home_directory_string);
			if (!home_directory.exists() && !home_directory.mkdirs()) {
				Log.d("", "home directory could not be created!");
				throw new Exception("External SD Card Is Not Detected!");

			}

			home_directory_string += File.separator + "image";
			home_directory = new File(home_directory_string);
			if (!home_directory.exists() && !home_directory.mkdirs()) {
				Log.d("", "home directory could not be created!");
				throw new Exception("Could Not Create Directory!");
			}

			home_directory_string += File.separator + "photostrips";
			home_directory = new File(home_directory_string);
			if (!home_directory.exists() && !home_directory.mkdirs()) {
				Log.d("", "home directory could not be created!");
				throw new Exception("Could Not Create Directory!");
			}

            String thumb_directory_string = home_directory_string + File.separator + ".thumb";
            File thumb_directory = new File(thumb_directory_string);
            if (!thumb_directory.exists() && !thumb_directory.mkdirs()) {
                Log.d("", "home directory could not be created!");
                throw new Exception("Could Not Create Directory!");
            }



            String thumb_land_directory_string = home_directory_string + File.separator + ".thumb-land";
            File thumb_land_directory = new File(thumb_land_directory_string);
            if (thumb_land_directory.exists()) {
                thumb_land_directory.delete();
            }

            String thumb_port_directory_string = home_directory_string + File.separator + ".thumb-port";
            File thumb_port_directory = new File(thumb_port_directory_string);
            if (thumb_port_directory.exists()) {
                thumb_port_directory.delete();
            }

		} catch (Exception exp) {
			Toast.makeText(this, "Error During Initialization", Toast.LENGTH_LONG).show();
		}

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		boolean hasActionBar = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

		DisplayMetrics dispMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);

		disp_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dispMetrics.heightPixels, dispMetrics);
		disp_width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dispMetrics.widthPixels, dispMetrics);

		int actionBarCompensation = hasActionBar ? 0 : (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, dispMetrics);
		int marg8dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, dispMetrics);
		int loHeight = (int) ((float) (disp_height - disp_width - actionBarCompensation) * 0.5);

		// Setting the positions for the views dynamically for better visual design
		this.loTransport = (TransportViewGroup) findViewById(R.id.loTransport);

		RelativeLayout loSettings = (RelativeLayout) findViewById(R.id.loSettings);
		FrameLayout.LayoutParams loSettingsParams = new FrameLayout.LayoutParams(loTransport.getLayoutParams());
		loSettingsParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, dispMetrics);
		loSettingsParams.gravity = Gravity.TOP;
		loSettingsParams.setMargins(marg8dp, (int) ((float) (loHeight - actionBarCompensation) * 0.5), marg8dp, 0);
		loSettings.setLayoutParams(loSettingsParams);

		captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                if(!mIsTimerMode) {
                    if(isCapturing) {
                        isCapturing = false;
                        cancelCapturing(true);
                    }
                    else {
                        prepareForCapture();
                    }
                }
                return true;
            }
        });
		captureButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                if (!mIsTimerMode) {
                    if (isCapturing) {
                        demandModeTakeMyPicture();
                    } else {
                        Toast.makeText(PhotoBoothActivity.this, "Long Press to start session", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (!isCapturing) {
                        prepareForCapture();
                        takeMyPicture();

                    } else {
                        cancelCapturing(true);
                    }
                }
            }
        });

        btnSound = (ImageButton) findViewById(R.id.btnSound);
		
		btnSound.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (isCapturing) {
					return;
				}

				if (isSoundOn) {
					btnSound.setImageResource(R.drawable.sound_off);
					isSoundOn = false;
									
				} else {
					btnSound.setImageResource(R.drawable.sound_on);
					isSoundOn = true;
				}
			}
		});

		btnRotatePortrait = (ImageButton) findViewById(R.id.btnRotatePortrait);
		btnRotateLandscape = (ImageButton) findViewById(R.id.btnRotateLandscape);

		btnRotatePortrait.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if(isCapturing) return;
				
				isPortaitView = true;
				btnRotatePortrait.setBackgroundResource(R.drawable.selectedbutton);
				btnRotateLandscape.setBackgroundResource(R.drawable.unselectedbutton);
			}
		});

		btnRotateLandscape.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if(isCapturing) return;
				
				isPortaitView = false;
				btnRotateLandscape.setBackgroundResource(R.drawable.selectedbutton);
				btnRotatePortrait.setBackgroundResource(R.drawable.unselectedbutton);
			}
		});

		final ImageButton btnSwitchCam = (ImageButton) findViewById(R.id.btnSwitchCam);
		btnSwitchCam.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (Camera.getNumberOfCameras() > 1) {
					camId = (camId + 1) % Camera.getNumberOfCameras();
					runOnUiThread(new Runnable() {
						public void run() {
							initCamera();
						}
					});
				}
			}

		});

        // switches between timer mode and on demand mode
        final ImageButton btnSwitchMode = (ImageButton) findViewById(R.id.btnSwitchMode);
        btnSwitchMode.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if(!isCapturing) {
                    if(mIsTimerMode) {
                        mIsTimerMode = false;
                        btnSwitchMode.setBackgroundResource(R.drawable.unselectedbutton);
                        Toast.makeText(PhotoBoothActivity.this, "Demand Mode: long press to start/stop session", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mIsTimerMode = true;
                        btnSwitchMode.setBackgroundResource(R.drawable.selectedbutton);
                        Toast.makeText(PhotoBoothActivity.this, "Timer Mode: press to start/stop capturing", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

		RelativeLayout relLayout = (RelativeLayout) findViewById(R.id.loSettings);
		relLayout.setVisibility(View.VISIBLE);

		mpOne = MediaPlayer.create(getApplicationContext(), R.raw.one_wav);

		LinearLayout topBlur = (LinearLayout) findViewById(R.id.loTopBlur);
		ViewGroup.LayoutParams loParams = topBlur.getLayoutParams();
		loParams.height = loHeight;
		topBlur.setLayoutParams(loParams);

		LinearLayout bottomBlur = (LinearLayout) findViewById(R.id.loBottomBlur);
		loParams = bottomBlur.getLayoutParams();
		loParams.height = loHeight;
		bottomBlur.setLayoutParams(loParams);

        this.mGestureDetector = new GestureDetectorCompat(this, this);
		Thread.setDefaultUncaughtExceptionHandler(new
		 CustomUncaughtExceptionHandler());

	}
	
	/***
	 * camera is initialized here.
	 */
	@Override	
	protected void onResume() {
		super.onResume();
		
		if (mCamera == null) {
			initCamera();
		}

        if(mpOne == null) {
            mpOne = MediaPlayer.create(getApplicationContext(), R.raw.one_wav);
        }

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean("isPortraitView", isPortaitView);
		outState.putBoolean("isSoundOn", isSoundOn);
		outState.putInt("camId", camId);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {	
		super.onRestoreInstanceState(savedInstanceState);
		
		this.isPortaitView  = savedInstanceState.getBoolean("isPortraitView");
		this.isSoundOn = savedInstanceState.getBoolean("isSoundOn");
		this.camId = savedInstanceState.getInt("camId");
		
		if (isSoundOn) {
			btnSound.setImageResource(R.drawable.sound_on);
		} else {
			btnSound.setImageResource(R.drawable.sound_off);			
		}
		
		if(this.isPortaitView) {
			btnRotatePortrait.setBackgroundResource(R.drawable.selectedbutton);
			btnRotateLandscape.setBackgroundResource(R.drawable.unselectedbutton);
		}
		else {
			btnRotateLandscape.setBackgroundResource(R.drawable.selectedbutton);
			btnRotatePortrait.setBackgroundResource(R.drawable.unselectedbutton);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
		if (mpOne != null) {
			mpOne.release();
            mpOne = null;
		}

        removePreview();
	}

	@Override
	protected void onPause() {
		super.onPause();

        if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();

			mCamera = null;
		}

        removePreview();

		if (isCapturing) {
			cancelCapturing(true);
		}

        if (mpOne != null) {
            mpOne.release();
            mpOne = null;
        }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			showAboutMe();
			return true;
		case R.id.menu_gallery:
			if(isCapturing) cancelCapturing(true);
			
			Intent switchViewIntent = new Intent(this, AltGalleryActivity.class);
			switchViewIntent.setAction("com.ahmetkizilay.image.photostrips.AltGalleryActivity");
			this.startActivity(switchViewIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
			cancelCapturing(isCapturing);
		}

		return super.onKeyDown(keyCode, event);
	}


    private void removePreview() {
        if (mPreview != null) {
            mPreview.setOnTouchListener(null);
            mPreview.clearAnimation();

            FrameLayout preview = (FrameLayout) findViewById(R.id.mainLayout);
            preview.removeView(mPreview);

            mPreview = null;
        }
    }
	/***
	 * Method for initializing the camera, and the camera preview.
	 * Initialization takes place in a separate thread to prevent UI blocking.
	 */
	private void initCamera() {

		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}

        removePreview();

		Thread t = new Thread(new Runnable() {

			public void run() {
				if (mCamera == null) {
					mCamera = getCameraInstance();
                    if(mCamera == null) {
                        //Toast.makeText(PhotoBoothActivity.this, "Could not connect to the camera", Toast.LENGTH_SHORT).show();
                        return;
                    }

					int result = getCameraRotation();

					Parameters params = mCamera.getParameters();
                    List<Size> supportedPictureSizes = params.getSupportedPictureSizes();
                    Size pictureSize = supportedPictureSizes.get((supportedPictureSizes.size() / 2) + (supportedPictureSizes.size() % 2));
                    params.setPictureSize(pictureSize.width, pictureSize.height);

                    mColorEffects = params.getSupportedColorEffects();
                    if(mColorEffects != null && mColorEffects.size() > 0) {
                        numColorEffects = mColorEffects.size();
                        currentColorEffectIndex = 0;
                        params.setColorEffect(Parameters.EFFECT_NONE);
                    }
                    else {
                        numColorEffects = 0; currentColorEffectIndex = 0;
                    }

                    FrameLayout preview = (FrameLayout) findViewById(R.id.mainLayout);

                    Size previewSize = getOptimalPreviewSize(params.getSupportedPreviewSizes(), preview.getMeasuredWidth(), preview.getMeasuredHeight());
                    //Size previewSize = params.getPreviewSize();
					if (result == 90 || result == 270) {
						params.setPreviewSize(previewSize.width, previewSize.height);
					} else {
						params.setPreviewSize(previewSize.height, previewSize.width);
					}

                    params.setZoom(0);
					mCamera.setParameters(params);
					mCamera.setDisplayOrientation(result);

					// UI modifications should run on UI thread
					PhotoBoothActivity.this.runOnUiThread(postCameraInit);
				}
			}
			
			/***
			 * Simply opens the camera, and returns the camera instance
			 * @return the currently used camera
			 */
			private Camera getCameraInstance() {
				Camera c = null;
				try {
					c = Camera.open(camId);
				} catch (Exception exp) {
					PhotoBoothActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(PhotoBoothActivity.this, "Could Not Open Camera", Toast.LENGTH_LONG).show();
						}
					});
				}
				return c;
			}

			/***
			 * Calculates the camera rotation for camera preview.
			 * I basically copied this code from the Android docs.
			 * @return rotation angle
			 */
			private int getCameraRotation() {
				android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
				android.hardware.Camera.getCameraInfo(camId, info);

				int degrees = 0;
				switch (getWindowManager().getDefaultDisplay().getRotation()) {
				case Surface.ROTATION_0:
					degrees = 0;
					break;
				case Surface.ROTATION_90:
					degrees = 90;
					break;
				case Surface.ROTATION_180:
					degrees = 180;
					break;
				case Surface.ROTATION_270:
					degrees = 270;
					break;
				}

				int result;
				isCameraFrontFacing = info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
				if (isCameraFrontFacing) {
					result = (info.orientation + degrees) % 360;
					result = (360 - result) % 360; // compensate the mirror
				} else { // back-facing
					result = (info.orientation - degrees + 360) % 360;
				}
				return result;
			}
			
			/***
			 * selects the most suitable preview size, based on the available preview sizes and the width/height of the container
             * method taken from: http://stackoverflow.com/a/19592492/210391
			 * @param sizes, width, height
			 * @return the most suitable preview size
			 */
            private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
                final double ASPECT_TOLERANCE = 0.1;
                double targetRatio=(double)h / w;

                if (sizes == null) return null;

                Camera.Size optimalSize = null;
                double minDiff = Double.MAX_VALUE;

                int targetHeight = h;

                for (Camera.Size size : sizes) {
                    double ratio = (double) size.width / size.height;
                    if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }

                if (optimalSize == null) {
                    minDiff = Double.MAX_VALUE;
                    for (Camera.Size size : sizes) {
                        if (Math.abs(size.height - targetHeight) < minDiff) {
                            optimalSize = size;
                            minDiff = Math.abs(size.height - targetHeight);
                        }
                    }
                }
                return optimalSize;
            }

		});
		
		t.start();
	}

    private void demandModeTakeMyPicture() {

        new Thread(new Runnable() {
            public void run() {
                changePhotoCountFontColor(photoCount, Color.RED);
                photoCount += 1;

                mCamera.takePicture(isSoundOn ? new Camera.ShutterCallback() {
                    public void onShutter() {
                    }
                } : null, null, demandModePictureTakenCallback);

            }
        }).start();
    }

	/***
	 * method for taking pictures. starts the process as a separate thread.
	 * takes a picture and the camera callback recursively triggers this method again for the next picture.
	 */
	private void takeMyPicture() {
		if (!isCapturing) {
			return;
		}

		if (++photoCount > 4) {
			showPhotoCreationDialog();
			return;
		}

		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
                    long sleepTime = 750;
					Thread.sleep(sleepTime);

					if (!isCapturing)
						return;

					changePhotoCountFontColor(photoCount - 1, Color.RED);

					changeCaptureButtonResource(R.drawable.number_three);
					if (isSoundOn)
						mpOne.start();
					Thread.sleep(sleepTime);

					if (!isCapturing)
						return;
					changeCaptureButtonResource(R.drawable.number_two);

					if (isSoundOn)
						mpOne.start();
					Thread.sleep(sleepTime);

					if (!isCapturing)
						return;
					changeCaptureButtonResource(R.drawable.number_one);
					if (isSoundOn)
						mpOne.start();
					Thread.sleep(sleepTime);

					if (!isCapturing)
						return;

					changeCaptureButtonResource(R.drawable.empty);
					if (!isCapturing)
						return;

					mCamera.takePicture(isSoundOn ? new Camera.ShutterCallback() {
						public void onShutter() {
						}
					} : null, null, pictureTakenCallback);
				} catch (Exception exp) {
					exp.printStackTrace();
				}
			}

		});
		t.start();
	}
	
	/***
	 * called after 4 pictures are taken. calls the method to combine 4 pictures.
	 */
	private void prepareFinalImage() {

		Thread t = new Thread(new Runnable() {
			public void run() {				
				String newFilePath = getFileForImage(false).getAbsolutePath();
				boolean isSuccess = PhotoCreator.prepareFinalImage(photoParts, isCameraFrontFacing, isPortaitView, newFilePath);
				if(isSuccess) finalImagePath = newFilePath;
				cancelCapturing(false);
				hidePhotoCreationDialog(isSuccess);
			}
		});
		t.start();
	}

	/***
	 * Double-Tapping on the screens toggles the action bar and the settings layout
	 */
	private void toggleSettingsBar() {
		Animation anim = null;
		final RelativeLayout rel = (RelativeLayout) findViewById(R.id.loSettings);
		if (rel.getVisibility() == View.VISIBLE) {
			anim = outToLeftAnimation();
			anim.setAnimationListener(new Animation.AnimationListener() {

				public void onAnimationStart(Animation arg0) {
				}

				public void onAnimationRepeat(Animation arg0) {
				}

				public void onAnimationEnd(Animation arg0) {
					rel.setVisibility(View.INVISIBLE);
				}
			});
		} else {
			anim = inFromRightAnimation();
			anim.setAnimationListener(new Animation.AnimationListener() {

				public void onAnimationStart(Animation arg0) {
					rel.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation arg0) {
				}

				public void onAnimationEnd(Animation arg0) {
				}
			});
		}
		anim.setDuration(400);
		rel.startAnimation(anim);
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

    /* ******************BEGIN SORT OF UTILITY FUNCTIONS *************** */

    /***
     * called when capturing is enabled.
     * clearing variables for storing photo parts
     */
    private void prepareForCapture() {
        isCapturing = true;
        photoCount = 0;
        photoParts.clear();
        finalImagePath = null;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        captureButton.setBackgroundResource(R.drawable.roundedbutton_capturing);
    }

	/***
	 * reverts Views to their default states.
     * Checks if current thread is the UI thread to make sure Views are properly reverted
	 * @param cancelled if photo capture is cancelled
	 */
	private void cancelCapturing(boolean cancelled) {
		isCapturing = false;

        if(Looper.getMainLooper().getThread() == Thread.currentThread()) {
            cancelCapturingUI();
        }
        else {
            runOnUiThread(new Runnable() {
                public void run() {
                    cancelCapturingUI();
                }
            });
        }

		deletePartPhotoFiles();

		if (cancelled) {
			Toast.makeText(this, "Photo Cancelled", Toast.LENGTH_SHORT).show();
		}
	}

    /**
     * This is the UI Related part of cancel capturing.
     * cancelCapturing method checks if it is currently on the UI Thread.
     * If not, it makes sure this method run on the UI Thread.
     */
    private void cancelCapturingUI() {
        changePhotoCountFontColor(0, Color.WHITE);
        changePhotoCountFontColor(1, Color.WHITE);
        changePhotoCountFontColor(2, Color.WHITE);
        changePhotoCountFontColor(3, Color.WHITE);

        changeCaptureButtonResource(R.drawable.action_new);
        captureButton.setBackgroundResource(R.drawable.roundedbutton);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    /***
	 * Deletes temp photo parts that make up the final image
	 */
	private void deletePartPhotoFiles() {
		for (int i = 0; i < photoParts.size(); i++) {
			File thisFile = new File(photoParts.get(i));
			if (thisFile.exists())
				thisFile.delete();
		}
		photoParts.clear();
	}

    /***
     * capture button shows numbers and a plus sign as images from the resources.
     * This method is called to set a new background image for the capture button.
     * @param resourceId for the image file
     */
    private void changeCaptureButtonResource(final int resourceId) {
        runOnUiThread(new Runnable() {
            public void run() {
                captureButton.setImageResource(resourceId);
            }
        });
    }

	private void changePhotoCountFontColor(final int index, final int color) {

		runOnUiThread(new Runnable() {
			public void run() {
				loTransport.setNumberColor(index, color);
			}
		});
	}
	
	/***
	 * Creates a file name for the photos taken.
	 * for part photos, tmp names creates, because they will be deleted at the end of the process.
	 * @param forPart
	 * @return the file instance created
	 */
	private File getFileForImage(boolean forPart) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		File output = new File(home_directory_string + (forPart ? "/TMP_" : "/IMG_") + timeStamp + ".JPG");
		return output;
	}

    class CustomUncaughtExceptionHandler implements UncaughtExceptionHandler {
		public void uncaughtException(Thread thread, Throwable ex) {
			String message = ex.getMessage();
			System.out.println(message);
		}
	}

	/* ****************** END SORT OF UTILITY FUNCTIONS *************** */

	
	/* ****************** BEGIN METHODS FOR DIALOGS ************************ */
	private void showCompletionFragment() {
		DialogFragment newFragment = CompletionDialogFragment.newInstance();
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	public void positiveCompletionCallback() {
		showPicture(finalImagePath);
	}

    /***
     * interface implementation method for CompletionDialogFragment
     * This method is called immediately from the fragment which acts as a wait animation
     * while the final image is prepared by the activity
     */
	public void onPhotoCreationDialogCreated() {
		prepareFinalImage();
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

	private void showPhotoCreationDialog() {

		FragmentManager fm = getSupportFragmentManager();
		photoCreationFragment = PhotoCreationDialogFragment.newInstance();
		photoCreationFragment.show(fm, "photo create");

	}

	private void hidePhotoCreationDialog(final boolean isSuccess) {
		photoCreationFragment.dismissAllowingStateLoss();
		if (isSuccess)
			showCompletionFragment();
	}
	
	/***
	 * The intent to view images
	 * @param file
	 */
	private void showPicture(String file) {
		String selectedFile = file.replace("/mnt/", "/");
		Intent showPicIntent = new Intent(this, ViewImageActivity.class);
        showPicIntent.setDataAndType(Uri.parse("file://" + selectedFile), "image/*");
        showPicIntent.setAction("com.ahmetkizilay.image.photostrips.ViewImageActivity");

		startActivity(showPicIntent);
	}
	
	/* ****************** END METHODS FOR DIALOGS ************************ */

	/* ********* BEGIN METHODS RELATED TO THE ACTION BAR ********************** */

	private void toggleActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (getActionBar().isShowing()) {
				getActionBar().hide();
			} else {
				getActionBar().show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
	}
	
	/********** END METHODS RELATED TO THE ACTION BAR ********************** */
	

	/********** BEGIN ANIMATION DEFINITIONS ********************** */
	
	public static Animation outToLeftAnimation() {
		Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(400);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	public static Animation inFromRightAnimation() {

		Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(400);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	/********** BEGIN ANIMATION DEFINITIONS ********************** */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    public void onShowPress(MotionEvent motionEvent) {

    }

    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    /**
     * Vertical motion is used to zoom between values. startSmoothZoom is used if supported by the
     * device.
     * @param motionEvent
     * @param motionEvent2
     * @param v
     * @param v2
     * @return
     */
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        if(Math.abs(v) > Math.abs(v2)) {
            return true;
        }

        Parameters params = mCamera.getParameters();
        if(params.isSmoothZoomSupported()) {
            if(v2 > 0) {
                mCamera.startSmoothZoom(Math.min(params.getMaxZoom(), params.getZoom() + 1));
            }
            else {
                mCamera.startSmoothZoom(Math.min(params.getMaxZoom(), params.getZoom() - 1));
            }
        }
        else {
            if(v2 > 0) {
                params.setZoom(Math.min(params.getMaxZoom(), params.getZoom() + 1));
            }
            else {
                params.setZoom(Math.max(0, params.getZoom() - 1));
            }
            mCamera.setParameters(params);
        }

        return false;
    }

    public void onLongPress(MotionEvent motionEvent) {
    }

    /**
     * Horizontal fling is monitored to switch color effects on the image.
     *
     * @param event1
     * @param event2
     * @param xVel
     * @param yVel
     * @return
     */
    public boolean onFling(MotionEvent event1, MotionEvent event2, float xVel, float yVel) {

        float maxFlingVelocity    = ViewConfiguration.get(this).getScaledMaximumFlingVelocity();
        float velocityPercentX    = Math.abs(xVel) / maxFlingVelocity;
        float MIN_VEL = 0.2f;

        if(Math.abs(yVel) > Math.abs(xVel)) {
            return true;
        }

        if(velocityPercentX < MIN_VEL) {
            return false;
        }

        if(numColorEffects > 0) {
            if(xVel > 0) {
                currentColorEffectIndex = (currentColorEffectIndex + numColorEffects - 1) % numColorEffects;
            }
            else {
                currentColorEffectIndex = (currentColorEffectIndex + 1) % numColorEffects;
            }
            Parameters params = mCamera.getParameters();
            params.setColorEffect(mColorEffects.get(currentColorEffectIndex));
            mCamera.setParameters(params);
        }

        return false;
    }
}