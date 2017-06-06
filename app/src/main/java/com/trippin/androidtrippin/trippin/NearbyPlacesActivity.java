package com.trippin.androidtrippin.trippin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.adapters.NearbyPlacesListArrayAdapter;
import com.trippin.androidtrippin.adapters.NearbyPlacesTypePagerAdapter;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.GooglePlacesUtils;
import com.trippin.androidtrippin.model.GoogleServerKey;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.OnSnackBarActionClickListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class NearbyPlacesActivity extends MyActionBarActivity implements OnFragmentInteractionListener, OnSnackBarActionClickListener
{
    //private Trip currTrip;
    private ArrayList nearbyPlacesList;
    private ListView nearbyPlacesListView;
    private static Double latitude;
    private static Double longitude;
    private String tripID;
    private NearbyPlacesListArrayAdapter myAdapter;
    private static NearbyPlacesListArrayAdapter myStaticAdapter;
    private List<String> placesNamesList = new ArrayList();
    private List<String> placesIDs = new ArrayList();
    private NearbyPlacesTypePagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private ActionBar.TabListener tabListener;
    private ActionBar actionBar;
    private Integer chosenRadius;
    private boolean tabsInitialized = false;

    private final OnSnackBarActionClickListener snackBarListener = this;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //currTrip = getIntent().getParcelableExtra("tripObject");
        setContentView(R.layout.activity_nearby_places);
        chosenRadius = 500;
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(R.color.white));

        getLatLng();
        getTripID();
        loadPlacesIfInternetConnected();
//        initPager();
//        initActionBarTabs();

//        nearbyPlacesListView.setOnItemClickListener(new NearbyListItemClickListener());

    }

    private void loadPlacesIfInternetConnected()
    {
        RelativeLayout nearbyActivityLayout = (RelativeLayout)findViewById(R.id.nearby_places_activity_main_layout);

        if(AppUtils.isNetworkAvailable(this))
            new NearbyGooglePlacesAsyncTask().execute();
        else {
            showPagerAndUnshowLoading();
            AppUtils.showSnackBarMsgWithAction(NearbyPlacesActivity.this, nearbyActivityLayout, snackBarListener);
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

    private void getTripID()
    {
        if (getIntent().getExtras() != null)
        {
            tripID = getIntent().getExtras().getString("trip_id");
        }
    }

    private void getLatLng()
    {
        if (getIntent().getExtras() != null)
        {
            latitude = getIntent().getExtras().getDouble("lat");
            longitude = getIntent().getExtras().getDouble("lng");
        }
    }

    private void initActionBarTabs()
    {
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        initTabListener();
        addTabsToActionBar();
        tabsInitialized = true;
    }

    private void initTabListener()
    {
        tabListener = new ActionBar.TabListener()
        {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };
    }

    private void addTabsToActionBar()
    {
        // Adding 3 tabs to actionBar
        actionBar.addTab(actionBar.newTab().setText("Attractions").setIcon(R.drawable.ic_local_activity_white_24dp).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Food & Drinks").setIcon(R.drawable.ic_local_dining_white_24dp).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Hotels").setIcon(R.drawable.ic_hotel_white_24dp).setTabListener(tabListener));
    }

    private void initPager()
    {
        pagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        setViewPagerListener();
    }

    private void setViewPagerListener()
    {
        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener()
                {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getSupportActionBar().setSelectedNavigationItem(position);
                    }
                });
    }

    @Override
    public void onFragmentInteraction(String event, Object...objs)
    {
        switch(event){
            case "switchToPlaceDetails":
                switchToPlacesDetailsActivity(objs[0]);
                break;
        }
    }

    private void switchToPlacesDetailsActivity(Object obj)
    {
        Bundle bundle = (Bundle)obj;

        bundle.putString("trip_id", tripID);
        Intent placeDetailsIntent = new Intent(NearbyPlacesActivity.this, PlaceDetailsActivity.class);
        placeDetailsIntent.putExtras(bundle);
        placeDetailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(placeDetailsIntent);
    }

    private void getUIComponents(String accommodationsResults, String foodDrinksResults, String attractionsResults)
    {
//        nearbyPlacesListView = (ListView)findViewById(R.id.nearby_places_list);
        actionBar = getSupportActionBar();
        pagerAdapter = new NearbyPlacesTypePagerAdapter(getSupportFragmentManager());
        pagerAdapter.setLat(latitude);
        pagerAdapter.setLng(longitude);
        pagerAdapter.setAccommodationsResults(accommodationsResults);
        pagerAdapter.setFoodDrinksResults(foodDrinksResults);
        pagerAdapter.setAttractionsResults(attractionsResults);
        viewPager = (ViewPager) findViewById(R.id.nearby_view_pager);
    }



