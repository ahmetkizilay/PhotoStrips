package com.ahmetkizilay.image.photostrips.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import com.ahmetkizilay.image.photostrips.PhotoCreator;
import com.ahmetkizilay.image.photostrips.R;
import com.ahmetkizilay.image.photostrips.ViewImageActivity;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ahmetkizilay on 12.07.2014.
 */
public class GalleryItemAdapter extends BaseAdapter {
    private String mGalleryDirectory;
    private String[] mPhotos;
    private Activity context;

    private int mDefaultThumbSize;

    public GalleryItemAdapter(Activity context, String galleryDirectory) {
        this.context = context;
        this.mGalleryDirectory = galleryDirectory;
        this.mPhotos = getFiles();

        int[] screenDimensions = new int[2];
        getRealSize(screenDimensions);

        int disp_width = screenDimensions[0];
        int disp_height = screenDimensions[1];

        mDefaultThumbSize = Math.min(disp_height, disp_width) + 100;
    }

    public void removeItem(String photo) {
        String[] newPhotos = new String[this.mPhotos.length - 1];

        for(int i = 0, j = 0; i < this.mPhotos.length; i += 1) {
            if(!this.mPhotos[i].equals(photo)) {
                newPhotos[j++] = this.mPhotos[i];
            }
        }

        this.mPhotos = newPhotos;
    }

    public int getCount() {
        return this.mPhotos.length;
    }

    public Object getItem(int i) {
        return this.mPhotos[i];
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(final int position, View view, ViewGroup viewGroup) {

        ViewHolder holder;

        if(view == null) {
            LayoutInflater inflater = this.context.getLayoutInflater();
            view = inflater.inflate(R.layout.port_gallery_item, viewGroup, false);

            holder = new ViewHolder();

            holder.img = (ImageView) view.findViewById(R.id.imgPhoto);

            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        final String photo = this.mPhotos[position];

        Bitmap thumbBM = PhotoCreator.getThumbnailBM(new File(photo), mDefaultThumbSize);
        holder.img.setImageBitmap(thumbBM);
        holder.img.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String selectedFile = photo;

                Intent showPicIntent = new Intent(GalleryItemAdapter.this.context, ViewImageActivity.class);
                showPicIntent.setDataAndType(Uri.parse(selectedFile), "image*//*");
                showPicIntent.setAction("com.ahmetkizilay.image.photostrips.ViewImageActivity");
                GalleryItemAdapter.this.context.startActivityForResult(showPicIntent, 0);
            }
        });

        final HorizontalScrollView hScrollView = (HorizontalScrollView) view;
        hScrollView.postDelayed(new Runnable() {
            public void run() {
                hScrollView.scrollBy(position % 2 == 0 ? -50 : 50, 0);
                hScrollView.fullScroll(position % 2 == 0 ? CustomHorizontalScrollView.FOCUS_RIGHT : CustomHorizontalScrollView.FOCUS_LEFT);
            }
        }, 10);

        return view;
    }

    private static class ViewHolder {
        private ImageView img;
    }

    private String[] getFiles() {
        File[] photos = new File(this.mGalleryDirectory).listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().startsWith("IMG_");
            }
        });

        if (photos == null || photos.length < 1) {
            return new String[0];
        }

        Arrays.sort(photos, new Comparator<File>() {
            public int compare(File o1, File o2) {

                if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                    return -1;
                } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                    return 1;
                } else {
                    return 0;
                }
            }

        });

        String[] photoPaths = new String[photos.length];
        for(int i = 0; i < photos.length; i += 1) {
            photoPaths[i] = photos[i].getAbsolutePath();
        }

        return photoPaths;
    }

    private void getRealSize(int[] screenDimensions) {

        DisplayMetrics dispMetrics = new DisplayMetrics();
        Display display = context.getWindowManager().getDefaultDisplay();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            display.getRealMetrics(dispMetrics);
            screenDimensions[0] = dispMetrics.widthPixels;
            screenDimensions[1] = dispMetrics.heightPixels;
        }
        else {
            // using reflection here
            try {
                Method mGetRawWidth = Display.class.getMethod("getRawWidth");
                Method mGetRawHeight = Display.class.getMethod("getRawHeight");

                screenDimensions[0] = (Integer) mGetRawWidth.invoke(display);
                screenDimensions[1] = (Integer) mGetRawHeight.invoke(display);
            }
            catch(Exception exp) {
                display.getMetrics(dispMetrics);

                screenDimensions[0] = dispMetrics.widthPixels;
                screenDimensions[1] = dispMetrics.heightPixels;
            }
        }
    }
}
