package com.trippin.androidtrippin.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.Profile;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.OnSnackBarActionClickListener;
import com.trippin.androidtrippin.model.SaveSharedPreference;
import com.trippin.androidtrippin.model.User;
import com.trippin.androidtrippin.trippin.HomeActivity;
import com.trippin.androidtrippin.trippin.MainActivity;
import com.trippin.androidtrippin.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyProfileFragment extends Fragment implements OnSnackBarActionClickListener
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //*************************
    private View mainView;
    private User user = new User();

    private EditText fullNameET;
    private Spinner countrySpinner;
    private Spinner ageSpinner;
    private TextView dateJoinedTrippinTV;
    private EditText motoET;
    private TextView contributionsTV;
    private ImageView profilePictureIV;
    private FloatingActionButton editProfileFAB;
    private FloatingActionButton saveChangesFAB;
    private ImageButton saveChangesIB;
    private Drawable originalDrawable;
    private Bitmap userImageBitmap;
    private Boolean userChangedPicture = false;
    private String userProfilePictureStr;
    private ImageButton refreshProfileIB;
    private String lastCountry;
    private Integer lastAge;
    private String lastMoto;
    private Bitmap lastProfilePic;
    private Button changeProfilePictureButton;
    private boolean inEditMode;
    private LinearLayout mainFragLayout;
    private ArrayList<String> countriesList = new ArrayList<>();
    private ArrayAdapter<String> countriesAdapter;
    private ArrayAdapter<Integer> agesAdapter;
    private ArrayList<Integer> agesList = new ArrayList<>();
    private CoordinatorLayout profileCoordinateLayout;

    private final OnSnackBarActionClickListener snackBarListener = this;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyProfileFragment newInstance(String param1, String param2)
    {
        MyProfileFragment fragment = new MyProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MyProfileFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mainView = inflater.inflate(R.layout.fragment_my_profile, container, false);

        getUIComponents();
        disableEdit();
        initCountriesOptionsArray();
        setCountriesSpinnerAdapter();
        initAgesOptionsArray();
        setAgeSpinnerAdapter();
        setUIComponentsListener();
        setRefreshListener();
        mainView.findViewById(R.id.loading_panel_profile).setVisibility(View.VISIBLE);
        getUserDetailsFromServer();


        // Inflate the layout for this fragment
        return mainView;
    }

    private void setRefreshListener()
    {
        refreshProfileIB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                refreshProfileIB.setVisibility(View.GONE);
                mainView.findViewById(R.id.loading_panel_profile).setVisibility(View.VISIBLE);
                getUserDetailsFromServer();
            }
        });
    }

    private void setUIComponentsListener()
    {
        editProfileFAB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (inEditMode)
                    restoreLastProfileInfo();
                else
                    enableEdit();
            }
        });

        saveChangesFAB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (AppUtils.isNetworkAvailable(getActivity()))
                    onSaveClicked();
                else
                    AppUtils.showSnackBarMsgWithAction(getActivity(), profileCoordinateLayout, snackBarListener);
            }
        });
    }

    private void restoreLastProfileInfo()
    {
        inEditMode = false;
        editProfileFAB.setImageResource(R.drawable.ic_create_black_24dp);
        countrySpinner.setSelection(countriesAdapter.getPosition(lastCountry));
        ageSpinner.setSelection(agesAdapter.getPosition(lastAge));
        motoET.setText(lastMoto);
        profilePictureIV.setImageBitmap(lastProfilePic);

        disableEdit();
    }

    private void onSaveClicked()
    {
        inEditMode = false;
        editProfileFAB.setImageResource(R.drawable.ic_create_black_24dp);

        saveNewDetails();
        disableEdit();
        sendUpdatedUserInfoToServer();
        userChangedPicture = false;
    }

    private void saveNewDetails()
    {
        countrySpinner.setSelection(countrySpinner.getSelectedItemPosition());
        ageSpinner.setSelection((ageSpinner.getSelectedItemPosition()));
        motoET.setText(motoET.getText());
        profilePictureIV.buildDrawingCache();
        profilePictureIV.setImageBitmap(profilePictureIV.getDrawingCache());
    }

    private void sendUpdatedUserInfoToServer()
    {
        JSONObject updatedUserJSON;
        String url = AppConstants.SERVER_URL + AppConstants.UPDATE_USER_INFO;
        //String url = "http://192.168.203.2:3000/updateUserInfo";

        updatedUserJSON = getUpdatedInfoJSON();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, updatedUserJSON, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handleUpdateUserInfoResponse(response, getActivity());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("error response on sendUserDetails()");
                    }
                });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MainActivity.addRequestToQueue(jsObjRequest);
    }

    private JSONObject getUpdatedInfoJSON()
    {
        JSONObject updatedInfoJSON = new JSONObject();
        Bitmap bitmap = ((BitmapDrawable) profilePictureIV.getDrawable()).getBitmap();
        userImageBitmap = AppUtils.resizeBitmap(bitmap, AppConstants.IMAGE_MAX_SIZE);

        try {
            updatedInfoJSON.put("username", SaveSharedPreference.getUserName(getActivity()));
            updatedInfoJSON.put("fname", user.getFname());
            updatedInfoJSON.put("lname", user.getLname());
            updatedInfoJSON.put("dateJoined", user.getDateJoinedStr());
            updatedInfoJSON.put("country", countrySpinner.getSelectedItem().toString());
            updatedInfoJSON.put("moto", motoET.getText().toString());
            updatedInfoJSON.put("age", ageSpinner.getSelectedItem().toString());
            if(userChangedPicture == true) {
                updatedInfoJSON.put("encodedProfilePic", AppUtils.bitMapToString(userImageBitmap));
                updatedInfoJSON.put("isGoogleProfilePic", false);
                updatedInfoJSON.put("isFacebookProcilePic", false);
                user.setIsGoogleProfilePic(false);
                user.setIsFacebookProfilePic(false);
            }
            else {
                updatedInfoJSON.put("encodedProfilePic", AppConstants.EMPTY_STRING);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return updatedInfoJSON;
    }

    private void handleUpdateUserInfoResponse(JSONObject response, Context ctx)
    {
        try
        {
            switch(response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    //handleUserDetailsResponse(response);
                    AppUtils.showSnackBarMsg("User info updated successfully", ctx, profileCoordinateLayout);
                    //Toast.makeText(ctx, "User info updated successfully", Toast.LENGTH_LONG).show();
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    //Toast.makeText(ctx, "Update of details failed. Server error.", Toast.LENGTH_LONG).show();
                    AppUtils.showSnackBarMsg("Update of details failed. Server error.", ctx, profileCoordinateLayout);
                    break;
                default:
                    System.out.println("error on handleSignUpResponse();");
                    break;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void disableEdit()
    {
        countrySpinner.setEnabled(false);
        ageSpinner.setEnabled(false);
        motoET.setEnabled(false);
        motoET.setTextColor(getResources().getColor(R.color.list_item_dark_gray));
        dateJoinedTrippinTV.setEnabled(false);
        contributionsTV.setEnabled(false);
        countrySpinner.setBackground(null);
        ageSpinner.setBackground(null);
        motoET.setBackground(null);
        saveChangesFAB.setVisibility(View.INVISIBLE);
        saveChangesFAB.setEnabled(false);
        saveChangesFAB.setClickable(false);
        editProfileFAB.setVisibility(View.VISIBLE);
        editProfileFAB.setEnabled(true);
        editProfileFAB.setClickable(true);
        changeProfilePictureButton.setVisibility(View.INVISIBLE);
        changeProfilePictureButton.setOnClickListener(null);
    }

    private void enableEdit()
    {
        inEditMode = true;
        editProfileFAB.setImageResource(R.drawable.ic_clear_black_24dp);
        saveLastProfileInfo();

        fullNameET.setEnabled(true);
        motoET.setTextColor(getResources().getColor(R.color.black));
        countrySpinner.setEnabled(true);
        ageSpinner.setEnabled(true);
        motoET.setEnabled(true);
        countrySpinner.setBackground(originalDrawable);
        ageSpinner.setBackground(originalDrawable);
        //motoET.setBackground(originalDrawable);

        saveChangesFAB.setVisibility(View.VISIBLE);
        saveChangesFAB.setEnabled(true);
        saveChangesFAB.setClickable(true);

        changeProfilePictureButton.setVisibility(View.VISIBLE);
        changeProfilePictureButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //handleSelect(v);
                openUploadProfilePicDialog();
            }
        });

    }

    private void openUploadProfilePicDialog()
    {
        String[] imageOptions = {"Camera Roll", "Take a Photo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.changeProfilePicOptions)
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
        switch (which){
            case AppConstants.CAMERA_ROLL_REQUEST_CODE-1:
                handleSelect();
                break;
            case AppConstants.CAMERA_REQUEST_CODE-1:
                handleOpenCamera();
                break;
            default:
                System.out.println("in executeSelectedOption default myProfileFragment");
        }
    }

    private void handleOpenCamera()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, AppConstants.CAMERA_REQUEST_CODE+1);
    }

    private void saveLastProfileInfo()
    {
        lastCountry = countrySpinner.getSelectedItem().toString();
        lastAge = (Integer)ageSpinner.getSelectedItem();
        lastMoto = motoET.getText().toString();
        if(profilePictureIV.getDrawable() != null) {
            lastProfilePic = ((BitmapDrawable) profilePictureIV.getDrawable()).getBitmap();
            if(lastProfilePic!=null)
                lastProfilePic = AppUtils.resizeBitmap(lastProfilePic, AppConstants.IMAGE_MAX_SIZE);
        }

    }

    private void getUIComponents()
    {
        fullNameET = (EditText) mainView.findViewById(R.id.profile_fullname_ET);
        countrySpinner = (Spinner)mainView.findViewById(R.id.profile_country_spinner);
        motoET = (EditText) mainView.findViewById(R.id.moto_ET);
        dateJoinedTrippinTV = (TextView) mainView.findViewById(R.id.profile_dateJoined_tv);
        contributionsTV = (TextView) mainView.findViewById(R.id.contributions_TV);
        profilePictureIV = (ImageView) mainView.findViewById(R.id.profile_picture_IV);
        editProfileFAB = (FloatingActionButton) mainView.findViewById(R.id.edit_profile_FAB);
        saveChangesFAB = (FloatingActionButton) mainView.findViewById(R.id.save_changes_FAB);
        ageSpinner = (Spinner) mainView.findViewById(R.id.profile_age_spinner);
        refreshProfileIB = (ImageButton)mainView.findViewById(R.id.refresh_profile_IB);
        mainFragLayout = (LinearLayout)mainView.findViewById(R.id.profile_main_layout);
        profileCoordinateLayout = (CoordinatorLayout)mainView.findViewById(R.id.profile_fragment);
        changeProfilePictureButton = (Button)mainView.findViewById(R.id.profile_change_photo_Button);

        originalDrawable = motoET.getBackground();
    }

    private void handleSelect()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), AppConstants.CAMERA_ROLL_REQUEST_CODE + 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK){
            if (requestCode == (AppConstants.CAMERA_ROLL_REQUEST_CODE+1)){
                try {
                    userImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                    userImageBitmap = AppUtils.resizeBitmap(userImageBitmap, AppConstants.IMAGE_MAX_SIZE);
                    profilePictureIV.setImageBitmap(userImageBitmap);
                    String encodedProfilePic = AppUtils.bitMapToString(userImageBitmap);
                    userProfilePictureStr = encodedProfilePic;
                    userChangedPicture = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(requestCode == (AppConstants.CAMERA_REQUEST_CODE+1)){
                userImageBitmap = (Bitmap) data.getExtras().get("data");
                userImageBitmap = AppUtils.resizeBitmap(userImageBitmap, AppConstants.IMAGE_MAX_SIZE);
                profilePictureIV.setImageBitmap(userImageBitmap);
                String encodedProfilePic = AppUtils.bitMapToString(userImageBitmap);
                userProfilePictureStr = encodedProfilePic;
                userChangedPicture = true;
            }
        }
    }

    private void getUserDetailsFromServer()
    {

        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null)
        {
            JSONObject usernameJSON = new JSONObject();
            String url = AppConstants.SERVER_URL + AppConstants.GET_USER;

            try
            {
                usernameJSON.put("username", homeActivity.getUsername());
                JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, usernameJSON, new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try {
                            handleUserDetailsResponse(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        System.out.println("error response on getUserDetailsFromServer()");
                        //getActivity().findViewById(R.id.refresh_profile_IB).setVisibility(View.VISIBLE);
                        mainView.findViewById(R.id.loading_panel_profile).setVisibility(View.GONE);
                        AppUtils.showSnackBarMsgWithAction(getActivity(), profileCoordinateLayout, snackBarListener);
                    }
                });

                MainActivity.addRequestToQueue(jsObjRequest);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleUserDetailsResponse(JSONObject response) throws JSONException
    {
        if (response != null)
        {
            mainView.findViewById(R.id.refresh_profile_IB).setVisibility(View.GONE);
            mainView.findViewById(R.id.loading_panel_profile).setVisibility(View.GONE);
            user = new User();
            user.parseDetailsFromJSON(response);
            showUIComponents();
            appendInfoToTextViews();
        }
    }

    private void showUIComponents()
    {
        getActivity().findViewById(R.id.edit_profile_FAB).setVisibility(View.VISIBLE);

        getActivity().findViewById(R.id.profile_fullname_ET).setVisibility(View.VISIBLE);
        if(!user.getUserHasInfo()) {
            fullNameET.setTextColor(getResources().getColor(R.color.black));
        }

        getActivity().findViewById(R.id.age_TV).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.profile_country_title).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.profile_age_spinner).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.profile_country_spinner).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.date_joined_title_TV).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.profile_moto_TV).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.profile_contributions_TV).setVisibility(View.VISIBLE);
    }

    private void appendInfoToTextViews()
    {
//        fullNameET.setText(user.getFname().substring(0, 1).toUpperCase() + user.getFname().substring(1)
//                + " " + user.getLname().toUpperCase().substring(0, 1) + ".");
        if(user.getUserHasInfo()) {
            setProfilePic();
            fullNameET.setText(user.getFname().substring(0, 1).toUpperCase() + user.getFname().substring(1)
                    + " " + user.getLname().substring(0, 1).toUpperCase() + user.getLname().substring(1));

            ageSpinner.setSelection(agesAdapter.getPosition(user.getAgeNumber()));
            setCountrySpinner();
            dateJoinedTrippinTV.setText(user.getDateJoinedStr());
            motoET.setText(user.getMoto());
            contributionsTV.setText(Integer.toString(user.getNumOfTrips()));
        }
    }

    private void setCountrySpinner() {
        if(user.getCountry().equals(AppConstants.EMPTY_STRING))
            countrySpinner.setSelection(countriesAdapter.getPosition("United States"));
        else
            countrySpinner.setSelection(countriesAdapter.getPosition(user.getCountry()));
    }

    private void setProfilePic()
    {
        if(user.isGoogleProfilePic())
            setUpGoogleImageFromURL();
        else if(user.isFBProfilePic())
            setUpFBImageFromURL(getPictureURL());
        else if(!user.isGoogleProfilePic())
            setUpNormalProfilePic();
        else
            profilePictureIV.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.no_user_logo));
    }

    private String getPictureURL() {
        return user.getEncodedProfilePic();
    }

    private void setUpFBImageFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap FacebookBitmap = BitmapFactory.decodeStream(input);
            profilePictureIV.setImageBitmap(FacebookBitmap);
        } catch (IOException e) {
            // Log exception
            setUpNormalProfilePic();
        }

    }

    private void setUpGoogleImageFromURL()
    {
        Bitmap googlePictureBitmap;
        String googleUserUrl = user.getEncodedProfilePic();
        String subURL = googleUserUrl.substring(0, 13);
        if(subURL.equals("https://graph")) {
            String fb_url = getFBImageURL(Profile.getCurrentProfile().getId());
            new GetProfileImage().execute(fb_url);

        }
        else {
            int urlLength = googleUserUrl.length();
            String newUrl = googleUserUrl.substring(0, urlLength - 2);
            StringBuilder finalUrl = new StringBuilder().append(newUrl).append("250");

            if (finalUrl.toString() == null || finalUrl.toString().equals(AppConstants.EMPTY_STRING)) {
                googlePictureBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_user_logo);
                profilePictureIV.setImageBitmap(googlePictureBitmap);
            } else
                new GetProfileImage().execute(finalUrl.toString());
            //googlePictureBitmap = AppUtils.fromUrlToBitmap(user.getEncodedProfilePic());
        }
    }