//    private void setAdapterToNearbyPlaceListView(ArrayList<PhotoInfo> photoInfoList)
//    {
//        myAdapter = new NearbyPlacesListArrayAdapter(NearbyPlacesActivity.this, placesNamesList, placesIDs, photoInfoList);
//        myStaticAdapter = myAdapter;
//        nearbyPlacesListView.setAdapter(myAdapter);
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_nearby_places, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            Intent intent = NavUtils.getParentActivityIntent(this);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavUtils.navigateUpTo(this, intent);
            return true;
        }

        if(item.getItemId() == R.id.action_change_nearby_radius)
        {
            onChangeRadiusButtonClick();
        }

        return super.onOptionsItemSelected(item);
    }

    private String getStringTypesForURL(String type)
    {
        String types = null;

        switch (type){
            case "Accommodations":
                types = "lodging|campground";
                break;
            case "Food & Drinks":
                types = "restaurant|cafe|bar|bakery|meal_delivery";//"food|restaurant|bakery|bar|meal_delivery|meal_takeaway|cafe|convenience_store|grocery_or_supermarket|liquor_store";
                break;
            case "Attractions":
                types = "park|museum|church|zoo|aquarium|shopping_mall|art_gallery|city_hall|amusement_park|synagogue|stadium|mosque|casino|night_club";
                break;
            default:
                System.out.println("Places Types error");
                break;
        }

        try {

            // DONT EVER FORGET URLENCODER

            types = URLEncoder.encode(types, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return types;
    }

    private String getResultsFromGooglePlaces(String type){
        String results;

        results = GooglePlacesUtils.makeCall("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=" + chosenRadius+ "&rankby=prominence&type="+ type + "&key=" + GoogleServerKey.GOOGLE_SERVER_KEY);

        return results;
    }

    @Override
    public void onSnackBarActionClick()
    {
        loadPlacesIfInternetConnected();
    }

    private void showLoadingAndUnshowPager()
    {
        this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.nearby_view_pager).setVisibility(View.GONE);
                findViewById(R.id.loading_panel_nearby_places).setVisibility(View.VISIBLE);
            }
        });
    }

    private void showPagerAndUnshowLoading()
    {
        findViewById(R.id.nearby_view_pager).setVisibility(View.VISIBLE);
        findViewById(R.id.loading_panel_nearby_places).setVisibility(View.INVISIBLE);
    }

    public void onChangeRadiusButtonClick() {
        String[] radiusOptions = {"500 M", "1 KM", "2 KM", "5 KM", "10 KM"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_radius_title)
                .setItems(radiusOptions, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        setRadiusMemberFromDialog(which);
                        new NearbyGooglePlacesAsyncTask().execute();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setRadiusMemberFromDialog(int which)
    {
        switch(which)
        {
            case 0:
                chosenRadius = 500;
                break;
            case 1:
                chosenRadius = 1000;
                break;
            case 2:
                chosenRadius = 2000;
                break;
            case 3:
                chosenRadius = 5000;
                break;
            case 4:
                chosenRadius = 10000;
                break;
        }
    }

    private class NearbyGooglePlacesAsyncTask extends AsyncTask
    {
        private String accommodationsResults;
        private String foodDrinksResults;
        private String attractionsResults;

        private String results;

        @Override
        protected Object doInBackground(Object[] params)
        {
            return doInBackgroundHelper();
        }

        protected String doInBackgroundHelper(View... urls)
        {
            showLoadingAndUnshowPager();
//            if(AppUtils.isNetworkAvailable(thisActivty)) {
            accommodationsResults = getResultsFromGooglePlaces(getStringTypesForURL("Accommodations"));
            foodDrinksResults = getResultsFromGooglePlaces(getStringTypesForURL("Food & Drinks"));
            attractionsResults = getResultsFromGooglePlaces(getStringTypesForURL("Attractions"));
//            } else{
//                AppUtils.showSnackBarMsgWithAction(thisActivty, nearbyActivityLayout, snackBarListener);
//                this.cancel(true);
//            }
            // make Call to the url
            //results = GooglePlacesUtils.makeCall("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=10000&rankby=prominence&key=" + GoogleServerKey.GOOGLE_SERVER_KEY);

            //print the call in the console
            //System.out.println("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=10000&rankby=prominence&key=" + GoogleServerKey.GOOGLE_SERVER_KEY);
            return "";
        }

        @Override
        protected void onPostExecute(Object o)
        {
            super.onPostExecute(o);
            onPostExecuteHelper();
        }

        protected void onPostExecuteHelper()
        {
            getUIComponents(accommodationsResults, foodDrinksResults, attractionsResults);
            initPager();
            if(!tabsInitialized)
                initActionBarTabs();

            showPagerAndUnshowLoading();
        }
    }
}



