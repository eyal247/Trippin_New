package com.trippin.androidtrippin.trippin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.trippin.AppController;
import com.trippin.androidtrippin.trippin.MyActionBarActivity;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.adapters.FullScreenImageAdapter;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.SaveSharedPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;


public class FullScreenImageActivity extends MyActionBarActivity
{
    private ImageView imageView;
    private String imageID;
    private int firstImagePosition;
    private ArrayList<String> allImagesStrings;
    private ArrayList<String> allImagesServerIds;
    private boolean hideAll = true;
    private boolean madeCoverPhoto = false;
    private MenuItem optionsMenuItem;
    private LinearLayout activityMainLayout;
    private FullScreenImageAdapter imagesAdapter;
    private ViewPager imagesViewPager;

    private final Activity thisActivity = this;
    private int currentPagerPosition;
    private int deletedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        imageID = getIntent().getExtras().getString("imageID");
        firstImagePosition = getIntent().getExtras().getInt("position");
        currentPagerPosition = firstImagePosition;
        System.out.println("FIRST POSITION: " + firstImagePosition);
        allImagesStrings = AppController.getInstance().getCurrPlaceImagesStrings();
        allImagesServerIds = AppController.getInstance().getCurrPlaceImagesServerIds();

        getUIComponents();
        invalidateOptionsMenu();
        setAdapter();
        setViewPagerListener();
        //setUIComponentsListeners();
        //loadImageToView();
    }

    private void setViewPagerListener()
    {
        imagesViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {

            @Override
            public void onPageSelected(int pos)
            {
                currentPagerPosition = pos;
                System.out.println("PAGER POSITION: " + pos);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2)
            {

            }

            @Override
            public void onPageScrollStateChanged(int arg0)
            {

            }
        });
    }

    private void setAdapter()
    {
        imagesAdapter = new FullScreenImageAdapter(FullScreenImageActivity.this,
                allImagesStrings);

        imagesViewPager.setAdapter(imagesAdapter);

        // displaying selected image first
        imagesViewPager.setCurrentItem(firstImagePosition);
    }

    private void showOrHideButtons()
    {
        if(getIntent().getExtras().getString("caller").equals("OtherUserTripActivity"))
        {
            optionsMenuItem.setVisible(false);
        }
    }

    private void loadImageToView()
    {
        Bitmap b = null;
        if(getIntent().hasExtra("byteArray"))
            b = BitmapFactory.decodeByteArray(getIntent().getByteArrayExtra("byteArray"), 0, getIntent().getByteArrayExtra("byteArray").length);

        imageView.setImageBitmap(b);
        PhotoViewAttacher mAttacher = new PhotoViewAttacher(imageView);
        mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener()
        {
            @Override
            public void onPhotoTap(View view, float v, float v2)
            {
                onImageClick();
            }
        });
    }

