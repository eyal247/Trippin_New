package com.trippin.androidtrippin.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.Profile;
import com.trippin.androidtrippin.trippin.GetNameActivity;
import com.trippin.androidtrippin.trippin.HomeActivity;
import com.trippin.androidtrippin.trippin.MainActivity;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.trippin.SignUpActivity;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by EyalEngel on 16/08/15.
 */
public class SignUpUtils
{
    private static Logger logger;
    private static final String TAG = "FacebookSignUpUtils";
    private static JSONObject newUserJSON = new JSONObject();
    private static User user = new User();
    //public static String seedValue = "ELBAKAERBNUMAI";
    public static void checkGoogleUserWithServer(final Context ctx, final String username, final String password, final boolean signUpWithGoogle, final Person currentPerson)
    {

        String url = AppConstants.SERVER_URL + AppConstants.CHECK_IF_USER_EXISTS_URL;
        //String url = "http://192.168.203.2:3000/checkIfUserExists";

        try {
            //String encryptedData = AESHelper.encrypt(seedValue, password);
            newUserJSON.put("username", username);
            newUserJSON.put("password", password);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, newUserJSON, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleCheckUserResponse(response, ctx, signUpWithGoogle, currentPerson);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            handleSignUpErrorResponse(ctx, signUpWithGoogle);
                            System.out.println("error response on checkGoogleUserWithServer()");
                        }
                    });
            MainActivity.addRequestToQueue(jsObjRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleSignUpErrorResponse(Context ctx, boolean signUpWithGoogle){
        if(signUpWithGoogle){
            ((MainActivity)ctx).findViewById(R.id.loading_panel_main_activity).setVisibility(View.GONE);
            ((MainActivity)ctx).findViewById(R.id.main_activity_content_rl).setVisibility(View.VISIBLE);
            AppUtils.showSnackBarMsg("Signing in with Google failed.\nTry again", ctx, (RelativeLayout)(((MainActivity) ctx).findViewById(R.id.main_activity_relative_layout)));
        }
        else{
            AppUtils.showSnackBarMsg("Sorry, temporary server error.", ctx, (RelativeLayout)(((SignUpActivity) ctx).findViewById(R.id.signUpRelativeLayout)));
        }
    }

    private static void handleCheckUserResponse(JSONObject response, Context ctx, boolean signUpWithGoogle, Person currentPerson)
    {
        try
        {
            switch(response.getString("result"))
            {
                //user does not exist in DB
                case AppConstants.RESPONSE_SUCCESS:
                    handleResponseSuccess(ctx, signUpWithGoogle, currentPerson);
                    break;
                case AppConstants.SIGN_UP_USER_EXISTS:
                    handleUserExists(ctx, signUpWithGoogle);
                    break;
                default:
                    handleSignUpErrorResponse(ctx, signUpWithGoogle);
                    System.out.println("error handleCheckUserResponse()");
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void handleResponseSuccess(Context ctx, boolean signUpWithGoogle, Person currentPerson) throws JSONException
    {
        if(signUpWithGoogle == false)
            switchToGetNameActivity(ctx, newUserJSON.getString("username"), newUserJSON.getString("password"));
        else {
            sendUserDetails(ctx, true, null, signUpWithGoogle, currentPerson);
        }


    }

    private static void handleUserExists(Context ctx, boolean signUpWithGoogle)
    {
        if(!signUpWithGoogle)
            Toast.makeText(ctx, "User already exists", Toast.LENGTH_LONG).show();
        else
            try {
                ((MainActivity)ctx).findViewById(R.id.loading_panel_main_activity).setVisibility(View.GONE);
                Log.e(TAG, "Google user exists, about to switch to home activity");
                switchToHomeActivity(ctx, newUserJSON.getString("username"), signUpWithGoogle, "Google");
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    private static void switchToGetNameActivity(Context ctx, String username, String password)
    {
        Intent intent = ((Activity)ctx).getIntent();
        intent.putExtra(AppConstants.SIGN_UP_USERNAME, username);
        intent.putExtra(AppConstants.SIGN_UP_USER_PASSWORD, password);
        intent.setClass(ctx, GetNameActivity.class);
        ctx.startActivity(intent);
    }

    public static void sendUserDetails(final Context ctx, boolean addProfilePicture, String userProfilePictureStr,
                                       final boolean signUpWithGoogle, Person currentPerson) throws JSONException
    {
        JSONObject userJSON;
        String url = AppConstants.SERVER_URL + AppConstants.SIGN_UP_URL;
        //String url = "http://192.168.203.116:3000/signUp";
        boolean userUpdatedSuccessfullyFromGoogle = true;

        if(!signUpWithGoogle) {
            user.loadDetailsFromIntent(((Activity) ctx).getIntent());
            user.setEncodedProfilePic(userProfilePictureStr);
        } else {
            user.loadDetailsFromGooglePerson(currentPerson, newUserJSON.getString("username"));
        }
        user.addJoinedDate();

        try
        {
            userJSON = createUserJSON(user, addProfilePicture, signUpWithGoogle);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, userJSON, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleSignUpResponse(response, user.getUsername(), ctx, signUpWithGoogle);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            handleSignUpErrorResponse(ctx, signUpWithGoogle);
                            System.out.println("error response on sendUserDetails()");
                        }
                    });
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS*3,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void handleSignUpResponse(JSONObject response, String username, Context ctx, boolean signUpWithGoogle)
    {
        try
        {
            switch(response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    if(signUpWithGoogle)
                        ((MainActivity)ctx).findViewById(R.id.loading_panel_main_activity).setVisibility(View.GONE);
                    Log.e(TAG, "Google user signed up, about to switch to home activity");
                    switchToHomeActivity(ctx, username, signUpWithGoogle, "Google");
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    handleSignUpErrorResponse(ctx, signUpWithGoogle);
//                    Toast.makeText(ctx, "Sign Up failed. Server error.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    handleSignUpErrorResponse(ctx, signUpWithGoogle);
                    System.out.println("error on handleSignUpResponse();");
                    break;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject createUserJSON(User user, Boolean addProfilePicture, boolean signUpWithGoogle) throws JSONException
    {
        JSONObject newUserJSON = new JSONObject();
        String encodedProfilePicValue;

        if(user.getEncodedProfilePic() != null && addProfilePicture){
            encodedProfilePicValue = user.getEncodedProfilePic();
            newUserJSON.put("encodedProfilePic", encodedProfilePicValue);
        } else{
            newUserJSON.put("encodedProfilePic", "");
        }

        newUserJSON.put("username", user.getUsername());
        newUserJSON.put("password", user.getPassword());
        newUserJSON.put("fname", user.getFname());
        newUserJSON.put("lname", user.getLname());
        newUserJSON.put("country", user.getCountry());
        newUserJSON.put("age", user.getAge());
        newUserJSON.put("moto", user.getMoto());
        newUserJSON.put("dateJoined", user.getDateJoinedStr());

        if(signUpWithGoogle) {
            newUserJSON.put("signedUpWithGoogle", true);
            newUserJSON.put("isGoogleProfilePic", true);
        }
        else {
            newUserJSON.put("isGoogleProfilePic", false);
            newUserJSON.put("signedUpWithGoogle", false);
        }

        return newUserJSON;
    }

    private static void switchToHomeActivity(Context ctx, String username, boolean signUpWithSocialNetwork, String network)
    {
        Intent intent = new Intent(ctx, HomeActivity.class);

        logger.addRecordToLog("inside switchToHomeActivity");
        SaveSharedPreference.setUserName(ctx.getApplicationContext(), username);
        if(signUpWithSocialNetwork) {
            if (network.equals("Google")) {
                SaveSharedPreference.setIsGoogleSignIn(ctx.getApplicationContext(), true);
                SaveSharedPreference.setIsFacebookSignIn(ctx.getApplicationContext(), false);
            } else if (network.equals("Facebook")) {
                Log.e(TAG, "getting SharedPrefrences if network equals Facebook");
                SaveSharedPreference.setIsFacebookSignIn(ctx.getApplicationContext(), true);
                SaveSharedPreference.setIsGoogleSignIn(ctx.getApplicationContext(), false);
            }
        }

        intent.putExtra("username", user.getUsername());
        //intent.setClass(ctx, HomeActivity.class);
        Log.e(TAG, "switching to HomeActivity");
        try {
            ctx.startActivity(intent);
        }
        catch (Exception e)
        {
            logger.addRecordToLog("Caught an Exception in switchToHomeActivity: " + e.getMessage());
        }

    }

    public static void checkFBUserWithServer(final Context ctx, String email, String id, final String location, final String birthday, final boolean signUpWithFacebook, final Profile currentProfile) {
        logger.addRecordToLog("inside checkFBUserWithServer");
        String url = AppConstants.SERVER_URL + AppConstants.CHECK_IF_USER_EXISTS_URL;
        //String url = "http://192.168.203.2:3000/checkIfUserExists";

        try {
            //String encryptedData = AESHelper.encrypt(seedValue, password);
            newUserJSON.put("username", email);
            newUserJSON.put("password", id);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, newUserJSON, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleFBCheckUserResponse(response, ctx, signUpWithFacebook, currentProfile, location, birthday);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            handleSignUpErrorResponse(ctx, signUpWithFacebook);
                            System.out.println("error response on checkGoogleUserWithServer()");
                        }
                    });
            MainActivity.addRequestToQueue(jsObjRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleFBCheckUserResponse(JSONObject response, Context ctx, boolean signUpWithFacebook, Profile currProfile, String location, String birthday)
    {
        try
        {
            switch(response.getString("result"))
            {
                //user does not exist in DB
                case AppConstants.RESPONSE_SUCCESS:
                    Log.e(TAG,"SUCCESS");
                    logger.addRecordToLog("Handle FB Success");
                    handleFBResponseSuccess(ctx, signUpWithFacebook, currProfile, location, birthday);
                    break;
                case AppConstants.SIGN_UP_USER_EXISTS:
                    logger.addRecordToLog("Handle FB USER EXISTS");
                    handleFBUserExists(ctx, signUpWithFacebook);
                    break;
                default:
                    handleSignUpErrorResponse(ctx, signUpWithFacebook);
                    System.out.println("error handleCheckUserResponse()");
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void handleFBResponseSuccess(Context ctx, boolean signUpWithFacebook, Profile currentProfile, String location, String birthday) throws JSONException
    {
        if(signUpWithFacebook == false)
            switchToGetNameActivity(ctx, newUserJSON.getString("username"), newUserJSON.getString("password"));
        else {
            Log.e(TAG,"inside handleFBResponseSuccess");
            sendFBUserDetails(ctx, true, null, signUpWithFacebook, currentProfile, location, birthday);
        }


    }

    private static void handleFBSignUpErrorResponse(Context ctx, boolean signUpWithFacebook){
        if(signUpWithFacebook){
            ((MainActivity)ctx).findViewById(R.id.loading_panel_main_activity).setVisibility(View.GONE);
            ((MainActivity)ctx).findViewById(R.id.main_activity_content_rl).setVisibility(View.VISIBLE);
            AppUtils.showSnackBarMsg("Signing in with Facebook failed.\nTry again", ctx, (RelativeLayout)(((MainActivity) ctx).findViewById(R.id.main_activity_relative_layout)));
        }
        else{
            AppUtils.showSnackBarMsg("Sorry, temporary server error.", ctx, (RelativeLayout)(((SignUpActivity) ctx).findViewById(R.id.signUpRelativeLayout)));
        }
    }

    public static void sendFBUserDetails(final Context ctx, boolean addProfilePicture, String userProfilePictureStr,
                                         final boolean signUpWithFacebook, Profile currentProfile, String location, String birthday)
                                            throws JSONException
    {
        Log.e(TAG, "inside sendFBUserDetails");
        JSONObject userJSON;
        String url = AppConstants.SERVER_URL + AppConstants.SIGN_UP_URL;
        //String url = "http://192.168.203.116:3000/signUp";
        boolean userUpdatedSuccessfullyFromGoogle = true;

        if(!signUpWithFacebook) {
            user.loadDetailsFromIntent(((Activity) ctx).getIntent());
            user.setEncodedProfilePic(userProfilePictureStr);
        } else {
            user.loadDetailsFromFacebookProfile(ctx, currentProfile, newUserJSON.getString("username"), location, birthday);
        }
        user.addJoinedDate();

        try
        {
            userJSON = createFBUserJSON(user, addProfilePicture, signUpWithFacebook);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, userJSON, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleFBSignUpResponse(response, user.getUsername(), ctx, signUpWithFacebook);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            handleFBSignUpErrorResponse(ctx, signUpWithFacebook);
                            System.out.println("error response on sendUserDetails()");
                        }
                    });
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS*3,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject createFBUserJSON(User user, boolean addProfilePicture, boolean signUpWithFacebook) throws JSONException {

        JSONObject newUserJSON = new JSONObject();
        String encodedProfilePicValue;

        if(user.getEncodedProfilePic() != null && addProfilePicture){
            encodedProfilePicValue = user.getEncodedProfilePic();
            newUserJSON.put("encodedProfilePic", encodedProfilePicValue);
        } else{
            newUserJSON.put("encodedProfilePic", "");
        }

        newUserJSON.put("username", user.getUsername());
        newUserJSON.put("password", user.getPassword());
        newUserJSON.put("fname", user.getFname());
        newUserJSON.put("lname", user.getLname());
        newUserJSON.put("country", user.getCountry());
        newUserJSON.put("age", user.getAge());
        newUserJSON.put("moto", user.getMoto());
        newUserJSON.put("dateJoined", user.getDateJoinedStr());

        if(signUpWithFacebook) {
            newUserJSON.put("signedUpWithFacebook", true);
            newUserJSON.put("isFacebookProfilePic", true);
        }
        else {
            newUserJSON.put("isFacebookProfilePic", false);
            newUserJSON.put("signedUpWithFacebook", false);
        }

        return newUserJSON;
    }

    private static void handleFBSignUpResponse(JSONObject response, String username, Context ctx, boolean signUpWithFacebook)
    {
        try
        {
            switch(response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    if(signUpWithFacebook)
                        ((MainActivity)ctx).findViewById(R.id.loading_panel_main_activity).setVisibility(View.GONE);
                    Log.e(TAG, "in handleFBSignUpResponse success - should go to HomeActivity");
                    switchToHomeActivity(ctx, username, signUpWithFacebook, "Facebook");
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    Log.e(TAG, "in handleFBSignUpResponse Failure - should go to HomeActivity");
                    handleFBSignUpErrorResponse(ctx, signUpWithFacebook);
//                    Toast.makeText(ctx, "Sign Up failed. Server error.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    handleFBSignUpErrorResponse(ctx, signUpWithFacebook);
                    System.out.println("error on handleSignUpResponse();");
                    break;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void handleFBUserExists(Context ctx, boolean signUpWithFacebook)
    {
        if(!signUpWithFacebook)
            Toast.makeText(ctx, "User already exists", Toast.LENGTH_LONG).show();
        else
            try {
                logger.addRecordToLog("inside handleFBUserExists - should switch to HomeActivity");
                ((MainActivity)ctx).findViewById(R.id.loading_panel_main_activity).setVisibility(View.GONE);
                switchToHomeActivity(ctx, newUserJSON.getString("username"), signUpWithFacebook, "Facebook");
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }
}
