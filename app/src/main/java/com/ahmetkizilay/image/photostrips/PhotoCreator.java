package com.ahmetkizilay.image.photostrips;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.util.Log;

public class PhotoCreator {
		
	public static String getThumbnailLocation(File photo) {
		return photo.getParent() + File.separator + ".thumb" + File.separator + "." + photo.getName();
	}

    public static Bitmap getThumbnailBM(File photo, int targetDisplayWidth) {
        String thumbnailLocation = getThumbnailLocation(photo);
        File thumbnailFile = new File(thumbnailLocation);
        if(thumbnailFile.exists()) {
            Bitmap bmThumbnail = BitmapFactory.decodeFile(thumbnailLocation);
            if(bmThumbnail.getWidth() != targetDisplayWidth) {
                return createThumbnail(photo, targetDisplayWidth);
            }
            return bmThumbnail;
        }
        else {
            return createThumbnail(photo, targetDisplayWidth);
        }
    }

	public static Bitmap createThumbnail(File photo, int targetDisplayWidth) {
		String thumbLocation = getThumbnailLocation(photo);
		return createThumbnail(BitmapFactory.decodeFile(photo.getAbsolutePath()), targetDisplayWidth, thumbLocation);
	}
	
	public static Bitmap createThumbnail(Bitmap photoBM, int targetDisplayWidth, String thumbLocation) {
		try {
			boolean isPortrait = photoBM.getWidth() < photoBM
					.getHeight();
			float scaleFactor = (float) (targetDisplayWidth)
					/ (float) (!isPortrait ? photoBM.getWidth()
							: photoBM.getHeight());

			Matrix thumbnailMatrix = new Matrix();
			thumbnailMatrix.postScale(scaleFactor, scaleFactor);
			if (isPortrait)
				thumbnailMatrix.postRotate(-90f);

			Bitmap thumbnailPicture = Bitmap.createBitmap(photoBM, 0, 0,
					photoBM.getWidth(), photoBM.getHeight(),
					thumbnailMatrix, true);

			thumbnailPicture.compress(CompressFormat.JPEG, 100,
					new FileOutputStream(new File(thumbLocation)));
			
			return thumbnailPicture;
		}
		catch(Exception exp) {
			exp.printStackTrace();
			return null;
		}
	}
	
	public static boolean prepareFinalImage(List<String> partFileNames, boolean isCameraFrontFacing, boolean isPortrait, String newFilePath) {
		if(isPortrait) {
			return prepareFinalImagePortrait(partFileNames, isCameraFrontFacing, newFilePath);
		}
		else {
			return prepareFinalImageLandscape(partFileNames, isCameraFrontFacing, newFilePath);
		}
	}