//    private void setUIComponentsListeners()
//    {
//        deleteButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                if(AppUtils.isNetworkAvailable(FullScreenImageActivity.this))
//                    onDeleteClick();
//                else
//                    AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, thisActivity, activityMainLayout);
//            }
//        });
//
//        makeTripPhotoButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                if(AppUtils.isNetworkAvailable(FullScreenImageActivity.this))
//                    onMakeTripPhotoClick();
//                else
//                    AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, thisActivity, activityMainLayout);
//            }
//        });
//    }



    private void onImageClick()
    {
//        if (hideAll)
//        {
//            getSupportActionBar().hide();
////            deleteButton.setVisibility(View.GONE);
////            makeTripPhotoButton.setVisibility(View.GONE);
//            hideAll = false;
//        }
//        else
//        {
//            getSupportActionBar().show();
////            deleteButton.setVisibility(View.VISIBLE);
////            makeTripPhotoButton.setVisibility(View.VISIBLE);
//            hideAll = true;
//        }
    }

    private void onDeleteClick()
    {
        imageID = allImagesServerIds.get(currentPagerPosition);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE: //Yes button clicked
                    deletedPosition = currentPagerPosition;
                    deletePhoto();
                    break;

                case DialogInterface.BUTTON_NEGATIVE: //No button clicked
                    // Nothin to do
                    break;
            }
        }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete photo?").setPositiveButton("Delete", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).show();
    }

    private void deletePhoto()
    {
        String placeServerID = getIntent().getExtras().getString("placeServerID");

        JSONObject deleteImageJSON;
        String url = AppConstants.SERVER_URL + AppConstants.DELETE_USER_PLACE_IMAGE;
        //String url = "http://192.168.207.205:3000/deleteUserPlaceImage";

        try
        {
            deleteImageJSON = new JSONObject();
            deleteImageJSON.put("parentTripPlaceID", placeServerID);
            deleteImageJSON.put("imageID", imageID);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, deleteImageJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    handleDeleteImageResponse(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on deletePhoto()");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteImageResponse(JSONObject response)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    onDeleteFinished();
                    break;
                case AppConstants.RESPONSE_FAILURE:
//                    Toast.makeText(this, "Couldn't delete image from trip place. Server error.", Toast.LENGTH_LONG).show();
                    AppUtils.showSnackBarMsg("Couldn't delete image from trip place. Server error.", this, activityMainLayout);
                    break;
                default:
                    System.out.println("error on handleDeleteImageResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onDeleteFinished()
    {
        removeImageFromLists();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("imageID", imageID);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void removeImageFromLists()
    {
        AppController.getInstance().getCurrPlaceImagesStrings().remove(deletedPosition);
        AppController.getInstance().getCurrPlaceImagesServerIds().remove(deletedPosition);
//        allImagesServerIds.remove(deletedPosition);
//        allImagesStrings.remove(deletedPosition);
    }

    private void onMakeTripPhotoClick()
    {
        openMainPhotoDialog();
    }

    private void openMainPhotoDialog()
    {

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE: //Yes button clicked
                            handleMakeCoverPhotoYes();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE: //No button clicked
                            // Nothin to do
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Make Trip Cover Photo?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).show();
    }

    private void handleMakeCoverPhotoYes()
    {
        imageID = allImagesServerIds.get(currentPagerPosition);
        String mainPhotoStr = allImagesStrings.get(currentPagerPosition);
        sendMainPhotoToServer(mainPhotoStr);
    }

    private void sendMainPhotoToServer(final String mainPhotoStr)
    {
        JSONObject mainPhotoJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.UPDATE_TRIP_MAIN_PHOTO;
        //String url = "http://192.168.207.205:3000/updateTripMainPhoto";

        try {
            mainPhotoJSON.put("imageID", imageID);
            mainPhotoJSON.put("tripID", SaveSharedPreference.getTripId(getApplicationContext()));
            mainPhotoJSON.put("mainPhoto", mainPhotoStr);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, mainPhotoJSON, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleMainPhotoResponse(response, mainPhotoStr);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("error response on sendMainPhotoToServer()");
                        }
                    });
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS*3,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MainActivity.addRequestToQueue(jsObjRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleMainPhotoResponse(JSONObject response, String mainPhotoStr)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    //Bitmap tripMainPhoto = AppUtils.stringToBitMap(mainPhotoStr);
                    //currTrip.setTripMainImage(tripMainPhoto);
                    saveNewPhotoInCache(mainPhotoStr);
                    AppController.getInstance().getCurrTripOnMap().setTripCoverPhotoStr(mainPhotoStr);
                    madeCoverPhoto = true;
//                    Toast.makeText(this, "Trip's cover photo updated!", Toast.LENGTH_LONG).show();
                    AppUtils.showSnackBarMsg("Trip's cover photo updated!", this, activityMainLayout);
                    break;
                case AppConstants.RESPONSE_FAILURE:
//                    Toast.makeText(this, "Couldn't set as trip cover photo. Server error.", Toast.LENGTH_LONG).show();
                    AppUtils.showSnackBarMsg("Couldn't set as trip cover photo. Server error.", this, activityMainLayout);
                    break;
                default:
                    System.out.println("error on handleMainPhotoResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveNewPhotoInCache(String mainPhotoStr)
    {
        StringBuilder key = new StringBuilder(SaveSharedPreference.getTripId(getApplicationContext()));
        AppController.getInstance().getLruBitmapCache().putBitmap(key.toString(),AppUtils.stringToBitMap(mainPhotoStr));
    }

    private void getUIComponents()
    {
        imageView = (ImageView) findViewById(R.id.expanded_image_IV);
        activityMainLayout = (LinearLayout)findViewById(R.id.image_activity_main_layout);
        imagesViewPager = (ViewPager) findViewById(R.id.images_view_pager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image, menu);
        optionsMenuItem = menu.findItem(R.id.image_activity_action_settings);
        showOrHideButtons();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.image_activity_action_settings) {
            onOpenImageOptions();
            return true;
        }

        if (id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onOpenImageOptions()
    {
        String[] imageOptions = {"Make Trip Cover Photo", "Delete Photo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.imageOptions)
                .setItems(imageOptions, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        executeSelectedOption(which);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void executeSelectedOption(int which)
    {
        switch(which){
            case 0:
                onMakeTripPhotoClick();
                break;
            case 1:
                onDeleteClick();
                break;
            default:
                System.out.println("default in switch case executeSelectedOption image activity");
        }
    }
}
