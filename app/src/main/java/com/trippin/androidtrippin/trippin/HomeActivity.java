package com.trippin.androidtrippin.trippin;

import android.content.Intent;
import android.content.res.Configuration;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.trippin.androidtrippin.trippin.NewTripForm;
import com.trippin.androidtrippin.trippin.MyActionBarActivity;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.adapters.NavigationDrawerAdapter;
import com.trippin.androidtrippin.fragments.AboutTrippinFragment;
import com.trippin.androidtrippin.fragments.HomeOfUserFragment;
import com.trippin.androidtrippin.fragments.MyProfileFragment;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.SaveSharedPreference;
import com.trippin.androidtrippin.model.Trip;
import com.trippin.androidtrippin.model.eHomeNavItems;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;

public class HomeActivity extends MyActionBarActivity implements OnFragmentInteractionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private String[] navigationListStrings;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
    private CharSequence fragmentTitle;
    private String username;
    private Fragment homeFragment;
    private Fragment currFragment;
    public static FragmentManager fragmentManager;
    public Boolean isGoogleSignIn;
    public Boolean isFacebookSignIn;
    private static GoogleApiClient mGoogleApiClient;
    private boolean signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (savedInstanceState != null)
        {
            currFragment = getSupportFragmentManager().getFragment(savedInstanceState, "fragment");
        }

        SaveSharedPreference.setCurrTripID(getApplicationContext(), "");
        username = SaveSharedPreference.getUserName(getApplicationContext());
        isGoogleSignIn = (SaveSharedPreference.getIsGoogleSignIn(getApplicationContext()));
        isFacebookSignIn = (SaveSharedPreference.getIsFacebookSignedIn(getApplicationContext()));
        createNavigationDrawer();
        fragmentManager = getSupportFragmentManager();
        showHomeFragment(AppConstants.NOT_FOUND);
        if(isGoogleSignIn && mGoogleApiClient != null) {
            signOut = false;
            AppController.getInstance().getmGoogleApiClient().connect();
            mGoogleApiClient = AppController.getInstance().getmGoogleApiClient();
        }
        System.out.println("IN HOME ACTIVITY onCreate()");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        //if(getSupportFragmentManager().getFragment(outState, "homeFragment") != null)
            getSupportFragmentManager().putFragment(outState, "fragment", currFragment);
        System.out.println("In onSaveInstanceState in HOME ACTIVITY");
        super.onSaveInstanceState(outState);
    }

    public String getUsername()
    {
        return username;
    }

    private void showHomeFragment(int position)
    {
        if (homeFragment == null)
            homeFragment = new HomeOfUserFragment();

        currFragment = homeFragment;
        fragmentManager.beginTransaction().replace(R.id.content_frame, homeFragment).commit();
        if (position != AppConstants.NOT_FOUND)
        {
            // Highlight the selected item, update the title, and close the drawer
            drawerListView.setItemChecked(position, true);
            setTitle(navigationListStrings[position]);
            drawerLayout.closeDrawer(drawerListView);
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

    private void createNavigationDrawer()
    {
        navigationListStrings = getResources().getStringArray(R.array.home_navigation_list);
        ArrayList<Integer> iconsList = initIconsList();
        ArrayList<String> navStringsList = initNavStringsList();
        NavigationDrawerAdapter navAdapter = new NavigationDrawerAdapter(this, R.layout.drawer_list_item_1, R.id.rowText, navStringsList, iconsList);

        // Navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.left_drawer);
        fragmentTitle = drawerTitle = this.getTitle();

        initDrawerToggle();

        // Set the adapter for the list view and the list items click listener
        drawerListView.setAdapter(navAdapter);
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
        drawerLayout.setDrawerListener(drawerToggle);
        enableNavigationDrawerButton();
    }

    private ArrayList<String> initNavStringsList()
    {
        ArrayList<String> navStrings = new ArrayList<>();

        for (int i = 0; i < AppConstants.NumOfNavListItemsInHomeActivity; i++)
            navStrings.add(navigationListStrings[i]);

        return navStrings;
    }

    private ArrayList<Integer> initIconsList()
    {
        ArrayList<Integer> icons = new ArrayList<>();

        icons.add(R.drawable.ic_home);
        icons.add(R.drawable.ic_account);
        icons.add(R.drawable.ic_info);
        icons.add(R.drawable.ic_logout);

        return icons;
    }

    private void enableNavigationDrawerButton()
    {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    // Opening and closing listeners of NavigationDrawer.
    private void initDrawerToggle()
    {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(fragmentTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
    }

    private void switchToNewTripFormActivity()
    {
        Intent intent = getIntent();
        intent.setClass(this, NewTripForm.class);
        startActivity(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
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
            case "onPlanTripClick":
                switchToNewTripFormActivity();
                break;
            case "onLoadExistingTrip":
                switchToPlanTripActivity((Trip) objs[0]);
                break;
            case "switchToTripSettings":
                switchToTripSettingsActivity((Bundle) objs[0]);
            default:
                System.out.println("onFragmentInteraction switch/case error");
        }
    }

    private void switchToTripSettingsActivity(Bundle bundle)
    {
        Intent intent = new Intent(this, TripSettingsActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK){
            if (requestCode == 1){
                handleCallFromSettingsActivity(data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleCallFromSettingsActivity(Intent data)
    {
        ((HomeOfUserFragment)homeFragment).updateUserTripsList(data.getExtras());
    }

    private void switchToPlanTripActivity(Trip tripToLoad)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("tripObject", tripToLoad);
        bundle.putString("caller", "HomeActivity");

        SaveSharedPreference.setCurrTripID(getApplicationContext(), tripToLoad.getID());
        Intent intent = new Intent(HomeActivity.this, PlanTripActivity.class);
        intent.putExtras(bundle);

        startActivity(intent);
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
        switch(eHomeNavItems.values()[position])
        {
            case HOME:
                showHomeFragment(position);
                break;
            case MY_PROFILE:
                onClickMyProfileNavItem(position);
                break;
            case ABOUT_TRIPPIN:
                onClickAboutTrippinNavItem(position);
                break;
            case LOGOUT:
                onClickLogoutNavItem(position);
                break;
            default:
                System.out.println("handleDrawerItemSelection switch/case error");
                break;
        }
    }

    private void onClickAboutTrippinNavItem(int position)
    {
        Fragment fragment = new AboutTrippinFragment();

        currFragment = fragment;
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer
        drawerListView.setItemChecked(position, true);
        setTitle(navigationListStrings[position]);
        drawerLayout.closeDrawer(drawerListView);
    }

    private void onClickLogoutNavItem(int position)
    {
        SaveSharedPreference.setUserName(getApplicationContext(), "");
        Intent intent = new Intent(this, MainActivity.class);
        if(isGoogleSignIn) {
            signOut = true;
            onSignOutClicked();
            isGoogleSignIn = false;
//            mGoogleApiClient = buildGoogleApiClient();
//            AppController.getInstance().setmGoogleApiClient(mGoogleApiClient);
//
//            AppController.getInstance().getmGoogleApiClient().connect();
        }
        else if (isFacebookSignIn) {
            signOut = true;
            isFacebookSignIn = false;
            LoginManager.getInstance().logOut();
        }
        startActivity(intent);
    }

    private void onClickMyProfileNavItem(int position)
    {
        Fragment fragment = new MyProfileFragment();
        currFragment = fragment;
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer
        drawerListView.setItemChecked(position, true);
        setTitle(navigationListStrings[position]);
        drawerLayout.closeDrawer(drawerListView);
    }

    @Override
    public void setTitle(CharSequence title)
    {
        fragmentTitle = title;
        getSupportActionBar().setTitle(fragmentTitle);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
//        AppController.getInstance().getmGoogleApiClient().connect();
    }

    @Override
    public void onBackPressed()
    {
        //Need to prevent back from home to main activity
    }

    @Override
    protected void onStop()
    {
        super.onStop();
//        if (AppController.getInstance().getmGoogleApiClient().isConnected()) {
//            AppController.getInstance().getmGoogleApiClient().disconnect();
//        }
    }

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

    private void onSignOutClicked()
    {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected() || SaveSharedPreference.getIsGoogleSignIn(getApplicationContext())) {
            Plus.AccountApi.clearDefaultAccount(AppController.getInstance().getmGoogleApiClient());
            revokeAccess();
            SaveSharedPreference.setIsGoogleSignIn(getApplicationContext(), false);
            SaveSharedPreference.setUserName(getApplicationContext(), "");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("googleLogout", true);
        }
        if(isFacebookSignIn) {
            LoginManager.getInstance().logOut();
            isFacebookSignIn = false;
            SaveSharedPreference.setIsFacebookSignIn(getApplicationContext(), false);
        }
    }

    protected void revokeAccess()
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
}
