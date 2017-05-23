package com.trippin.androidtrippin.trippin;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.daimajia.androidanimations.library.Techniques;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.SaveSharedPreference;
import com.trippin.androidtrippin.model.SignUpUtils;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;

//import com.facebook.CallbackManager;
//
//import com.facebook.FacebookSdk;


public class MainActivity extends MyActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,
        ResultCallback<People.LoadPeopleResult>
{
    private static RequestQueue serverRequestQueue;
    private EditText usernameTB;
    private EditText passwordTB;
    private TextView agreetToTermsTV;

    // Google client to communicate with Google
    private static final String TAG = "GooglePlusSignIn";

    private static final int STATE_DEFAULT = 0; //default state of the app before 'sign in'/after 'sign out'
    private static final int STATE_GOOGLE_SIGN_IN = 1; //user has clicked 'sign in'
    private static final int STATE_IN_PROGRESS = 2; //started an intent to resolve an error
    private static final int STATE_GOOGLE_SIGN_OUT = 3; //started an intent to resolve an error
    private static final int RC_SIGN_IN = 0;
    private static final int FB_LOGIN = 4;

    private static final String SAVED_PROGRESS = "sign_in_progress";
    private static GoogleApiClient mGoogleApiClient;
    private int mSignInProgress;
    private PendingIntent mSignInIntent;
    private int mSignInError;
    private boolean mRequestServerAuthCode = false;
    private boolean mServerHasToken = true;
    private SignInButton googleLoginButton;
    private Button loginButton;
    private LoginButton FBLoginButton;
    private TextView signUpTv;
    private TextView forgotPasswordTV;
    private RelativeLayout mainActivityLayout;
    private Bitmap googlePictureBitmap;
    private ConnectionResult mConnectionResult;
    private boolean mResolveOnFail;
    private CallbackManager callbackManager;
    private LoginManager loginManager;


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_PROGRESS, mSignInProgress);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        callbackManager = CallbackManager.Factory.create();
        checkIfLoggedIn();
        mGoogleApiClient = buildGoogleApiClient();
        AppController.getInstance().setmGoogleApiClient(mGoogleApiClient);
        setContentView(R.layout.activity_main);
        getUIComponents();
        serverRequestQueue = Volley.newRequestQueue(this);
        setUIListeners();

        if (savedInstanceState != null) {
            mSignInProgress = savedInstanceState
                    .getInt(SAVED_PROGRESS, STATE_DEFAULT);
        }
    }

    private void getUIComponents()
    {
        googleLoginButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        googleLoginButton.setSize(SignInButton.SIZE_WIDE);
        loginButton = (Button) findViewById(R.id.loginButton);
        FBLoginButton = (LoginButton)findViewById(R.id.fb_login_button);
        signUpTv = (TextView) findViewById(R.id.signUpTextView);
        forgotPasswordTV = (TextView) findViewById(R.id.forgot_password_TV);
        usernameTB = (EditText) findViewById(R.id.usernameTB);
        passwordTB = (EditText) findViewById(R.id.passwordTB);
        mainActivityLayout = (RelativeLayout) findViewById(R.id.main_activity_relative_layout);
        agreetToTermsTV = (TextView)findViewById(R.id.agree_to_terms_tv);
        setTermsTVText();
        //facebookLoginButton = (LoginButton)findViewById(R.id.facebook_login_button);

    }

    private void setUIListeners()
    {
        loginButton.setOnClickListener(this);
        signUpTv.setOnClickListener(this);
        googleLoginButton.setOnClickListener(this);
        FBLoginButton.setOnClickListener(this);
        mainActivityLayout.setOnClickListener(this);
        forgotPasswordTV.setOnClickListener(this);
        agreetToTermsTV.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.loginButton:
                handleTrippinLogin();
                break;
            case R.id.signUpTextView:
                handleSignUpNewUser();
                break;
            case R.id.fb_login_button:
                handleFacebookLogin();
                break;
            case R.id.forgot_password_TV:
                handleForgotPassword();
                break;
            case R.id.main_activity_relative_layout:
                AppUtils.hideKeyboard(this, findViewById(android.R.id.content).getWindowToken());
                break;
            case R.id.google_sign_in_button:
                handleGoogleLogin();
                break;
            case R.id.agree_to_terms_tv:
                openPolicyDialog();
                break;
        }
    }

    private void setTermsTVText()
    {
        String first = "By signing in you agree to trippin\'s ";
        String second = "<font color='#66B8FF'>privacy policy</font>";
        agreetToTermsTV.setText(Html.fromHtml(first + second));
    }

    private void openPolicyDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Privacy Policy")
                .setMessage("trippin\' will not share your personal data outside this app, including photos, trip details, and login information. However, trippin\' takes no responsibility for any user data and content security.")
                .setCancelable(false)
                .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do nothing (dismiss)
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void handleFacebookLogin()
    {
        setPermissions();
        registerCallback();
    }

