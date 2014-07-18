package com.ahmetkizilay.image.photostrips.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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

    private int disp_height;
    private int disp_width;

    public GalleryItemAdapter(Activity context, String galleryDirectory) {
        this.context = context;
        this.mGalleryDirectory = galleryDirectory;
        this.mPhotos = getFiles();

        DisplayMetrics dispMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dispMetrics);

        disp_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dispMetrics.heightPixels, dispMetrics);
        disp_width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dispMetrics.widthPixels, dispMetrics);
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

    public View getView(int position, View view, ViewGroup viewGroup) {

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
        File photoFile = new File(photo);

        String thumbLocation = PhotoCreator.getThumbnailLocation(photoFile);
        File thumbFile = new File(thumbLocation);
        if (!thumbFile.exists()) {
            if (PhotoCreator.createThumbnail(photoFile, disp_width + 100)) {
                thumbFile = new File(thumbLocation);
            } else {
                // TODO take care of this
            }
        }

        Bitmap thumbBM = BitmapFactory.decodeFile(thumbLocation);
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
                hScrollView.smoothScrollBy(20 + (int) Math.floor(Math.random() * 200.0f), 0);
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
}