    /***
     * checking something called exif orientation to hopefully fix the problem of pictures
     * appearing upside down on certain devices.
     * the code is taken from an StackOverflow answer: http://stackoverflow.com/a/13176590/210391
     * @param fileName
     * @return
     */
    private static int getExifOrientation(String fileName) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(fileName);
        }
        catch(IOException exp) {
        }

        if(exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if(orientation != -1) {
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        return 90;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        return 180;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        return 270;
                }
            }
        }

        return degree;
    }

	private static boolean prepareFinalImagePortrait(List<String> partFileNames, boolean isCameraFrontFacing, String newFilePath) {
		try {

			Bitmap finalPicture = null;

			int[] pixelBuffer = new int[0];
			int[] horizontalFrameBuffer = new int[0];
			int[] verticalFrameBuffer = new int[0];

			for (int i = 0; i < partFileNames.size(); i++) {
                int exifOrientation = getExifOrientation(partFileNames.get(i));
				Bitmap thisPicture = BitmapFactory.decodeFile(partFileNames.get(i));

				int x = (int) ((float) (thisPicture.getWidth() - thisPicture.getHeight()) * 0.5);
				int y = 0;
				int w = thisPicture.getHeight();
				int h = thisPicture.getHeight();

				float scaleRatio = 480.0f / (float) thisPicture.getHeight();
				Matrix rotateMatrix = new Matrix();
				rotateMatrix.postScale(scaleRatio, (isCameraFrontFacing ? -1f : 1f) * scaleRatio);
                if(exifOrientation == 180) rotateMatrix.preRotate(180);
				rotateMatrix.postRotate(isCameraFrontFacing ? -90f : 90f);

				thisPicture = Bitmap.createBitmap(thisPicture, x, y, w, h, rotateMatrix, true);
				if (i == 0) {
					finalPicture = Bitmap.createBitmap(thisPicture.getWidth(), thisPicture.getHeight() * partFileNames.size(), thisPicture.getConfig());

					pixelBuffer = new int[thisPicture.getWidth() * thisPicture.getHeight()];

					horizontalFrameBuffer = new int[thisPicture.getWidth() * 10];
					verticalFrameBuffer = new int[(thisPicture.getHeight() - 20) * 10];

					Arrays.fill(horizontalFrameBuffer, Color.WHITE);
					Arrays.fill(verticalFrameBuffer, Color.WHITE);
				}

				thisPicture.getPixels(pixelBuffer, 0, thisPicture.getWidth(), 0, 0, thisPicture.getWidth(), thisPicture.getHeight());
				finalPicture.setPixels(pixelBuffer, 0, thisPicture.getWidth(), 0, thisPicture.getHeight() * i, thisPicture.getWidth(), thisPicture.getHeight());

				finalPicture.setPixels(horizontalFrameBuffer, 0, thisPicture.getWidth(), 0, thisPicture.getHeight() * i, thisPicture.getWidth(), 10);
				finalPicture.setPixels(horizontalFrameBuffer, 0, thisPicture.getWidth(), 0, (thisPicture.getHeight() * (i + 1)) - 10, thisPicture.getWidth(), 10);

				finalPicture.setPixels(verticalFrameBuffer, 0, 10, 0, (thisPicture.getHeight() * i) + 10, 10, (thisPicture.getHeight() - 20));
				finalPicture.setPixels(verticalFrameBuffer, 0, 10, thisPicture.getWidth() - 10, (thisPicture.getHeight() * i) + 10, 10, (thisPicture.getHeight() - 20));
			}

			
			finalPicture.compress(CompressFormat.JPEG, 100, new FileOutputStream(newFilePath));

			return true;

		} catch (Exception exp) {
			exp.printStackTrace();			
			return false;
		}
	}
	
	private static boolean prepareFinalImageLandscape(List<String> partFileNames, boolean isCameraFrontFacing, String newFilePath) {
		try {

			Bitmap finalPicture = null;

			int[] pixelBuffer = new int[0];
			int[] horizontalFrameBuffer = new int[0];
			int[] verticalFrameBuffer = new int[0];

			for (int i = 0; i < partFileNames.size(); i++) {
                int exifOrientation = getExifOrientation(partFileNames.get(i));
				Bitmap thisPicture = BitmapFactory.decodeFile(partFileNames.get(i));
				int x = (int) ((float) (thisPicture.getWidth() - thisPicture.getHeight()) * 0.5);
				int y = 0;
				int w = thisPicture.getHeight();
				int h = thisPicture.getHeight();

				float scaleRatio = 480.0f / (float) thisPicture.getHeight();
				Matrix rotateMatrix = new Matrix();
				rotateMatrix.postScale(scaleRatio, (isCameraFrontFacing ? -1f : 1f) * scaleRatio);
                if(exifOrientation == 180) rotateMatrix.preRotate(180);
				rotateMatrix.postRotate(isCameraFrontFacing ? -90f : 90f);

				thisPicture = Bitmap.createBitmap(thisPicture, x, y, w, h, rotateMatrix, true);
				if (i == 0) {
					finalPicture = Bitmap.createBitmap(thisPicture.getWidth() * partFileNames.size(), thisPicture.getHeight(), thisPicture.getConfig());

					pixelBuffer = new int[thisPicture.getWidth() * thisPicture.getHeight()];

					horizontalFrameBuffer = new int[thisPicture.getWidth() * 10];
					verticalFrameBuffer = new int[(thisPicture.getHeight() - 20) * 10];

					Arrays.fill(horizontalFrameBuffer, Color.WHITE);
					Arrays.fill(verticalFrameBuffer, Color.WHITE);
				}

				thisPicture.getPixels(pixelBuffer, 0, thisPicture.getWidth(), 0, 0, thisPicture.getWidth(), thisPicture.getHeight());
				finalPicture.setPixels(pixelBuffer, 0, thisPicture.getWidth(), thisPicture.getWidth() * i, 0, thisPicture.getWidth(), thisPicture.getHeight());

				finalPicture.setPixels(horizontalFrameBuffer, 0, thisPicture.getWidth(), thisPicture.getWidth() * i, 0, thisPicture.getWidth(), 10);
				finalPicture.setPixels(horizontalFrameBuffer, 0, thisPicture.getWidth(), thisPicture.getWidth() * i, thisPicture.getHeight() - 10, thisPicture.getWidth(), 10);

				finalPicture.setPixels(verticalFrameBuffer, 0, 10, thisPicture.getWidth() * i, 10, 10, thisPicture.getHeight() - 20);
				finalPicture.setPixels(verticalFrameBuffer, 0, 10, (thisPicture.getWidth() * (i + 1)) - 10, 10, 10, thisPicture.getHeight() - 20);

			}

			finalPicture.compress(CompressFormat.JPEG, 100, new FileOutputStream(newFilePath));

			return true;

		} catch (Exception exp) {
			Log.d("", exp.getMessage());
			return false;
		}
	}

}
