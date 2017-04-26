package com.trippin.androidtrippin.model;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by User on 13/08/2015.
 */
public class LruBitmapCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache
{
    public static int getDefaultLruCacheSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        return cacheSize;
    }

    public LruBitmapCache() {
        this(getDefaultLruCacheSize());
    }

    public LruBitmapCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight() / 1024;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        if(url != null && bitmap != null)
            put(url, bitmap);
    }
//
//    public void loadBitmap(int resId, ImageView imageView, String imageStr)
//    {
//        final String imageKey = String.valueOf(resId);
//
//        final Bitmap bitmap = get(imageKey);
//        if (bitmap != null)
//        {
//            imageView.setImageBitmap(bitmap);
//        }
//        else
//        {
//            imageView.setImageResource(R.drawable.grey_image);
//            BitmapWorkerTask task = new BitmapWorkerTask(imageView, imageStr);
//            task.execute(resId);
//        }
//    }
//
//    private class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap>
//    {
//        private ImageView mImageView;
//        private String mImageStr;
//
//        public BitmapWorkerTask(ImageView imageView, String imageStr)
//        {
//            mImageView = imageView;
//            mImageStr = imageStr;
//        }
//
//        // Decode image in background.
//        @Override
//        protected Bitmap doInBackground(Integer... params)
//        {
//            final Bitmap bitmap = AppUtils.stringToBitMap(mImageStr);
//            mImageView.setImageBitmap(bitmap);
//            putBitmap(String.valueOf(params[0]), bitmap);
//            return bitmap;
//        }
//    }
}
