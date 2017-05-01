package com.trippin.androidtrippin.trippin;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.login.LoginManager;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.adapters.NavigationDrawerAdapter;
import com.trippin.androidtrippin.fragments.TripMapFragment;
import com.trippin.androidtrippin.fragments.TripPlacesListFragment;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.DateUtils;
import com.trippin.androidtrippin.model.LoadTripUtils;
import com.trippin.androidtrippin.model.Note;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.SaveSharedPreference;
import com.trippin.androidtrippin.model.Trip;
import com.trippin.androidtrippin.model.TripPlace;
import com.trippin.androidtrippin.model.ePlanTripNavItems;
import com.trippin.androidtrippin.model.eTravelingWith;
import com.trippin.androidtrippin.model.eTripType;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.plus.Plus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlanTripActivity extends MyActionBarActivity implements OnFragmentInteractionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private Trip currTrip;
    private String[] navigationListStrings;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
    private CharSequence fragmentTitle;
    private Intent myIntent;
    private TripMapFragment tripMapFragment;
    private FragmentManager fragmentManager;
    private TripPlacesListFragment tripPlacesListFragment;
    private static GoogleApiClient mGoogleApiClient;
    private boolean signOut;
    public Boolean isGoogleSignIn;
    public Boolean isFacebookSignIn;
    private RelativeLayout planTripMainLayout;

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState)
    {
        getSupportFragmentManager().putFragment(savedInstanceState, "tripMapFragment", tripMapFragment);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        tripMapFragment = (TripMapFragment)getSupportFragmentManager().getFragment(savedInstanceState, "tripMapFragment");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_trip);
        System.out.println("In onCreate planTripActivity");

        createNavigationDrawer();
        isGoogleSignIn = (SaveSharedPreference.getIsGoogleSignIn(getApplicationContext()));
        isFacebookSignIn = (SaveSharedPreference.getIsFacebookSignedIn(getApplicationContext()));

        fragmentManager = getSupportFragmentManager();
        myIntent = getIntent();

        if(myIntent.getExtras() != null)
        {
            System.out.println("Going to showTripMap()");
            displayMapFragment(AppConstants.NOT_FOUND);
        }
        if(isGoogleSignIn && mGoogleApiClient != null) {
            signOut = false;
            AppController.getInstance().getmGoogleApiClient().connect();
            mGoogleApiClient = AppController.getInstance().getmGoogleApiClient();
        }
        if(isFacebookSignIn)
        {
            signOut = false;
        }
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

    private void displayMapFragment(int position)
    {

        new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] params)
            {
                tripMapFragment = TripMapFragment.newInstance(null);
                fragmentManager.beginTransaction().replace(R.id.plan_trip_relative_layout, tripMapFragment).commit();
                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                super.onPostExecute(o);
                showTripMap();
            }
        }.execute();

        if (position != AppConstants.NOT_FOUND)
        {
            // Highlight the selected item, update the title, and close the drawer
            drawerListView.setItemChecked(position, true);
            setTitle(navigationListStrings[position]);
            drawerLayout.closeDrawer(drawerListView);
        }
    }

    private void showTripMap()
    {
        String caller;

        if (myIntent != null)
        {
            caller = myIntent.getStringExtra("caller");

            if (caller != null)
            {
                switch (caller)
                {
                    case "NewTripForm":
                        handleCallFromNewTripForm(myIntent.getExtras().getDouble("lat"), myIntent.getExtras().getDouble("lng"));
                        break;
                    case "PlaceDetailsActivity":
                        handleCallFromPlaceDetailsActivity();
                        break;
                    case "HomeActivity":
                        handleCallFromHomeActivity((Trip) myIntent.getExtras().getParcelable("tripObject"));
                        break;
                    case "TripPlaceActivity":
                        handleCallFromTripPlaceActivity();
                        break;
                    case "OtherUserTripActivity":
                    case "SimilarTripsActivity":
                        handleCallFromSimilarTrips();
                        break;
                    default:
                        System.out.println("error on showTripMap caller switch/case");
                        break;
                }
            }
            else
            {
                tripMapFragment.handleCallFromPlaceDetailsActivity(currTrip, false, AppConstants.USE_OLD_ZOOM);
            }
        }
    }

    private void handleCallFromSettingsActivity(Intent intent)
    {
        Bundle bundle = intent.getExtras();

        if (bundle != null)
        {
            currTrip.setName(bundle.getString("trip_name"));
            currTrip.setDepartureDate(DateUtils.stringToDate(bundle.getString("trip_departure")));
            currTrip.setReturnDate(DateUtils.stringToDate(bundle.getString("trip_return")));
            currTrip.setTripType(eTripType.fromStringToEnum(bundle.getString("trip_type")));
            currTrip.setTravelingWith(eTravelingWith.fromStringToEnum(bundle.getString("trip_going_with")));
            currTrip.setShare(bundle.getBoolean("share"));
            currTrip.setTripCoverPhotoStr(bundle.getString("trip_photo"));
            setTitle(currTrip.getName());
        }
        else
        {
            System.out.println("settings bundle in PlanTripActivity is NULL!!!");
        }
    }

    private void handleCallFromSimilarTrips()
    {
        JSONObject tripJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.GET_TRIP_PLACES_SERVER_IDS;
        //String url = "http://192.168.206.86:3000/getTripPlacesServerIDs";

        try
        {
            tripJSON.put("tripID", currTrip.getID());

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    onGetTripPlacesServerIDsResponse(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on handleCallFromSimilarTrips");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onGetTripPlacesServerIDsResponse(JSONObject response)
    {
        try {
            switch(response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    parseServerIDs(response);
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    System.out.println("Server error - received failure on onGetTripPlacesServerIDsResponse");
                    break;
                default:
                    System.out.println("error onGetTripPlacesServerIDsResponse()");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseServerIDs(JSONObject response)
    {
        ArrayList<String> myTripPlacesServerIDs = new ArrayList<>();
        JSONArray googleIDsJSONArray;

        if (response.has("placesServerIDs")) {
            try {
                googleIDsJSONArray = response.getJSONArray("placesServerIDs");

                for (int i = 0; i < googleIDsJSONArray.length(); i++) {
                    String currServerID = googleIDsJSONArray.get(i).toString();
                    myTripPlacesServerIDs.add(currServerID);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        addNewTripPlaces(myTripPlacesServerIDs);
    }

    private void addNewTripPlaces(ArrayList<String> myTripPlacesServerIDs)
    {
        ArrayList<String> currTripPlacesServerIDs = getCurrTripPlacesServerIDs();

        for(int i = 0; i < myTripPlacesServerIDs.size(); i++){
            String currPlaceServerID = myTripPlacesServerIDs.get(i);
            if(!isPlaceInMyTrip(currTripPlacesServerIDs, currPlaceServerID)){
                addTripPlaceFromServer(currPlaceServerID);
            }
        }
    }

    private void addTripPlaceFromServer(String placeServerID)
    {
        JSONObject tripJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.GET_TRIP_PLACE;
        //String url = "http://192.168.206.86:3000/getTripPlace";

        try
        {
            tripJSON.put("serverID", placeServerID);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    onGetTripPlaceResponse(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on handleCallFromSimilarTrips");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onGetTripPlaceResponse(JSONObject response)
    {
        planTripMainLayout = (RelativeLayout)findViewById(R.id.plan_trip_relative_layout);

        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    TripPlace place = TripPlace.parsePlaceFromJSON(response);
                    addPlaceToMap(place);
                    break;
                case AppConstants.RESPONSE_FAILURE:
//                    Toast.makeText(this, "Couldn't delete place from trip. Server error.", Toast.LENGTH_LONG).show();
                    AppUtils.showSnackBarMsg("Couldn't delete place from trip. Server error.", this, planTripMainLayout);
                    break;
                default:
                    System.out.println("error on handleDeleteTripPlaceResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addPlaceToMap(TripPlace place)
    {
        currTrip.addTripPlace(place);
        tripMapFragment.drawNewPlaceOnMap(place);
    }

    private ArrayList<String> getCurrTripPlacesServerIDs()
    {
       ArrayList<String> currTripPlacesServerIDs = new ArrayList<>();

       for(int i = 0; i < currTrip.getNumOfTripPlaces(); i++){
           currTripPlacesServerIDs.add(currTrip.getTripPlace(i).getServerID());
       }

       return currTripPlacesServerIDs;
    }

    private boolean isPlaceInMyTrip(ArrayList<String> currTripPlacesServerIDs, String currPlaceServerID)
    {
        return currTripPlacesServerIDs.contains(currPlaceServerID);
    }


    private void handleCallFromTripPlaceActivity()
    {
        Bundle bundle = myIntent.getExtras();

        if (bundle.getBoolean("addToMyTrip") == true)
        {

        }
        else
        {
            String placeServerID = bundle.getString("place_server_id");
            findAndDeleteTripPlace(placeServerID);
            deleteTripPlaceFromPlaceListFragment();
            deleteTripPlaceFromMap();
        }
    }

    private void deleteTripPlaceFromPlaceListFragment()
    {
        if(tripPlacesListFragment != null)
            tripPlacesListFragment.removeTripPlace();
    }

    private void deleteTripPlaceFromMap()
    {
        tripMapFragment.handleCallFromPlaceDetailsActivity(currTrip, false, AppConstants.USE_OLD_ZOOM);
    }

    private void findAndDeleteTripPlace(String placeServerID)
    {
        for (int i = 0; i < currTrip.getNumOfTripPlaces(); i++)
        {
            TripPlace place = currTrip.getTripPlace(i);
            if (place.getServerID().equals(placeServerID))
            {
                currTrip.deleteTripPlace(i);
            }
        }
    }

    private void handleCallFromHomeActivity(Trip tripToLoad)
    {
        // Load all existing trip places to map.
        getIntent().removeExtra("caller");
        currTrip = tripToLoad;
        AppController.getInstance().setCurrTripOnMap(currTrip);
        setActionBarTitle(currTrip.getName());
        fragmentTitle = currTrip.getName();

        LoadTripUtils.requestTripPlacesFromServer(tripToLoad, this, tripMapFragment);
    }

    private void handleCallFromPlaceDetailsActivity()
    {
        TripPlace tripPlaceToAdd;

        setActionBarTitle(currTrip.getName());
        System.out.println("came back from PlaceDetail Activity");
        tripPlaceToAdd = buildTripPlaceToAdd();
        boolean added = currTrip.addTripPlace(tripPlaceToAdd);
        //sendTripToServer(currTrip);

        tripMapFragment.handleCallFromPlaceDetailsActivity(currTrip, added, 13.0f);
    }

    private TripPlace buildTripPlaceToAdd()
    {
        TripPlace tripPlaceToAdd = new TripPlace();
        Bundle bundle;
        Bitmap placeImageBitmap;

        bundle = myIntent.getExtras();
        if (bundle != null)
        {
            String placeName = bundle.getString("place_name");
            Double lat = bundle.getDouble("lat");
            Double lng = bundle.getDouble("lng");
            String serverID = bundle.getString("trip_place_id");
            tripPlaceToAdd.setLatitude(lat);
            tripPlaceToAdd.setLongitude(lng);
            tripPlaceToAdd.setPlaceID(bundle.getString("place_id"));
            tripPlaceToAdd.setName(placeName);
            tripPlaceToAdd.setServerID(serverID);
            placeImageBitmap = AppUtils.stringToBitMap(bundle.getString("image"));
            tripPlaceToAdd.setMainPhotoBitmap(placeImageBitmap);
            tripPlaceToAdd.setNote(new Note(placeName,"","", bundle.getString("note_id")));
        }

        return tripPlaceToAdd;
    }

    private void handleCallFromNewTripForm(double lat, double lng)
    {
        System.out.println("came back from New Trip Form");
        currTrip = myIntent.getParcelableExtra("tripObject");
        AppController.getInstance().setCurrTripOnMap(currTrip);
        setActionBarTitle(currTrip.getName());
        fragmentTitle = currTrip.getName();
        tripMapFragment.handleCallFromNewTripForm(currTrip);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        myIntent = intent;
    }

    // *** NAVIGATION DRAWER INITIALIZE ***

    private void createNavigationDrawer()
    {
        navigationListStrings = getResources().getStringArray(R.array.plan_trip_navigation_list);
        ArrayList<Integer> iconsList = initIconsList();
        ArrayList<String> navStringsList = initNavStringsList(iconsList);
        NavigationDrawerAdapter navAdapter = new NavigationDrawerAdapter(this, R.layout.drawer_list_item_1, R.id.rowText, navStringsList, iconsList);

        // Navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_plan);
        drawerListView = (ListView) findViewById(R.id.left_drawer_plan);
        fragmentTitle = drawerTitle = "";
        initDrawerToggle();

        // Set the adapter for the list view and the list items click listener
        drawerListView.setAdapter(navAdapter);
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
        drawerLayout.setDrawerListener(drawerToggle);
        enableNavigationDrawerButton();
    }

    private ArrayList<String> initNavStringsList(ArrayList<Integer> iconsList)
    {
        ArrayList<String> navStrings = new ArrayList<>();

        for (int i = 0; i < iconsList.size(); i++)
            navStrings.add(navigationListStrings[i]);

        return navStrings;
    }

    private ArrayList<Integer> initIconsList()
    {
        ArrayList<Integer> icons = new ArrayList<>();

        icons.add(R.drawable.ic_map_black_24dp);
        icons.add(R.drawable.ic_place_black_24dp);
        icons.add(R.drawable.ic_settings_black_drawer_24dp);
        icons.add(R.drawable.ic_home);
        icons.add(R.drawable.ic_logout);

        return icons;
    }

    private void enableNavigationDrawerButton()
    {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle.setDrawerIndicatorEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerToggle.syncState();
    }

    // Opening and closing listeners of NavigationDrawer.
    private void initDrawerToggle()
    {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        {
            public void onDrawerClosed(View view) {
                setActionBarTitle(fragmentTitle.toString());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                setActionBarTitle(fragmentTitle.toString());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerListView);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String event, Object...objs)
    {
        switch(event)
        {
            case "switchToTripPlaceActivity":
                switchToActivity("TripPlaceActivity", objs[0]);
                break;
            case "switchToNearbyPlacesActivity":
                switchToActivity("NearbyPlacesActivity", objs[0]);
                break;
            case"showTripMap":
                showTripMap();
                break;
            case "switchToPlaceDetails":
                switchToActivity("PlaceDetailsActivity", objs[0]);
                break;
            case "switchToLongClickActivity":
                switchToActivity("longClickPlaceFormActivity", objs[0]);
                break;
            case "switchToSimilarTripsActivity":
                switchToActivity("SimilarTripsActivity", objs[0]);
                break;
            default:
                System.out.println("PlanTripActivity onFragmentInteraction switch/case error");
        }
    }

    private void switchToActivity(String destActivity, Object obj)
    {
        Bundle bundle = (Bundle)obj;
        bundle.putBoolean("place_self_added_by_user", true);  //TODO !!!
        Intent intent = null;
        bundle.putString("trip_id", currTrip.getID());

        if (bundle != null){
            switch(destActivity) {
                case "TripPlaceActivity":
                    intent = new Intent(this, TripPlaceActivity.class);
                    break;
                case "NearbyPlacesActivity":
                    intent = new Intent(this, NearbyPlacesActivity.class);
                    break;
                case "PlaceDetailsActivity":
                    intent  = new Intent(this, PlaceDetailsActivity.class);
                    break;
                case "SimilarTripsActivity":
                    intent = new Intent(PlanTripActivity.this, SimilarTripsActivity.class);
                    break;
                case "longClickPlaceFormActivity":
                    intent = new Intent(this, LongClickPlaceFormActivity.class);
            }

            intent.putExtras(bundle);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        if (AppController.getInstance().getmGoogleApiClient().isConnected() && signOut == true) {
            onSignOutClicked();
            //AppController.getInstance().setmGoogleApiClient(null);
            //mGoogleApiClient.connect();
        }
    }

    private void onSignOutClicked()
    {
        Plus.AccountApi.clearDefaultAccount(AppController.getInstance().getmGoogleApiClient());
        revokeAccess();
        SaveSharedPreference.setUserName(getApplicationContext(), "");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("googleLogout", true);
    }

    private void revokeAccess()
    {
        Plus.AccountApi.revokeAccessAndDisconnect(AppController.getInstance().getmGoogleApiClient())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        AppController.getInstance().getmGoogleApiClient().disconnect();
                        //mGoogleApiClient.connect();
                        // Clear data and go to login activity
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            handleDrawerItemSelection(position);
        }
    }

    private void handleDrawerItemSelection(int position)
    {
        switch(ePlanTripNavItems.values()[position])
        {
            case MAP:
                switchToMapFragment(position);
                break;
            case MY_PLACES_LIST:
                switchToTripPlacesListFragment(position);
                break;
            case TRIP_SETTINGS:
                switchToTripSettingsActivity(position);
                break;
            case HOME:
                switchToHomeActivity();
                break;
            case LOGOUT:
                onClickLogoutNavItem();
                break;

            default:
                System.out.println("handleDrawerItemSelection PlanTripActivity switch/case error");
        }
    }

    private void switchToTripSettingsActivity(final int position)
    {
        final Bundle bundle = currTrip.createTripDetailsBundle();
        final Intent intent = new Intent(this, TripSettingsActivity.class);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
//                Intent intent = new Intent(PlanTripActivity.this, TripSettingsActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);

//                intent.putExtras(bundle);
//                startActivity(intent);
            }
        }, 200);

        drawerListView.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerListView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK){
            if (requestCode == 1){
                handleCallFromSettingsActivity(data);
            }
        }
    }

    private void switchToMapFragment(int position)
    {
        //fragmentManager.beginTransaction().replace(R.id.plan_trip_relative_layout, tripMapFragment).commit();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.plan_trip_relative_layout, tripMapFragment);
//        transaction.addToBackStack(null);
//        transaction.commit();

       if(tripPlacesListFragment != null){
           fragmentManager.beginTransaction().hide(tripPlacesListFragment).commit();
           fragmentManager.beginTransaction().show(tripMapFragment).commit();
       }

        drawerListView.setItemChecked(position, true);
        setTitle(currTrip.getName());
        drawerLayout.closeDrawer(drawerListView);
    }

    private void switchToTripPlacesListFragment(int position)
    {
        fragmentManager.beginTransaction().hide(tripMapFragment).commit();

        if(tripPlacesListFragment != null) {
            fragmentManager.beginTransaction().remove(tripPlacesListFragment).commit();
        }
//
        tripPlacesListFragment = TripPlacesListFragment.newInstance(currTrip);
        fragmentManager.beginTransaction().add(R.id.plan_trip_relative_layout, tripPlacesListFragment).commit();
        fragmentManager.beginTransaction().show(tripPlacesListFragment).commit();

//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.plan_trip_relative_layout, tripPlacesListFragment);
//        transaction.addToBackStack(null);
//        transaction.commit();

        drawerListView.setItemChecked(position, true);
        setTitle(navigationListStrings[position]);
        drawerLayout.closeDrawer(drawerListView);
    }

    private void switchToHomeActivity()
    {
        AppController.getInstance().setCurrTripOnMap(null);
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void onClickLogoutNavItem()
    {
        Intent mainActivityIntent;
        SaveSharedPreference.setUserName(getApplicationContext(), "");
        AppController.getInstance().setCurrTripOnMap(null);
        mainActivityIntent = new Intent(this, MainActivity.class);


        if(isGoogleSignIn) {
            signOut = true;
            onSignOutClicked();
        } else if(isFacebookSignIn) {
            signOut = true;
            LoginManager.getInstance().logOut();
        }

        startActivity(mainActivityIntent);
    }

    @Override
    public void setTitle(CharSequence title)
    {
        fragmentTitle = title;
        setActionBarTitle(fragmentTitle.toString());
    }



    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #googleMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
//    private void setUpMapIfNeeded() {
//        // Do a null check to confirm that we have not already instantiated the map.
//        if (googleMap == null) {
//            // Try to obtain the map from the SupportMapFragment.
//            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
//
//            // Check if we were successful in obtaining the map.
//            if (googleMap != null) {
//
//                setUpMap();
//            } else {
//                Toast.makeText(this,"Couldn't Load Google Map", Toast.LENGTH_LONG).show();
//            }
//        }
//    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #googleMap} is not null.
     */
//    private void setUpMap() {
////        googleMap.addMarker(new MarkerOptions().position(new LatLng(32.066158, 34.777819))
////                        .title("Hello Google Maps!")
////                        .snippet("subtitle more info")
////                        .icon(BitmapDescriptorFactory.fromAsset("beer2.png"))
////        );
////        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
//    }
}
