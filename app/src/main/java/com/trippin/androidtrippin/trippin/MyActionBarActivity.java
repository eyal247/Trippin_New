package com.trippin.androidtrippin.trippin;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.trippin.androidtrippin.model.AppUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by EyalEngel on 30/07/15.
 */
public class MyActionBarActivity extends AppCompatActivity
{
    private ActionBar actionBar;
    private boolean showToast = true;
    private Runnable runnable;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setElevation(15);
    }

    protected void setActionBarTitle(String title)
    {
        getSupportActionBar().setTitle(title); //todo decide if uppercase
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
                    }
                }
            };
        }
        runConnectionCheckInBackground();
    }

    private void runConnectionCheckInBackground()
    {
        int delay = 1000; // delay for 5 sec.
        int period = 2000; // repeat every 10 secs.

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                new Thread(runnable).start();
            }

        }, delay, period);
    }

    private void checkInternetConnection(){
        if(!AppUtils.isNetworkAvailable(this)) {
            this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(MyActionBarActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    System.out.println("In checkInternetConnection");
                    showToast = false;
                }
            });
        }else if(AppUtils.isNetworkAvailable(this)){
            showToast = true;
        }
    }

    protected void stopCheckInternetConnection(){
        if(timer != null)
            timer.cancel();
        showToast = true;
    }
}
