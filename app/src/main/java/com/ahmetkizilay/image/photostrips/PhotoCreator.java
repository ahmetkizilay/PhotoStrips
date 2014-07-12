package com.ahmetkizilay.image.photostrips;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class PhotoCreator {
		
	public static String getThumbnailLocation(File photo) {
		return photo.getParent() + File.separator + ".thumb" + File.separator + "." + photo.getName();
	}
	public static boolean createThumbnail(File photo, int targetDisplayWidth) {
		String thumbLocation = getThumbnailLocation(photo);	
		return createThumbnail(BitmapFactory.decodeFile(photo.getAbsolutePath()), targetDisplayWidth, thumbLocation);
	}
	
	public static boolean createThumbnail(Bitmap photoBM, int targetDisplayWidth, String thumbLocation) {
		try {
			boolean isPortrait = photoBM.getWidth() < photoBM
					.getHeight();
			float scaleFactor = (float) (targetDisplayWidth)
					/ (float) (!isPortrait ? photoBM.getWidth()
							: photoBM.getHeight());

			if(isPortrait) {
				if(((float) photoBM.getWidth() * scaleFactor) < 150f) {
					scaleFactor = 150f / (float) photoBM.getWidth();
				}				
			}
			else {
				if(((float) photoBM.getHeight() * scaleFactor) < 150f) {
					scaleFactor = 150f / (float) photoBM.getHeight();
				}	
			}
			
			Matrix thumbnailMatrix = new Matrix();
			thumbnailMatrix.postScale(scaleFactor, scaleFactor);
			if (isPortrait)
				thumbnailMatrix.postRotate(-90f);

			Bitmap thumbnailPicture = Bitmap.createBitmap(photoBM, 0, 0,
					photoBM.getWidth(), photoBM.getHeight(),
					thumbnailMatrix, true);

			thumbnailPicture.compress(CompressFormat.JPEG, 100,
					new FileOutputStream(new File(thumbLocation)));
			
			return true;
		}
		catch(Exception exp) {
			exp.printStackTrace();
			return false;
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
	
	private static boolean prepareFinalImagePortrait(List<String> partFileNames, boolean isCameraFrontFacing, String newFilePath) {
		try {

			Bitmap finalPicture = null;

			int[] pixelBuffer = new int[0];
			int[] horizontalFrameBuffer = new int[0];
			int[] verticalFrameBuffer = new int[0];

			for (int i = 0; i < partFileNames.size(); i++) {
				Bitmap thisPicture = BitmapFactory.decodeFile(partFileNames.get(i));
				int x = (int) ((float) (thisPicture.getWidth() - thisPicture.getHeight()) * 0.5);
				int y = 0;
				int w = thisPicture.getHeight();
				int h = thisPicture.getHeight();

				float scaleRatio = 480.0f / (float) thisPicture.getHeight();
				Matrix rotateMatrix = new Matrix();
				rotateMatrix.postScale(scaleRatio, (isCameraFrontFacing ? -1f : 1f) * scaleRatio);
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
				Bitmap thisPicture = BitmapFactory.decodeFile(partFileNames.get(i));
				int x = (int) ((float) (thisPicture.getWidth() - thisPicture.getHeight()) * 0.5);
				int y = 0;
				int w = thisPicture.getHeight();
				int h = thisPicture.getHeight();

				float scaleRatio = 480.0f / (float) thisPicture.getHeight();
				Matrix rotateMatrix = new Matrix();
				rotateMatrix.postScale(scaleRatio, (isCameraFrontFacing ? -1f : 1f) * scaleRatio);
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
