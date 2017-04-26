package com.trippin.androidtrippin.trippin;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.LruBitmapCache;
import com.trippin.androidtrippin.model.Trip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shaiyahli on 8/9/2015.
 */
public class AppController extends Application
{
    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;
    private static AppController mInstance;
    private GoogleApiClient mGoogleApiClient;
    private Trip currTripOnMap;
    private ImageLoader mImageLoader;
    private LruBitmapCache mLruBitmapCache;
    private File excpFile;
    private boolean showToast = true;
    private Runnable runnable;
    private Timer timer;
    private MyUncaughtExceptionHandler exceptionHandler;
    private ArrayList<String> currPlaceImagesStrings;
    private ArrayList<String> currPlaceImagesServerIds;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mInstance = this;
        excpFile = new File(getExternalFilesDir(null), "log.txt" );
        exceptionHandler = new MyUncaughtExceptionHandler();

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

        startCheckInternetConnection();
    }

    public ArrayList<String> getCurrPlaceImagesStrings()
    {
        return currPlaceImagesStrings;
    }

    public void setCurrPlaceImagesStrings(ArrayList<String> placeImagesStrings)
    {
        currPlaceImagesStrings = placeImagesStrings;
    }

    public void setCurrPlaceImagesServerIds(ArrayList<String> imagesIds)
    {
        currPlaceImagesServerIds = imagesIds;
    }

    public ArrayList<String> getCurrPlaceImagesServerIds()
    {
        return currPlaceImagesServerIds;
    }

    private class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        @Override
        public void uncaughtException(Thread thread, Throwable ex)
        {
            handleUncaughtException(thread, ex);
        }
    }

    private void handleUncaughtException(Thread thread, Throwable e)
    {
        timer.cancel();
        e.printStackTrace();

        writeExceptionToFile(e);
        System.exit(2);
//        startMainActivity();
    }

    private void writeExceptionToFile(Throwable e)
    {
        try {
//            FileOutputStream outputStream = new FileOutputStream(excpFile);
            PrintWriter printWriter = new PrintWriter(excpFile);
            e.printStackTrace(printWriter);
            printWriter.close();

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void startMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public GoogleApiClient getmGoogleApiClient()
    {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient)
    {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null)
        {
            if (mLruBitmapCache == null)
                mLruBitmapCache = new LruBitmapCache();
            mImageLoader = new ImageLoader(this.mRequestQueue, mLruBitmapCache);
        }

        return this.mImageLoader;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public LruBitmapCache getLruBitmapCache()
    {
        if (mLruBitmapCache == null)
            mLruBitmapCache = new LruBitmapCache();

        return mLruBitmapCache;
    }

    protected void startCheckInternetConnection()
    {
        if(runnable == null) {
            runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        checkInternetConnection();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                    }
                }
            };
        }
        runConnectionCheckInBackground();
    }

    private void runConnectionCheckInBackground()
    {
        int delay = 1000; // delay for 5 sec.
        int period = 3000; // repeat every 10 secs.

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask()
        {

            public void run()
            {
                Thread t = new Thread(runnable);
                t.start();
                t.setUncaughtExceptionHandler(exceptionHandler);
            }

        }, delay, period);
    }

    private void checkInternetConnection()
    {
        if(!AppUtils.isNetworkAvailable(this))
        {

//            runOnUiThread(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
//                    System.out.println("In checkInternetConnection");
//                    showToast = false;
//                }
//            });
        }else if(AppUtils.isNetworkAvailable(this)){
            showToast = true;
        }
    }

    protected void stopCheckInternetConnection(){
        if(timer != null)
            timer.cancel();
        showToast = true;
    }

    public Trip getCurrTripOnMap()
    {
        return currTripOnMap;
    }

    public void setCurrTripOnMap(Trip currTripOnMap)
    {
        this.currTripOnMap = currTripOnMap;
    }
}