//    private Bitmap getFacebookProfilePicture(String userID){
//        Bitmap bm = null;
//        URL imageURL = null;
//        try {
//            imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
//            bm = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return bm;
//    }

    private String getFBImageURL(String userID) {
           return "https://graph.facebook.com/" + userID + "/picture?type=large";
    }

    private void setUpNormalProfilePic()
    {
        Bitmap userProfilePic;
        String bitmapStr = user.getEncodedProfilePic();
        if(bitmapStr == null || bitmapStr.equals(AppConstants.EMPTY_STRING))
            userProfilePic = BitmapFactory.decodeResource(getResources(), R.drawable.no_user_logo);
        else {
            userProfilePic = AppUtils.stringToBitMap(bitmapStr);
            userImageBitmap = AppUtils.resizeBitmap(userProfilePic, AppConstants.IMAGE_MAX_SIZE);
        }

        profilePictureIV.setImageBitmap(userImageBitmap);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSnackBarActionClick()
    {
        getUserDetailsFromServer();
    }

    private class GetProfileImage extends AsyncTask<String, Void, Bitmap>
    {

        protected Bitmap doInBackground(String... urls)
        {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result)
        {
            userImageBitmap = AppUtils.resizeBitmap(result, AppConstants.IMAGE_MAX_SIZE);
            profilePictureIV.setImageBitmap(userImageBitmap);
        }
    }

    private void initCountriesOptionsArray()
    {
        countriesList = AppUtils.getCountriesList();
    }

    private void setCountriesSpinnerAdapter()
    {
        String defaultCountry = null;
        countriesAdapter = new ArrayAdapter<>(getActivity(), R.layout.my_spinner_item, countriesList);
        countrySpinner.setAdapter(countriesAdapter);
        if(user.getUserHasInfo()) {
            defaultCountry = user.getCountry();
            countrySpinner.setSelection(countriesAdapter.getPosition(defaultCountry));
        }
        else {
            countrySpinner.setSelection(countriesAdapter.getPosition("United States"));
        }
    }

    private void initAgesOptionsArray()
    {
        for(int i = AppConstants.MIN_AGE ; i < AppConstants.MAX_AGE ; i++)
        {
            agesList.add(i-AppConstants.MIN_AGE, i);
        }

    }

    private void setAgeSpinnerAdapter()
    {
        agesAdapter = new ArrayAdapter<>(getActivity(), R.layout.my_spinner_item, agesList);
        ageSpinner.setAdapter(agesAdapter);
    }
}
