package com.trippin.androidtrippin.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.DateUtils;
import com.trippin.androidtrippin.model.GooglePlace;
import com.trippin.androidtrippin.model.GooglePlacesUtils;
import com.trippin.androidtrippin.model.GoogleServerKey;
import com.trippin.androidtrippin.model.MultiDrawable;
import com.trippin.androidtrippin.model.Note;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.PlaceJSONParser;
import com.trippin.androidtrippin.model.Trip;
import com.trippin.androidtrippin.model.TripPlace;
import com.trippin.androidtrippin.trippin.MainActivity;
import com.trippin.androidtrippin.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TripMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripMapFragment extends Fragment implements ClusterManager.OnClusterClickListener<TripPlace>,
        ClusterManager.OnClusterInfoWindowClickListener<TripPlace>,
        ClusterManager.OnClusterItemClickListener<TripPlace>,
        ClusterManager.OnClusterItemInfoWindowClickListener<TripPlace>,
        GoogleMap.OnMapLongClickListener
{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String BUNDLE = "bundle";

    private Bundle myBundle;
    private GoogleMap googleMap; // Might be null if Google Play services APK is not available.
    private UiSettings mapSettings;
    private ParserTask parserTask;
    private static Double destLat;
    private static Double destLng;
    private static float currZoom;
    private Button showAttractionsButton;
    private Button similarTripsButton;
    private TextView similarTripModeTV;
    private RelativeLayout fragMainLayout;
    private View mainView;
    private Trip currTrip;
    private GoogleApiClient mGoogleApiClient;
    private ClusterManager<TripPlace> mClusterManager;
    private OnFragmentInteractionListener mListener;
    private float zoom;

    //rating data members
    private RelativeLayout ratingRL;
    private Button noThanksButton;
    private Button rateButton;
    private RatingBar mRatingBar;
    private Dialog rateDialog;

    //AutoCompleteTextView Data Members
    private AutoCompleteTextView actvPlaces;
    private GooglePlace actvChosenPlace = new GooglePlace();
    private TripPlace addedPlace;

    private GooglePlace longMapClickPlace = new GooglePlace();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Parameter 1.
     * @return A new instance of fragment TripMapFragment.
     */
    public static TripMapFragment newInstance(Bundle bundle)
    {
        TripMapFragment fragment = new TripMapFragment();
        Bundle args = new Bundle();
        args.putBundle(BUNDLE, bundle);
        fragment.setArguments(args);
        return fragment;
    }

    public TripMapFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            myBundle = getArguments().getBundle(BUNDLE);
        }

        System.out.println("was in oncreate fragment map");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        mainView = inflater.inflate(R.layout.fragment_trip_map, container, false);

        System.out.println("In onCreateView Map Fragment");

        setUpMapIfNeeded();
        getUIComponents();
        setUIComponentsListeners();


        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();


        if (googleMap != null) {
            /* ---INITIALIZATIONS---*/
//            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            mapSettings = googleMap.getUiSettings();
            googleMap.setMyLocationEnabled(true); //todo: get permission from user if target SDK is changed back to >23
            googleMap.setPadding(0, 0, 0, 140);

            mapSettings.setZoomControlsEnabled(true);

            mapSettings.setScrollGesturesEnabled(true);
            mapSettings.setTiltGesturesEnabled(true);
            mapSettings.setRotateGesturesEnabled(true);
            setInfoWindowAdapter();
            initClusteringInMap();

        }

        return mainView;
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case 1: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    googleMap.setMyLocationEnabled(true);
//                    googleMap.setPadding(0, 0, 0, 140);
//                    mapSettings.setZoomControlsEnabled(true);
//
//                    mapSettings.setScrollGesturesEnabled(true);
//                    mapSettings.setTiltGesturesEnabled(true);
//                    mapSettings.setRotateGesturesEnabled(true);
//                    setInfoWindowAdapter();
//                    initClusteringInMap();
//                    break;
//
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                break;
//            }
//
//            case 2: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    clearAndAddClusterItems();
//                    changeCameraPositionIfNeeded(true, zoom);
//                    mClusterManager.cluster();
//                    break;
//
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                break;
//            }
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }

    private void initClusteringInMap()
    {
        mClusterManager = new ClusterManager<TripPlace>(getActivity(), googleMap);
        mClusterManager.setRenderer(new PlaceRenderer());
        googleMap.setOnCameraChangeListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
        googleMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
    }

    private void setAutoCompleteTVListener()
    {
        actvPlaces.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (AppUtils.isNetworkAvailable(getActivity()))
                    showDropDown(s);
                else
                    AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, getActivity(), fragMainLayout);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        actvPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId)
            {
                HashMap<String, String> resultHM = (HashMap<String, String>) parent.getItemAtPosition(position);
                Boolean fromAutoComplete = true;

                final String placeID = resultHM.get("_id");
                removeActvPlaceMarkerIfExists();
                getPlaceDetailsAndZoomPlaceIfNeeded(placeID, fromAutoComplete);
                AppUtils.hideKeyboard(getActivity(), mainView.getWindowToken());
            }
        });
    }

    private void removeActvPlaceMarkerIfExists()
    {
        if(addedPlace != null) {
            clearAndAddClusterItems();
            mClusterManager.cluster();
        }
    }

    private void getPlaceDetailsAndZoomPlaceIfNeeded(final String placeID, final boolean fromAutoComplete)
    {
        new AsyncTask()
        {
            String results;

            @Override
            protected Object doInBackground(Object[] params)
            {
                if(fromAutoComplete) {
                    String placeDetailsRequestURL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeID + "&key=" + GoogleServerKey.GOOGLE_SERVER_KEY;
                    results = GooglePlacesUtils.makeCall(placeDetailsRequestURL);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                Double lat = 0.0;
                Double lng = 0.0;

                super.onPostExecute(o);

                actvChosenPlace = GooglePlacesUtils.parseGooglePlacesDetailsJson(results);
                if (actvChosenPlace != null) {
                    lat = actvChosenPlace.getLatitude();
                    lng = actvChosenPlace.getLongitude();
                    actvPlaces.dismissDropDown();
                    actvPlaces.clearComposingText();
                    zoomToChosenActvPlace(lat, lng);
                    createTripPlaceAndAddToCluster(lat, lng, fromAutoComplete);
                }
            }
        }.execute();
    }

    private void  showDropDown(CharSequence s)
    {
        final String sAsString = s.toString();
        LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
        destLat = bounds.getCenter().latitude;
        destLng = bounds.getCenter().longitude;

        new AsyncTask(){

            @Override
            protected Object doInBackground(Object[] params)
            {
                new PlacesTask().execute(sAsString.toString());
                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                actvPlaces.showDropDown();
            }
        }.execute();
    }

    private void zoomToChosenActvPlace(Double lat, Double lng)
    {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 16.0f), 3000, null);
        actvPlaces.setText("");
    }

    private void createTripPlaceAndAddToCluster(Double lat, Double lng, boolean fromAutoComplete)
    {
        String placeName;
        String placeID;
        String serverID;
        Note note = new Note("","","","");

        if(fromAutoComplete) {
            placeName = actvChosenPlace.getName();
            placeID = actvChosenPlace.getPlaceID();
            serverID = "fakeServerID";
        } else {
            placeName = "New Place";
            placeID = "tempPlaceID";
            serverID = "longClickServerID";
        }
        addedPlace = new TripPlace(placeName, placeID, lat, lng, serverID, null, note);
        if(!isPlaceAlreadyInTrip()) {
            mClusterManager.addItem(addedPlace);
            mClusterManager.cluster();
        }
    }

    private boolean isPlaceAlreadyInTrip()
    {
        boolean isPlaceAlreadyInTrip = false;

        for(int i = 0 ; i < currTrip.getNumOfTripPlaces() ; i++)
        {
            if(currTrip.getTripPlace(i).getPlaceID().equals(addedPlace.getPlaceID()))
                isPlaceAlreadyInTrip = true;
        }

        return isPlaceAlreadyInTrip;
    }

    private void handleLongClickOnMap(final Double lat, final Double lng, final Boolean fromAutoComplete)
    {
                longMapClickPlace.setLatitude(lat);
                longMapClickPlace.setLongitude(lng);
                createTripPlaceAndAddToCluster(lat, lng, fromAutoComplete);
    }