//    public static void showHashKey(Context context) {
//        try {
//            PackageInfo info = context.getPackageManager().getPackageInfo(
//                    "com.trippin.androidtrippin", PackageManager.GET_SIGNATURES);
//            for (android.content.pm.Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.i("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//        } catch (NoSuchAlgorithmException e) {
//        }
//    }

    private void setPermissions() {
        FBLoginButton.setReadPermissions("email", "public_profile");
    }

    private void registerCallback() {
        FBLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final boolean signUpWithFacebook = true;
                GraphRequest graphRequest   =   GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback()
                {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response)
                    {
                        Log.d("JSON", ""+response.getJSONObject().toString());
                        String email = object.optString("email");
                        String id = object.optString("id");
                        String location = object.optString("location");
                        mSignInProgress = FB_LOGIN;
                        System.out.println(FacebookSdk.getApplicationSignature(getApplicationContext()));
                        SignUpUtils.checkFBUserWithServer(MainActivity.this, email, id, location, signUpWithFacebook, Profile.getCurrentProfile());
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,first_name,last_name,email,location");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleSignUpNewUser()
    {
        Intent signUpIntent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(signUpIntent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
    }

    private void handleTrippinLogin()
    {
        if(AppUtils.isNetworkAvailable(this))
            handleTrippinUserLogin();
        else {
            AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, this, mainActivityLayout);
        }
    }

    private void handleGoogleLogin()
    {
        if(AppUtils.isNetworkAvailable(this))
        {
            if(mGoogleApiClient.isConnected())
                onConnected(null);
            else
            {
                mSignInProgress = STATE_GOOGLE_SIGN_IN;
                if(mConnectionResult != null){
                    startResolution(mConnectionResult);
                }
            }
        }
        else {
            AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, this, mainActivityLayout);
        }
    }

    private void handleForgotPassword()
    {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.forgot_password_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.forgot_password_enter_email_ET);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setTitle("")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                String email = userInput.getText().toString();
                                if (isValidEmailInput(email))
                                    sendEmailToServer(email);
                                else
                                    showInvalidInputSnackBarMsg("Please enter a valid email address");
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    private void showInvalidInputSnackBarMsg(String inputErrorMsg)
    {
        AppUtils.showSnackBarMsg(inputErrorMsg, MainActivity.this, MainActivity.this.mainActivityLayout);
    }

    private void sendEmailToServer(String email)
    {
        String url = AppConstants.SERVER_URL + AppConstants.RETRIEVE_PASSWORD;
        JSONObject retrievePassJSON = new JSONObject();
        //String url = "http://192.168.203.2:3000/retrievePassword";

        try {
            retrievePassJSON.put("email", email);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, retrievePassJSON, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleRetrievePasswordResponse(response);
                        }
                    }, new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("error response on sendEmailToServer()");
                            AppUtils.showSnackBarMsg("Sorry, no response from server", MainActivity.this, MainActivity.this.mainActivityLayout);
                        }
                    });
            MainActivity.addRequestToQueue(jsObjRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleRetrievePasswordResponse(JSONObject response)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    AppUtils.showSnackBarMsg("Your password has been sent to your email address", this, this.mainActivityLayout);
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    AppUtils.showSnackBarMsg("Couldn't send password to your email address. Server error.", this, this.mainActivityLayout);
                    break;
                default:
                    System.out.println("error on handleRetrievePasswordResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidEmailInput(String email)
    {
        boolean validInput = true;
        boolean isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

        if(!isEmailValid)  {
            validInput = false;
        }

        return validInput;
    }

    @Override
    public void onBackPressed()
    {
        System.out.println("onBack in MainActiviy");
    }

    private void checkIfLoggedIn()
    {
        if(SaveSharedPreference.getUserName(getApplicationContext()).length() != 0) //user still signed in
        {
            if(SaveSharedPreference.getIsGoogleSignIn(getApplicationContext())) {
                mGoogleApiClient = buildGoogleApiClient();
                AppController.getInstance().setmGoogleApiClient(mGoogleApiClient);
                mSignInProgress = STATE_GOOGLE_SIGN_IN;
            }
            else if (SaveSharedPreference.getIsFacebookSignedIn(getApplicationContext())) {
                mSignInProgress = FB_LOGIN;
            }
            switchToHomeActivity(SaveSharedPreference.getUserName(getApplicationContext()));
        }
    }

    private void switchToHomeActivity(String username)
    {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("username", username);
        setSignInState();
        SaveSharedPreference.setUserName(getApplicationContext(), username);
        startActivity(intent);
    }

    private void setSignInState() {
        if(mSignInProgress == STATE_GOOGLE_SIGN_IN) {
            SaveSharedPreference.setIsGoogleSignIn(getApplicationContext(), true);
            SaveSharedPreference.setIsFacebookSignIn(getApplicationContext(), false);
        }
        if(mSignInProgress == FB_LOGIN) {
            SaveSharedPreference.setIsFacebookSignIn(getApplicationContext(), true);
            SaveSharedPreference.setIsGoogleSignIn(getApplicationContext(), false);
        }

    }

    public static void addRequestToQueue(Request request)
    {
        AppController.getInstance().addToRequestQueue(request);
    }

    private void handleTrippinUserLogin()
    {
        String username;
        String password;
        JSONObject userLoginInfo = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.LOGIN_URL;
        //String url = "http://192.168.207.56:3000/login";
//        String url1 = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&types=food&name=cruise&key=AIzaSyAQP3QWMrHOpYZytcnXzD8qCxYGVNpHJ6M";
//        String url2 = "https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJrTLr-GyuEmsRBfy61i59si0&key=AIzaSyAQP3QWMrHOpYZytcnXzD8qCxYGVNpHJ6M";

        username = usernameTB.getText().toString();
        password = passwordTB.getText().toString();
        if (isInputValid(username, password)) {
            try {
                userLoginInfo.put("username", username);
                userLoginInfo.put("password", password);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.POST, url, userLoginInfo, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    handleLoginResponse(response, usernameTB.getText().toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                AppUtils.showSnackBarMsg("Sorry, no response from server", MainActivity.this, MainActivity.this.mainActivityLayout);
                            }
                        });
                addRequestToQueue(jsObjRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleLoginResponse(JSONObject response, String username) throws JSONException
    {
        switch(response.getString("result"))
        {
            case AppConstants.LOGIN_USERNAME_ERROR:
                AppUtils.showSnackBarMsg("No such username", this, this.mainActivityLayout);
                AppUtils.animateView(Techniques.Shake, 700, findViewById(R.id.usernameTB));
                break;
            case AppConstants.LOGIN_PASSWORD_ERROR:
                AppUtils.showSnackBarMsg("Incorrect password. Try again", this, this.mainActivityLayout);
                AppUtils.animateView(Techniques.Shake, 700, findViewById(R.id.passwordTB));
                break;
            case AppConstants.LOGIN_SUCCESS:
                mSignInProgress = STATE_DEFAULT;
                switchToHomeActivity(username);
                break;
            default:
                AppUtils.showSnackBarMsg("Sorry, temporary server error.", this, this.mainActivityLayout);
                System.out.println("error handleLoginResponse()");
                break;
        }
    }

    private boolean isInputValid(String email, String password)
    {
        boolean emailValid = isValidEmailInput(email);
        boolean passwordValid = isValidPassword(password);

        if(emailValid && passwordValid)
            return true;
        else {
            if (!emailValid && !passwordValid) {
                showInvalidInputSnackBarMsg("Please enter valid email address and password");
                AppUtils.animateView(Techniques.Shake, 700, usernameTB);
                AppUtils.animateView(Techniques.Shake, 700, passwordTB);
            }
            else if (!emailValid) {
                showInvalidInputSnackBarMsg("Please enter a valid email address");
                AppUtils.animateView(Techniques.Shake, 700, findViewById(R.id.usernameTB));
            }
            else if(!passwordValid){
                showInvalidInputSnackBarMsg("Please enter your password");
                AppUtils.animateView(Techniques.Shake, 700, findViewById(R.id.passwordTB));
            }
            return false;
        }
    }

    private boolean isValidPassword(String password)
    {
        return password.length() == 0 ? false : true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**************************************************************************/

    /********************** GOOGLE API CLIENT METHODS *************************/

    /**************************************************************************/


    public GoogleApiClient buildGoogleApiClient()
    {
        GoogleApiClient builder = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        return builder;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //if(SaveSharedPreference.getUserName(this).length() != 0) {
            mSignInProgress = STATE_GOOGLE_SIGN_IN;
            AppController.getInstance().getmGoogleApiClient().connect();
        //}
    }

    private void onSignOutClicked()
    {
        //mSignInProgress = STATE_DEFAULT;
        Plus.AccountApi.clearDefaultAccount(AppController.getInstance().getmGoogleApiClient());
        revokeAccess();
    }

    protected void revokeAccess()
    {
        Plus.AccountApi.revokeAccessAndDisconnect(AppController.getInstance().getmGoogleApiClient())
                .setResultCallback(new ResultCallback<Status>()
                {
                    @Override
                    public void onResult(Status status)
                    {
                        AppController.getInstance().getmGoogleApiClient().disconnect();
                        //mGoogleApiClient.connect();
                        // Clear data and go to login activity
                    }
                });
    }

    @Override
    public void onConnected(Bundle connectionHint)
    {
        String email = null;
        boolean signUpWithGoogle;
        Log.i(TAG, "onConnected");
        mResolveOnFail = false;

        if(mSignInProgress == STATE_GOOGLE_SIGN_OUT)
        {
            onSignOutClicked();
        }
        else if(mSignInProgress == STATE_GOOGLE_SIGN_IN) {
            Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);

            if(Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null){
                signUpWithGoogle = true;
                email = Plus.AccountApi.getAccountName(AppController.getInstance().getmGoogleApiClient());
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                findViewById(R.id.main_activity_content_rl).setVisibility(View.INVISIBLE);
                findViewById(R.id.loading_panel_main_activity).setVisibility(View.VISIBLE);
                SignUpUtils.checkGoogleUserWithServer(MainActivity.this, email, null, signUpWithGoogle, currentPerson);
            }
            else{
                onSignOutClicked();
            }
            //switchToHomeActivity(email);
        }



        // Retrieve some profile information to personalize our app for the user.
//            Person currentUser = Plus.PeopleApi.getCurrentPerson(AppController.getInstance().getmGoogleApiClient());

//            Plus.PeopleApi.loadVisible(AppController.getInstance().getmGoogleApiClient(), null).setResultCallback(this);
        // Indicate that the sign in process is complete.
    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
        // Refer to the javadoc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());

        if(result.hasResolution()) {
            mConnectionResult = result;
            if(mResolveOnFail)
                startResolution(result);
        }
    }

    private void startResolution(ConnectionResult result)
    {
        if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            // An API requested for GoogleApiClient is not available. The device's current
            // configuration might not be supported with the requested API or a required component
            // may not be installed, such as the Android Wear application. You may need to use a
            // second GoogleApiClient to manage the application's optional APIs.
            Log.w(TAG, "API Unavailable.");
        } else if (mSignInProgress != STATE_IN_PROGRESS) {
            // We do not have an intent in progress so we should store the latest
            // error resolution intent for use when the sign in button is clicked.
            mSignInIntent = result.getResolution();
            mSignInError = result.getErrorCode();

            if (mSignInProgress == STATE_GOOGLE_SIGN_IN) {
                // STATE_GOOGLE_SIGN_IN indicates the user already clicked the sign in button
                // so we should continue processing errors until the user is signed in
                // or they click cancel.
                resolveSignInError();
            }
        }
    }

    private void resolveSignInError() {
        if (mSignInIntent != null) {
            // We have an intent which will allow our user to sign in or
            // resolve an error.  For example if the user needs to
            // select an account to sign in with, or if they need to consent
            // to the permissions your app is requesting.

            try {
                // Send the pending intent that we stored on the most recent
                // OnConnectionFailed callback.  This will allow the user to
                // resolve the error currently preventing our connection to
                // Google Play services.
                mSignInProgress = STATE_IN_PROGRESS;
                startIntentSenderForResult(mSignInIntent.getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.i(TAG, "Sign in intent could not be sent: "
                        + e.getLocalizedMessage());
                // The intent was canceled before it was sent.  Attempt to connect to
                // get an updated ConnectionResult.
                mSignInProgress = STATE_GOOGLE_SIGN_IN;
                AppController.getInstance().getmGoogleApiClient().connect();
            }
        } else {
            // Google Play services wasn't able to provide an intent for some
            // error types, so we show the default Google Play services error
            // dialog which may still start an intent on our behalf if the
            // user can resolve the issue.
            createErrorDialog().show();
        }
    }

    private Dialog createErrorDialog() {
        if (GooglePlayServicesUtil.isUserRecoverableError(mSignInError)) {
            return GooglePlayServicesUtil.getErrorDialog(
                    mSignInError,
                    this,
                    RC_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Log.e(TAG, "Google Play services resolution cancelled");
                            mSignInProgress = STATE_DEFAULT;
                        }
                    });
        } else {
            return new AlertDialog.Builder(this)
                    .setMessage(R.string.play_services_error)
                    .setPositiveButton(R.string.close,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.e(TAG, "Google Play services error could not be "
                                            + "resolved: " + mSignInError);
                                    mSignInProgress = STATE_DEFAULT;
                                }
                            }).create();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if(requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // If the error resolution was successful we should continue
                // processing errors.
                mResolveOnFail = true;
                mSignInProgress = STATE_GOOGLE_SIGN_IN;
            } else {
                // If the error resolution was not successful or the user canceled,
                // we should stop processing errors.
                mResolveOnFail = false;
                mSignInProgress = STATE_DEFAULT;
            }

            if (!AppController.getInstance().getmGoogleApiClient().isConnecting()) {
                // If Google Play services resolved the issue with a dialog then
                // onStart is not called so we need to re-attempt connection here.
                mSignInProgress = STATE_GOOGLE_SIGN_IN;
                AppController.getInstance().getmGoogleApiClient().connect();
            }
        }
        else if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }


    }

