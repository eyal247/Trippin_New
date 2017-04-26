package com.trippin.androidtrippin.trippin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.SignUpUtils;

import org.json.JSONException;

import java.io.IOException;


public class SignUpProfilePictureActivity extends MyActionBarActivity
{
    private ImageView userImageIV;
    private Bitmap userImageBitmap;
    private ImageButton selectIB;
    private Button finishButton;
    private TextView skipTV;
    private final boolean PROFILE_PICTURE = true;
    private String userProfilePictureStr;
    private ImageButton cameraIB;
    private RelativeLayout activityMainLayout;

    private final Activity thisActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_profile_picture);
        getUIComponents();
        setUIComponentsListeners();
    }

//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//        super.startCheckInternetConnection();
//    }
//
//    @Override
//    protected void onPause()
//    {
//        super.onPause();
//        super.stopCheckInternetConnection();
//    }

    private void setUIComponentsListeners()
    {
        selectIB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                handleOpenCameraRoll();
            }
        });

        cameraIB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                handleOpenCamera(v);
            }
        });

        skipTV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (AppUtils.isNetworkAvailable(SignUpProfilePictureActivity.this))
                    handleSkip();
                else
                    AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, thisActivity, activityMainLayout);
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (AppUtils.isNetworkAvailable(SignUpProfilePictureActivity.this))
                    handleFinish();
                else
                    AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, thisActivity, activityMainLayout);
            }
        });
    }

    private void handleOpenCamera(View v)
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, AppConstants.CAMERA_REQUEST_CODE);
    }

    private void getUIComponents()
    {
        userImageIV = (ImageView) findViewById(R.id.signUpImageView);
        userImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_user_logo);
        userImageIV.setImageBitmap(userImageBitmap);
        selectIB = (ImageButton) findViewById(R.id.selectButton);
        skipTV = (TextView) findViewById(R.id.skipText);
        finishButton = (Button) findViewById(R.id.finishButton);
        cameraIB = (ImageButton)findViewById(R.id.cameraButton);
        activityMainLayout = (RelativeLayout)findViewById(R.id.sign_up_profile_pic_main_layout);
    }

    private void handleSkip()
    {
        try {
            SignUpUtils.sendUserDetails(this, !PROFILE_PICTURE, userProfilePictureStr, false, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleFinish()
    {
        try {
            SignUpUtils.sendUserDetails(this, PROFILE_PICTURE, userProfilePictureStr, false, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private void sendUserDetails(View v, boolean addProfilePicture)
//    {
//        JSONObject newUserJSON;
//        String url = AppConstants.SERVER_URL + AppConstants.COMPLETE_SIGN_UP_URL;
//        //String url = "http://10.0.0.5:3000/completeSignup";
//
//        user.loadDetailsFromIntent(getIntent());
//
//        try
//        {
//            newUserJSON = createUserJSON(user, addProfilePicture);
//            JsonObjectRequest jsObjRequest = new JsonObjectRequest
//                    (Request.Method.POST, url, newUserJSON, new Response.Listener<JSONObject>() {
//
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            handleSignUpResponse(response, user.getUsername());
//                        }
//                    }, new Response.ErrorListener() {
//
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            System.out.println("error response on sendUserDetails()");
//                        }
//                    });
//
//            MainActivity.addRequestToQueue(jsObjRequest);
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void handleSignUpResponse(JSONObject response, String username)
//    {
//        try
//        {
//            switch(response.getString("result"))
//            {
//                case AppConstants.RESPONSE_SUCCESS:
//                    SaveSharedPreference.setUserName(getApplicationContext(), username);
//                    switchToHomeActivity();
//                    break;
//                case AppConstants.RESPONSE_FAILURE:
//                    Toast.makeText(this, "Sign Up failed. Server error.", Toast.LENGTH_LONG).show();
//                    break;
//                default:
//                    System.out.println("error on handleSignUpResponse();");
//                    break;
//            }
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private JSONObject createUserJSON(User user, Boolean addProfilePicture) throws JSONException
//    {
//        JSONObject newUserJSON = new JSONObject();
//        String encodedProfilePicValue = "";
//
//        if(user.getEncodedProfilePic() != null)
//            encodedProfilePicValue = user.getEncodedProfilePic();
//
//        newUserJSON.put("username", user.getUsername());
//        newUserJSON.put("password", user.getPassword());
//        newUserJSON.put("fname", user.getFname());
//        newUserJSON.put("lname", user.getLname());
//        newUserJSON.put("encodedProfilePic", encodedProfilePicValue);
//
//        return newUserJSON;
//    }

//    private void switchToHomeActivity()
//    {
//        Intent intent = getIntent();
//        intent.putExtra("username", user.getUsername());
//        intent.setClass(SignUpProfilePictureActivity.this, HomeActivity.class);
//        startActivity(intent);
//    }

    private void handleOpenCameraRoll()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"), AppConstants.CAMERA_ROLL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode ==  RESULT_OK){
            if (requestCode == AppConstants.CAMERA_ROLL_REQUEST_CODE){
                try {
                    userImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
//                  userImageIV.setImageURI(data.getData());
//                  userImageIV.buildDrawingCache();
                    userImageBitmap = AppUtils.resizeBitmap(userImageBitmap, AppConstants.IMAGE_MAX_SIZE);
                    userImageIV.setImageBitmap(userImageBitmap);
                    //Bitmap bitmap = userImageIV.getDrawingCache(); // getting the bitmap of the image the user selected.
                    String encodedProfilePic = AppUtils.bitMapToString(userImageBitmap);
                    userProfilePictureStr = encodedProfilePic;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(requestCode == AppConstants.CAMERA_REQUEST_CODE){
                userImageBitmap = (Bitmap) data.getExtras().get("data");
                userImageBitmap = AppUtils.resizeBitmap(userImageBitmap, AppConstants.IMAGE_MAX_SIZE);
                userImageIV.setImageBitmap(userImageBitmap);
                String encodedProfilePic = AppUtils.bitMapToString(userImageBitmap);
                userProfilePictureStr = encodedProfilePic;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up_profile_picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }


}