//    private void addLongClickPlaceToCluster(String placeID, Boolean fromAutoComplete)
//    {
//        Double lat = longMapClickPlace.getLatitude();
//        Double lng = longMapClickPlace.getLongitude();
//        BitmapDescriptor trippinIcon = BitmapDescriptorFactory.fromResource(R.drawable.trippin_marker);
//
//        Marker marker = googleMap.addMarker(new MarkerOptions().
//                position(new LatLng(lat,lng))
//                .title("New Place")
//                .snippet("7,456 Trippers have been here")
//                .icon(trippinIcon)
//        );
//    }

    public void handleCallFromNewTripForm(Trip currTrip){
        this.currTrip = currTrip;
        this.destLat = currTrip.getMainLat();
        this.destLng = currTrip.getMainLng();
        currZoom = getZoomAccordingToDestination();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(destLat, destLng), currZoom));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    public void handleCallFromPlaceDetailsActivity(Trip currTrip, boolean added, float zoom)
    {
        this.currTrip = currTrip;
//        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        clearAndAddClusterItems();
        changeCameraPositionIfNeeded(added, zoom);
        mClusterManager.cluster();
    }

    private void clearAndAddClusterItems()
    {
        mClusterManager.clearItems();
        for (int i = 0; i < currTrip.getNumOfTripPlaces(); i++)
        {
            mClusterManager.addItem(currTrip.getTripPlace(i));
        }
    }


    public void handleCallFromHomeActivity(Trip currTrip)
    {
        this.currTrip = currTrip;
        if (currTrip.getNumOfTripPlaces() == 0) {
            handleCallFromNewTripForm(currTrip); // ***NEED TO ADD mainLat, mainLng TO TRIP***
        }
        else {
            zoom = getZoomAccordingToDestination();
            handleCallFromPlaceDetailsActivity(currTrip, true, zoom);
        }
    }

    private void changeCameraPositionIfNeeded(boolean needed, float zoom)
    {
        if (needed)
        {
            int lastPlaceInd = currTrip.getNumOfTripPlaces() - 1;
            if (zoom > 0.0f)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currTrip.getTripPlace(lastPlaceInd).getPosition(), zoom), 3000, null);
        }
    }

    private void setInfoWindowAdapter()
    {
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker)
            {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker)
            {
                View v = getActivity().getLayoutInflater().inflate(R.layout.custom_infowindow, null);
                TextView placeNameTv = (TextView)v.findViewById(R.id.place_name_frag_tv);
                if(marker.getTitle().equals(AppConstants.EMPTY_STRING))
                    placeNameTv.setText("New Place");
                else
                    placeNameTv.setText(marker.getTitle());

                return v;
            }
        });
    }

    private void getUIComponents()
    {
        actvPlaces = (AutoCompleteTextView)mainView.findViewById(R.id.actv_places);
        showAttractionsButton = (Button) mainView.findViewById(R.id.nearby_places_button_map_frag);
        similarTripsButton = (Button) mainView.findViewById(R.id.similar_trips_button_frag);
        similarTripModeTV = (TextView) mainView.findViewById(R.id.similar_trip_mode_TV);
        fragMainLayout = (RelativeLayout)mainView.findViewById(R.id.plan_trip_relative_layout_frag);
    }

    private void setUIComponentsListeners()
    {
        googleMap.setOnMapLongClickListener(this);

        mainView.post(new Runnable()
        {
            @Override
            public void run()
            {
                setAutoCompleteTVListener();
            }
        });

        showAttractionsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickAttractionsNearby();
            }
        });

        similarTripsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickSimilarTrips();
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                AppUtils.hideKeyboard(getActivity(), mainView.getWindowToken());
            }
        });
    }

    @Override
    public void onMapLongClick(LatLng point) {
//        BitmapDescriptor trippinIcon = BitmapDescriptorFactory.fromResource(R.drawable.trippin_marker);
//        googleMap.addMarker(new MarkerOptions().position(point).icon(trippinIcon));
        Boolean fromAutoComplete = false;
        handleLongClickOnMap(point.latitude, point.longitude, fromAutoComplete);
    }

    private void onClickSimilarTrips()
    {
        System.out.println("onClick similar Trips!");
        Bundle bundle = new Bundle();

//        bundle.putParcelable("currTrip", currTrip);
        bundle.putString("dest", currTrip.getDestination());
        bundle.putString("depDate", DateUtils.dateToString(currTrip.getDepartureDate()));
        bundle.putString("retDate", DateUtils.dateToString(currTrip.getReturnDate()));
        bundle.putString("type", currTrip.getType().strValue());
        bundle.putString("goingWith", currTrip.getGoingWith().strValue());
        mListener.onFragmentInteraction("switchToSimilarTripsActivity", bundle);
    }

    private void onClickAttractionsNearby()
    {
        Bundle bundle = new Bundle();

        LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
        destLat = bounds.getCenter().latitude;
        destLng = bounds.getCenter().longitude;
        currZoom = googleMap.getCameraPosition().zoom;

        bundle.putDouble("lat", destLat);
        bundle.putDouble("lng", destLng);
        bundle.putString("trip_id", currTrip.getID());
        bundle.putString("caller", "PlanTripActivity");
        //bundle.putParcelable("tripObject", currTrip);

        mListener.onFragmentInteraction("switchToNearbyPlacesActivity", bundle);
    }

    private float getZoomAccordingToDestination()
    {
        float zoom = AppConstants.ZOOM_SIX;

        if (currTrip != null)
        {
            ArrayList<String> countries = AppUtils.getCountriesList();
            if (countries.contains(currTrip.getDestination()))
            {
                if (isLargeCountry(currTrip.getDestination()))
                    zoom = AppConstants.ZOOM_LARGE_COUNTRY;
                else
                    zoom = AppConstants.ZOOM_COUNTRY;
            }
            else
            {
                zoom = AppConstants.ZOOM_CITY;
            }
        }

        return zoom;
    }

    private boolean isLargeCountry(String destination)
    {
        boolean isLarge;

        switch (destination.toLowerCase())
        {
            case "united states":
            case "russia":
            case "canada":
            case "china":
            case "australia":
            case "brazil":
            case "argentina":
                isLarge = true;
                break;
            default:
                isLarge = false;
                break;
        }

        return isLarge;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        //MapsInitializer.initialize(getActivity());

        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment f = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map_frag);
            googleMap = f.getMap();

            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                //setUpMap();