//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//        // Logs 'install' and 'app activate' App Events.
//        AppEventsLogger.activateApp(this);
//    }
//
//    @Override
//    protected void onPause()
//    {
//        super.onPause();
//        // Logs 'app deactivate' App Event.
//        AppEventsLogger.deactivateApp(this);
//    }

    @Override
    public void onResult(People.LoadPeopleResult peopleData) {

    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        // We call connect() to attempt to re-establish the connection or get a
        // ConnectionResult that we can attempt to resolve.
        mSignInProgress = STATE_GOOGLE_SIGN_IN;
        AppController.getInstance().getmGoogleApiClient().connect();
    }

//    private void sendGoogleUserDetailsToServer(final String email)
//    {
//        JSONObject googleUserJSON = null;
//        String url = AppConstants.SERVER_URL + AppConstants.SIGN_UP_URL;
//        //String url = "http://10.0.0.5:3000/completeSignup";
//
//        try
//        {
//            googleUserJSON = getGoogleUserDetailsJSON(email);
//            JsonObjectRequest jsObjRequest = new JsonObjectRequest
//                    (Request.Method.POST, url, googleUserJSON, new Response.Listener<JSONObject>() {
//
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            handleGoogleUserDetailsResponse(response, email);
//                        }
//                    }, new Response.ErrorListener() {
//
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            System.out.println("error response on sendGoogleUserDetailsToServer()");
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
//    private JSONObject getGoogleUserDetailsJSON(String email) throws JSONException
//    {
//        JSONObject googleUserJSON = new JSONObject();
//        String firstName = null;
//        String lastName = null;
//        String imageUrl = null;
//
//        Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
//        if (currentPerson != null) {
//            firstName = currentPerson.getName().getGivenName();
//            lastName = currentPerson.getName().getFamilyName();
//            imageUrl = currentPerson.getImage().getUrl();
//        }
//
//        googleUserJSON.put("username", email);
//        googleUserJSON.put("password", "googleUserPassword");
//        googleUserJSON.put("fname", firstName);
//        googleUserJSON.put("lname", lastName);
//        googleUserJSON.put("profilePicture", imageUrl);
//        googleUserJSON.put("encodedProfilePic", "profilePictureString");
//
//        return googleUserJSON;
//    }
//
//    private void handleGoogleUserDetailsResponse(JSONObject response, String email)
//    {
//        try
//        {
//            switch(response.getString("result"))
//            {
//                case AppConstants.RESPONSE_SUCCESS:
//                    switchToHomeActivity(email);
//                    break;
//                case AppConstants.RESPONSE_FAILURE:
//                    Toast.makeText(this, "Google Sign Up failed. Server error.", Toast.LENGTH_LONG).show();
//                    break;
//                default:
//                    System.out.println("error on handleGoogleUserDetailsResponse();");
//                    break;
//            }
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    // download Google Account profile image, to complete profile
    private class LoadProfileImage extends AsyncTask
    {
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap icon = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                icon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return icon;
        }

        protected void onPostExecute(Bitmap result) {
            googlePictureBitmap = result;
        }

        @Override
        protected Object doInBackground(Object[] params)
        {
            return null;
        }
    }



}