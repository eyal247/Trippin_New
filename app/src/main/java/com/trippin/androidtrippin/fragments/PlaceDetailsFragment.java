package com.trippin.androidtrippin.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.GooglePlace;
import com.trippin.androidtrippin.model.GooglePlaceLoader;
import com.trippin.androidtrippin.model.GooglePlacesUtils;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.OnGooglePlaceLoadListener;
import com.trippin.androidtrippin.model.OnSnackBarActionClickListener;
import com.trippin.androidtrippin.model.PhotoInfo;
import com.trippin.androidtrippin.model.SaveSharedPreference;
import com.trippin.androidtrippin.trippin.AppController;
import com.trippin.androidtrippin.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlaceDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaceDetailsFragment extends Fragment implements OnGooglePlaceLoadListener, OnSnackBarActionClickListener
{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PLACE_ID = "placeID";
    private static final String PLACE_NAME = "placeName";

    private String placeID;
    private String placeName;

    private OnFragmentInteractionListener mListener;

    private GooglePlace place = null;
    //private Trip currTrip;
    private TextView placeNameTV;
//    private ImageView mainPlaceImage;
    private NetworkImageView mainPlaceImage;
    private ImageButton leftArrow;
    private ImageButton rightArrow;
    private TextView placeAddressTV;
    private TextView placePhoneNumTV;
    private TextView placeRatingTV;
    private TextView placePriceLevelTV;
    private TextView placeWebsiteTV;
    private TextView placeDescriptionTV;
    private TextView inYourTripTV;
    private FrameLayout placeDetailsLayout;
    private Button tripPlaceButton;
    private int photosIndex = 0;
    private View mainView;
    private ArrayList<String> myTripPlacesGoogleIDs = new ArrayList<>();
    private GooglePlaceLoader googlePlaceLoader;
    private Boolean fullDescriptionIsShown;
    private String shortDescription;
    private String fullDescription;
    private ImageButton refreshPlaceDetailsFragIB;
    
    private final OnSnackBarActionClickListener snackBarListener = this;
    private boolean imageLoaded;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param placeID Parameter 1.
     * @param placeName Parameter 2.
     * @return A new instance of fragment PlaceDetailsFragment.
     */

    public static PlaceDetailsFragment newInstance(String placeID, String placeName)
    {
        PlaceDetailsFragment fragment = new PlaceDetailsFragment();
        Bundle args = new Bundle();
        args.putString(PLACE_ID, placeID);
        args.putString(PLACE_NAME, placeName);
        fragment.setArguments(args);

        return fragment;
    }

    public PlaceDetailsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeID = getArguments().getString(PLACE_ID);
            placeName = getArguments().getString(PLACE_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mainView = inflater.inflate(R.layout.fragment_place_details, container, false);

        getUIComponents();
        setUIComponentsListeners();
//        setTripPlaceButtonText();
        if (place == null){
//            getPlaceDetails(placeID);
            String myTripID = SaveSharedPreference.getTripId(getActivity().getApplicationContext());
            if(placeID != null) {
                googlePlaceLoader = new GooglePlaceLoader(myTripPlacesGoogleIDs, myTripID, this, placeID, getActivity());
                googlePlaceLoader.loadGooglePlace();
            }
        }
        else
            setUIComponents();

        return mainView;
    }

//    private void getMyTripPlacesGoogleIDsFromServer()
//    {
//        String myTripID = SaveSharedPreference.getTripId(getActivity().getApplicationContext());
//        JSONObject tripJSON = new JSONObject();
//        String url = AppConstants.SERVER_URL + AppConstants.GET_TRIP_PLACES_GOOGLE_IDS;
//        //String url = "http://192.168.204.50:3000/getTripPlacesGoogleIDs";
//
//        try
//        {
//            tripJSON.put("tripID", myTripID);
//
//            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripJSON, new Response.Listener<JSONObject>()
//            {
//                @Override
//                public void onResponse(JSONObject response) {
//                    onGetTripPlacesGoogleIDsResponse(response);
//                }
//            }, new Response.ErrorListener() {
//
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    System.out.println("error response on getMyTripPlacesGoogleIDsFromServer");
//                }
//            });
//
//            MainActivity.addRequestToQueue(jsObjRequest);
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void onGetTripPlacesGoogleIDsResponse(JSONObject response)
//    {
//        try {
//            switch(response.getString("result"))
//            {
//                case AppConstants.RESPONSE_SUCCESS:
//                    parseGoogleIDs(response);
//                    break;
//                case AppConstants.RESPONSE_FAILURE:
//                    System.out.println("Server error - received failure on onGetTripPlacesGoogleIDsResponse");
//                    break;
//                default:
//                    System.out.println("error onGetTripPlacesGoogleIDsResponse()");
//            }
//            getPlaceDetails(placeID);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void parseGoogleIDs(JSONObject response)
//    {
//        JSONArray googleIDsJSONArray;
//
//        if (response.has("googlePlacesIDs")) {
//            try {
//                googleIDsJSONArray = response.getJSONArray("googlePlacesIDs");
//
//                for (int i = 0; i < googleIDsJSONArray.length(); i++) {
//                    String currGoogleID = googleIDsJSONArray.get(i).toString();
//                    myTripPlacesGoogleIDs.add(currGoogleID);
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private void setTripPlaceButtonText()
    {
        if (getActivity() == null)
            return;

        if (getActivity().getIntent().hasExtra("caller"))
        {
            if (getActivity().getIntent().getExtras().getString("caller").equals("OtherUserTripActivity")) {
                setOtherUserTripPlaceButtonText();
            }
            else {
                tripPlaceButton.setVisibility(View.VISIBLE);
                tripPlaceButton.setClickable(true);
                tripPlaceButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                tripPlaceButton.setText("Delete From Trip");
            }
        }
        else
            tripPlaceButton.setText("Delete From Trip");
    }

    private void setOtherUserTripPlaceButtonText()
    {
        boolean found = false;

        for (int i = 0; i < myTripPlacesGoogleIDs.size() && !found; i++)
        {
            if (placeID.equals(myTripPlacesGoogleIDs.get(i)))
            {
                tripPlaceButton.setVisibility(View.GONE);
                tripPlaceButton.setClickable(false);
                inYourTripTV.setVisibility(View.VISIBLE);
                found = true;
            }
        }
        if (!found) {
            inYourTripTV.setVisibility(View.GONE);
            tripPlaceButton.setVisibility(View.VISIBLE);
            tripPlaceButton.setClickable(true);
        }
    }

//    private void getPlaceDetails(final String placeID)
//    {
//        new AsyncTask()
//        {
//            String results;
//
//            @Override
//            protected Object doInBackground(Object[] params)
//            {
//                String placeDetailsRequestURL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeID + "&key=" + GoogleServerKey.GOOGLE_SERVER_KEY;
//                results = GooglePlacesUtils.makeCall(placeDetailsRequestURL);
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Object o)
//            {
//                super.onPostExecute(o);
//
//                place = GooglePlacesUtils.parseGooglePlacesDetailsJson(results);
//                setUIComponents();
//            }
//        }.execute();
//    }

    private void setUIComponents()
    {
        setTripPlaceButtonText();
        placeNameTV.setText(place.getName());
        //placeAddressTV.setText(place.getAddress());
        setAndUnderlineAddressTV();

        placePhoneNumTV.setText(place.getInternationalPhoneNum());
        Linkify.addLinks(placePhoneNumTV, Linkify.PHONE_NUMBERS);
        placeRatingTV.setText(place.getRating());
        placeWebsiteTV.setText(place.getWebsite());
        Linkify.addLinks(placeWebsiteTV, Linkify.WEB_URLS);
        setDescriptionGravity();
        createDescriptionSubString();
        setDescriptionTvText();

        setPriceLevelTV();
        loadNetworkImage(0);
    }

    private void setAndUnderlineAddressTV()
    {
        SpannableString address = new SpannableString(place.getAddress());
        address.setSpan(new UnderlineSpan(), 0, address.length(), 0);
        placeAddressTV.setText(address);
    }

    private void setDescriptionGravity()
    {
        if(place.getDescription().equals(""))
            placeDescriptionTV.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    private void setPriceLevelTV()
    {
        String priceLevel = place.getPriceLevel();

        switch (priceLevel)
        {
            case "0":
                placePriceLevelTV.append(" Free");
                break;
            case "1":
                placePriceLevelTV.append(" Inexpensive");
                break;
            case "2":
                placePriceLevelTV.append(" Moderate");
                break;
            case "3":
                placePriceLevelTV.append(" Expensive");
                break;
            case "4":
                placePriceLevelTV.append(" Very Expensive");
                break;
            default:
                placePriceLevelTV.append(" Not Available");
                break;
        }
    }

    private void getUIComponents()
    {
        inYourTripTV = (TextView) mainView.findViewById(R.id.in_your_trip_fragment_TV);
        placeNameTV = (TextView) mainView.findViewById(R.id.place_name_frag_tv);
        mainPlaceImage = (NetworkImageView) mainView.findViewById(R.id.main_place_frag_image);
        setMainPlaceImageDefaults();
        placeAddressTV = (TextView) mainView.findViewById(R.id.place_address_frag_tv);
        placePhoneNumTV = (TextView) mainView.findViewById(R.id.place_phone_frag_tv);
        placeRatingTV = (TextView) mainView.findViewById(R.id.place_rating_frag_tv);
        placePriceLevelTV = (TextView) mainView.findViewById(R.id.place_price_level_frag_tv);
        placeWebsiteTV = (TextView) mainView.findViewById(R.id.place_website_frag_tv);
        leftArrow = (ImageButton) mainView.findViewById(R.id.left_arrow_frag);
        rightArrow = (ImageButton) mainView.findViewById(R.id.right_arrow_frag);
        tripPlaceButton = (Button) mainView.findViewById(R.id.trip_button_place_frag);
        placeDescriptionTV = (TextView) mainView.findViewById(R.id.place_fragment_description_tv);
        refreshPlaceDetailsFragIB = (ImageButton)mainView.findViewById(R.id.refresh_place_details_frag_IB);
        placeDetailsLayout = (FrameLayout)mainView.findViewById(R.id.place_details_layout);
    }

    private void setMainPlaceImageDefaults()
    {
        mainPlaceImage.setDefaultImageResId(R.drawable.loading);
        mainPlaceImage.setErrorImageResId(R.drawable.no_photo_available);
    }

    private void setUIComponentsListeners()
    {

        rightArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onPictureArrowClicked(AppConstants.RIGHT_ARROW_CLICKED); //right arrow
            }
        });

        leftArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onPictureArrowClicked(!AppConstants.RIGHT_ARROW_CLICKED); //not right arrow
            }
        });

        tripPlaceButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(AppUtils.isNetworkAvailable(getActivity()))
                    onTripPlaceButtonClick();
                else
                    AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, getParentFragment().getActivity(), placeDetailsLayout);
            }
        });

        placeDescriptionTV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!placeDescriptionTV.getText().equals(AppConstants.NO_PLACE_DESCRIPTION))
                {
                    if (!fullDescriptionIsShown) {
                        placeDescriptionTV.setText(fullDescription);
                        fullDescriptionIsShown = true;
                    } else {
                        placeDescriptionTV.setText(shortDescription);
                        fullDescriptionIsShown = false;
                    }
                }
            }
        });

        refreshPlaceDetailsFragIB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                refreshPlaceDetailsFragIB.setVisibility(View.GONE);
                mainView.findViewById(R.id.loading_panel_place_details_fragment).setVisibility(View.VISIBLE);
                String myTripID = SaveSharedPreference.getTripId(getActivity().getApplicationContext());
                if(placeID != null) {
                    googlePlaceLoader = new GooglePlaceLoader(myTripPlacesGoogleIDs, myTripID, PlaceDetailsFragment.this, placeID, getActivity());
                    googlePlaceLoader.loadGooglePlace();
                }
            }
        });

        placeAddressTV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showNavigateDialog();
            }
        });
    }

    private void showNavigateDialog()
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: //Yes button clicked
                        openGoogleMapsForNavigation();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE: //No button clicked
                        // Nothing to do
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Would you like to navigate here?").setPositiveButton("Navigate", dialogClickListener).setNegativeButton("Not now", dialogClickListener).show();
    }

    private void openGoogleMapsForNavigation()
    {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + place.getLatitude() + ", " + place.getLongitude() + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void onTripPlaceButtonClick()
    {
        if (tripPlaceButton.getText().toString().equals("Delete From Trip"))
        {
            onDeletePlace();
        }
        else
        {
            onAddToMyTrip();
        }
    }

    private void onAddToMyTrip()
    {
        tripPlaceButton.setClickable(false);
        Bitmap placeImageBitmap = ((BitmapDrawable) mainPlaceImage.getDrawable()).getBitmap();
        placeImageBitmap = Bitmap.createScaledBitmap(placeImageBitmap, 150, 150, false);
        String placeImageDecodedString = AppUtils.bitMapToString(placeImageBitmap);
        Bundle bundle = new Bundle();

        bundle.putString("image", placeImageDecodedString);
        bundle.putString("place_id", place.getPlaceID());
        bundle.putString("place_name", place.getName());
        bundle.putDouble("lat", place.getLatitude());
        bundle.putDouble("lng", place.getLongitude());

        mListener.onFragmentInteraction("addToMyTrip", bundle);
    }

    private void setDescriptionTvText()
    {
        if(!fullDescription.equals(AppConstants.EMPTY_STRING)) {
            if (fullDescriptionIsShown)
                placeDescriptionTV.setText(fullDescription);
            else
                placeDescriptionTV.setText(shortDescription);
        }
        else
            placeDescriptionTV.setText(AppConstants.NO_PLACE_DESCRIPTION);
    }

    private void createDescriptionSubString()
    {
        fullDescription = place.getDescription();
        shortDescription = fullDescription;

        if(fullDescription.length() > 100) {
            shortDescription = fullDescription.substring(0, 100) + "...";
            fullDescriptionIsShown = false;
        } else {
            fullDescriptionIsShown = true;
        }
    }

    private void onDeletePlace()
    {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE: //Yes button clicked
                        deletePlace();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE: //No button clicked
                        // Nothin to do
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete place from trip?").setPositiveButton("Delete", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).show();
    }

    private void deletePlace()
    {
        mListener.onFragmentInteraction("deletePlace", (Object[]) null);
    }

    private void onPictureArrowClicked(boolean arrowDirection)
    {
        System.out.println("onClick of mainImageView!!!");
        changePlacePhoto(arrowDirection);
    }

    private void changePlacePhoto(boolean arrowDirection)
    {
        Bitmap photo;

        setPhotoIndex(arrowDirection);

//        System.out.println("On changePlacePhoto()!!!");
//        photo = place.getPhoto(photosIndex);
//
//        System.out.println("photos index: " + photosIndex);
//
//        if (photo != null)
//        {
//            System.out.println("photo bitmap: " + photo.toString());
//            mainPlaceImage.setImageBitmap(photo);
//        }
//        else
//        {
//            System.out.println("photo is null");
//            loadImage(photosIndex);
//        }
        loadNetworkImage(photosIndex);
    }

    private void loadNetworkImage(int index)
    {
        String imageUrl = GooglePlacesUtils.createPlaceImageUrl(place.getPhotoInfoFromList(index));

        if(AppUtils.isNetworkAvailable(getActivity())) {
            if (imageUrl == null) {
                mainPlaceImage.setDefaultImageResId(R.drawable.no_photo_available);
            } else {
                mainPlaceImage.setImageUrl(imageUrl, AppController.getInstance().getImageLoader());
            }

        }
        else
        {
            AppUtils.showSnackBarMsgWithAction(getActivity(), placeDetailsLayout, snackBarListener);
        }
    }

    private void setPhotoIndex(boolean arrowDirection)
    {
        if(arrowDirection == AppConstants.RIGHT_ARROW_CLICKED)
        {
            if (photosIndex < place.getPlacePhotosInfo().size() - 1)
                photosIndex++;
            else
                photosIndex = 0;
        }

        else //left arrow was clicked
        {
            if (photosIndex == AppConstants.FIRST_PHOTO_INDEX)
                photosIndex = place.getPlacePhotosInfo().size() - 1;
            else
                photosIndex--;
        }
    }

    private void loadImage(final int index)
    {
        new AsyncTask()
        {
            Bitmap mainPhotoBitmap = null;
            PhotoInfo mainPhotoInfo = place.getPhotoInfoFromList(index);

            @Override
            protected Object doInBackground(Object[] params)
            {
                if(mainPhotoInfo != null)
                    mainPhotoBitmap = GooglePlacesUtils.makePhotoCall(mainPhotoInfo.getPhotoRef(), mainPhotoInfo.getMaxHeight());

                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                super.onPostExecute(o);

                if (mainPhotoBitmap != null) {
                    mainPlaceImage.setImageBitmap(mainPhotoBitmap);
                    place.addPhoto(mainPhotoBitmap);
                } else {
//                    if (photosIndex > 0)
//                        photosIndex--;
                }
            }
        }.execute();
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
    public void onGooglePlaceLoadFinished(String event, Object obj)
    {
        switch (event){
            case AppConstants.RESPONSE_SUCCESS:
                place = (GooglePlace)obj;
                showArrowsIfNeeded();
                mainView.findViewById(R.id.loading_panel_place_details_fragment).setVisibility(View.GONE);
                setUIComponents();
                mainView.findViewById(R.id.place_details_fragment_rl).setVisibility(View.VISIBLE);
                break;
            case AppConstants.RESPONSE_FAILURE:
                refreshPlaceDetailsFragIB.setVisibility(View.VISIBLE);
                AppUtils.showSnackBarMsgWithAction(getActivity(), placeDetailsLayout, snackBarListener);
                mainView.findViewById(R.id.loading_panel_place_details_fragment).setVisibility(View.GONE);
                break;
            case AppConstants.PLACE_IS_NULL:
                getActivity().onBackPressed();
                break;
            default:
                System.out.println("error on onGooglePlaceLoadFinished switch case");
        }

    }

    private void showArrowsIfNeeded()
    {
        if(place.getPlacePhotosInfo().size() > 1){
            leftArrow.setVisibility(View.VISIBLE);
            rightArrow.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSnackBarActionClick()
    {
        mainView.findViewById(R.id.loading_panel_place_details_fragment).setVisibility(View.VISIBLE);
        String myTripID = SaveSharedPreference.getTripId(getActivity().getApplicationContext());
        if(placeID != null) {
            googlePlaceLoader = new GooglePlaceLoader(myTripPlacesGoogleIDs, myTripID, PlaceDetailsFragment.this, placeID, getActivity());
            googlePlaceLoader.loadGooglePlace();
        }
    }
}