//                mListener.onFragmentInteraction("showTripMap", null);
            } else {
               //Toast.makeText(this, "Couldn't Load Google Map", Toast.LENGTH_LONG).show();
                System.out.println("Couldn't load Google Map");
            }
        }
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
    public void onResume()
    {
        super.onResume();
        System.out.println("In onResume Map Fragment");

        setUpMapIfNeeded();
        if (googleMap != null)
            mListener.onFragmentInteraction("showTripMap", (Object)null);
        actvPlaces.setText("");
        showAttractionsButton.setText("Nearby Places");
        similarTripsButton.setText("Similar Trips");
        removeActvPlaceMarkerIfExists();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        //super.onViewCreated(view, savedInstanceState);

        if(googleMap != null)
//            mListener.onFragmentInteraction("showTripMap", null);

        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.map_frag)).getMap();

            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                //setUpMap();
//                mListener.onFragmentInteraction("showTripMap", null);
            } else {
                //Toast.makeText(this, "Couldn't Load Google Map", Toast.LENGTH_LONG).show();
                System.out.println("Couldn't load Google Map");
            }
        }

    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public void hideNearbyAndSimilarButtons()
    {
        showAttractionsButton.setVisibility(View.GONE);
        similarTripsButton.setVisibility(View.GONE);
    }

    public void showSimilarTripModeBar()
    {
        similarTripModeTV.setVisibility(View.VISIBLE);
        similarTripModeTV.setClickable(true);
        setBackToMyTripListener();
    }

    private void setBackToMyTripListener()
    {
        similarTripModeTV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                rateOtherUserTrip();
            }
        });
    }

    private void sendRatingToServer()
    {
        float rating = mRatingBar.getRating();
        String url = AppConstants.SERVER_URL + AppConstants.UPDATE_TRIP_RATING;
        JSONObject ratingJSON = new JSONObject();
        //String url = "http://192.168.203.2:3000/updateTripRating";

        try {
            ratingJSON.put("tripID", currTrip.getID());
            ratingJSON.put("tripRating", rating);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, ratingJSON, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleSendRatingResponse(response);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("error response on sendRatingToServer()");
                        }
                    });
            MainActivity.addRequestToQueue(jsObjRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("Rating of " + currTrip.getName() + ": " + rating);
    }

    private void handleSendRatingResponse(JSONObject response)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    currTrip.setRating(Float.parseFloat(response.optString("rating")));
                    rateDialog.dismiss();
                    returnToUserTrip();
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    System.out.println("Couldn't update trip rating. Server error.");
                    break;
                default:
                    System.out.println("error on handleMainPhotoResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void rateOtherUserTrip()
    {
        openRatingDialog();
    }

    private void openRatingDialog()
    {
        rateDialog = new Dialog(getActivity(), R.style.FullHeightDialog);
        rateDialog.setContentView(R.layout.rating_dialog);
        rateDialog.setCancelable(true);
        mRatingBar = (RatingBar)rateDialog.findViewById(R.id.dialog_ratingbar);

        LayerDrawable stars = (LayerDrawable) mRatingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        rateButton = (Button) rateDialog.findViewById(R.id.rank_dialog_button);
        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRatingToServer();
            }
        });
        //now that the dialog is set up, it's time to show it
        rateDialog.show();
    }

    private void returnToUserTrip()
    {
        mListener.onFragmentInteraction("switchToPlanTripActivity", (Object[]) null);
    }

    public void drawNewPlaceOnMap(TripPlace place)
    {
        mClusterManager.addItem(place);
        mClusterManager.cluster();
    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            //AIzaSyAD9nllP0DMAuh-HD0WXz-WwxcVim1W5nU
            String key = "key="+ GoogleServerKey.GOOGLE_SERVER_KEY;

            String input="";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }


            // place type to be searched
            //String types = "types=geocode";

            // Sensor enabled
            String sensor = "&sensor=false";
            //String radius = "radius=5000";
            //String types = "(regions)|(cities)";

            // Building the parameters to the web service
            String parameters = input+"&"+"location=" + destLat + "," + destLng + "&radius=2000" +  "&"+key+sensor;  //"&"+types+

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

            try{
                // Fetching the data from web service in background
                data = downloadUrl(url);
                System.out.println("!!!!!!!!" + data.toString());
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }

    @Override
    public boolean onClusterClick(Cluster<TripPlace> cluster) {
        // Show a toast with some info when the cluster is clicked.
//        CameraUpdate zoom = CameraUpdateFactory.zoomTo(googleMap.getCameraPosition().zoom + 1);
//        CameraUpdate center = CameraUpdateFactory.newLatLng(cluster.getPosition());
//        googleMap.moveCamera(center);
//        googleMap.animateCamera(zoom, 3000, new GoogleMap.CancelableCallback() {
//            @Override
//            public void onFinish() {
//
//            }
//
//            @Override
//            public void onCancel() {
//
//            }
//        });

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), googleMap.getCameraPosition().zoom + 1), 500, null);

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<TripPlace> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(TripPlace item)
    {
        Collection<Marker> markersList = mClusterManager.getMarkerCollection().getMarkers();
        Marker marker = null;
        for (Marker m : markersList)
        {
            if (m.getPosition().latitude == item.getLatitude() && m.getPosition().longitude == item.getLongitude()) {
                marker = m;
                break;
            }
        }

        if (marker != null)
        {
            marker.showInfoWindow();
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(item.getLatitude(), item.getLongitude())), 1000, null);
        }

        return true;
    }

    @Override
    public void onClusterItemInfoWindowClick(TripPlace place)
    {

        if(place.getServerID().equals("fakeServerID"))
            switchToPlaceDetailsActivity(place);
        else if(place.getServerID().equals("longClickServerID"))
            switchToLongClickPlaceDetailsActivity(place);
        else
            switchToTripPlaceActivity(place);
    }

    private void switchToLongClickPlaceDetailsActivity(TripPlace place)
    {
        Bundle bundle = new Bundle();

        bundle.putString("caller", "longClickOnMap");
        bundle.putDouble("lat", place.getLatitude());
        bundle.putDouble("lng", place.getLongitude());
        bundle.putString("trip_id", currTrip.getID());
        mListener.onFragmentInteraction("switchToLongClickActivity", bundle);
    }

    private void switchToPlaceDetailsActivity(TripPlace place)
    {
        Bundle bundle = new Bundle();

        bundle.putString("caller", "autoCompletePlace");
        bundle.putString("place_id", place.getPlaceID());
        bundle.putBoolean("addPlace", true);
        bundle.putString("trip_id", currTrip.getID());
        mListener.onFragmentInteraction("switchToPlaceDetails", bundle);
    }

    private void switchToTripPlaceActivity(TripPlace place)
    {
        System.out.println("Cluster marker info window clicked!");
        Bundle bundle = new Bundle();
        bundle.putString("caller", "InfoWindowClick");
        bundle.putString("place_id", place.getPlaceID());
        bundle.putString("place_name", place.getName());
        bundle.putString("place_server_id", place.getServerID());
        bundle.putString("note_title", place.getNote().getNoteTitle());
        bundle.putString("note_date", place.getNote().getNoteDate());
        bundle.putString("note_text", place.getNote().getNoteText());
        bundle.putString("note_id", place.getNote().getNoteID());
        mListener.onFragmentInteraction("switchToTripPlaceActivity", bundle);
    }

    private class PlaceRenderer extends DefaultClusterRenderer<TripPlace>
    {
        private final IconGenerator mIconGenerator = new IconGenerator(getActivity().getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity().getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PlaceRenderer()
        {
            super(getActivity().getApplicationContext(), googleMap, mClusterManager);

            View multiProfile = getActivity().getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getActivity().getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_place_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_place_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(TripPlace place, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            if(place.getMainPhotoBitmap() == null)
            {
                mImageView.setImageResource(R.drawable.trippin_marker2);
            }
            else
            {
                mImageView.setImageBitmap(place.getMainPhotoBitmap());
            }

            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(place.getName());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<TripPlace> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (TripPlace p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = new BitmapDrawable(getResources(),p.getMainPhotoBitmap());
                if (p.getMainPhotoBitmap() == null)
                    drawable = getResources().getDrawable(R.drawable.trippin_marker2);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }

            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }


    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);


                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result)

        {

            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };


            if(getActivity() != null){
                // Creating a SimpleAdapter for the AutoCompleteTextView
                SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

                // Setting the adapter
                actvPlaces.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }


    }
}
