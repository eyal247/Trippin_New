package com.trippin.androidtrippin.trippin;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.adapters.TripPlaceFragmentPagerAdapter;
import com.trippin.androidtrippin.fragments.MyPlacePhotosFragment;
import com.trippin.androidtrippin.fragments.NoteFragment;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.Note;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.OnSnackBarActionClickListener;
import com.trippin.androidtrippin.model.SaveSharedPreference;
import com.trippin.androidtrippin.model.TripPlaceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;


public class TripPlaceActivity extends MyActionBarActivity implements OnFragmentInteractionListener, OnSnackBarActionClickListener
{
    private TripPlaceFragmentPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private ActionBar.TabListener tabListener;
    private ActionBar actionBar;
    private String placeServerID = null;
    private NoteFragment noteFragment;
    private Note note;
    private RelativeLayout tripPlaceActivityMainLayout;
    private final OnSnackBarActionClickListener snackBarListener = this;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_place);
        getMembersReference();
        initTabsIfInternetConnected();
    }

    private void initTabsIfInternetConnected()
    {
        if(AppUtils.isNetworkAvailable(this)) {
            initPager();
            initActionBarTabs();
        }
        else
            AppUtils.showSnackBarMsgWithAction(TripPlaceActivity.this, tripPlaceActivityMainLayout, snackBarListener);

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

    private void getMembersReference()
    {
        actionBar = getSupportActionBar();
        pagerAdapter = new TripPlaceFragmentPagerAdapter(getSupportFragmentManager());
        tripPlaceActivityMainLayout = (RelativeLayout)findViewById(R.id.trip_place_activity_main_layout);
        pagerAdapter.setPlaceID(getPlaceID());
        pagerAdapter.setPlaceName(getPlaceName());
        pagerAdapter.setNoteTitle(getNoteTitle());
        pagerAdapter.setNoteDate(getNoteDate());
        pagerAdapter.setNoteText(getNoteText());
        pagerAdapter.setNoteID(getNoteID());
        getPlaceServerIDFromBundle();
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        setActivityTitle();
    }

    private String getNoteID()
    {
        String noteID = null;

        if (getIntent().getExtras() != null)
            noteID = getIntent().getExtras().getString("note_id");

        return noteID;
    }

    private void getPlaceServerIDFromBundle()
    {
        String serverID = null;

        if (getIntent().getExtras() != null)
            serverID = getIntent().getExtras().getString("place_server_id");

        this.placeServerID = serverID;
    }

    private boolean getIsPlaceSelfAddedByUser()
    {
        boolean placeSelfAddedByUser;

        placeSelfAddedByUser = getIntent().getExtras().getBoolean("place_self_added_by_user");

        return placeSelfAddedByUser;
    }

    public String getPlaceServerID()
    {
        return placeServerID;
    }

    private void setActivityTitle()
    {
        setActionBarTitle(pagerAdapter.getPlaceName());
    }

    private String getPlaceID()
    {
        String placeId = null;

        if (getIntent().getExtras() != null)
            placeId = getIntent().getExtras().getString("place_id");

        return placeId;
    }

    private String getPlaceName()
    {
        String placeName = null;

        if (getIntent().getExtras() != null)
            placeName = getIntent().getExtras().getString("place_name");

        return placeName;
    }

    private String getNoteTitle()
    {
        String noteTitle = null;

        if (getIntent().getExtras() != null)
            noteTitle = getIntent().getExtras().getString("note_title");

        return noteTitle;
    }

    private String getNoteDate()
    {
        String noteDate = null;

        if (getIntent().getExtras() != null)
            noteDate = getIntent().getExtras().getString("note_date");

        return noteDate;
    }

    private String getNoteText()
    {
        String noteText = null;

        if (getIntent().getExtras() != null)
            noteText = getIntent().getExtras().getString("note_text");

        return noteText;
    }

    private void initActionBarTabs()
    {
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        initTabListener();
        addTabsToActionBar();
    }

    private void initTabListener()
    {
        tabListener = new ActionBar.TabListener()
        {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // when selecting a tab
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
        actionBar.addTab(actionBar.newTab().setText("Details").setIcon(R.drawable.ic_place_white_24dp).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Photos").setIcon(R.drawable.ic_image_white_24dp).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Notes").setIcon(R.drawable.ic_note_add_white_24dp).setTabListener(tabListener));
    }

    private void initPager()
    {
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
                    public void onPageSelected(int position)
                    {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getSupportActionBar().setSelectedNavigationItem(position);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trip_place, menu);
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
//        if (id == R.id.action_settings) {
//            return true;
//        }

        if (id == android.R.id.home)
        {
            Intent intent = getIntent();
            System.out.println(intent);
//            Intent intent = NavUtils.getParentActivityIntent(this);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            NavUtils.navigateUpTo(this, intent);
            onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        String imageID = null;
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (0) :
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    if (data.hasExtra("imageID"))
                        imageID = data.getStringExtra("imageID");
                    updateGridViewAfterDelete(imageID);
                }
                break;
            }
        }
    }

    private void updateGridViewAfterDelete(String imageID)
    {
        if (pagerAdapter.getMyPlacePhotosFragment() instanceof MyPlacePhotosFragment)
        {
            MyPlacePhotosFragment fragment = (MyPlacePhotosFragment) pagerAdapter.getMyPlacePhotosFragment();
            fragment.updateGridViewAfterDelete(imageID);
        }
    }

    @Override
    public void onFragmentInteraction(String event, Object...objs)
    {
        switch(event)
        {
            case "deletePlace":
                deletePlaceFromTrip();
                break;
            case "saveNote":
                this.note = (Note)objs[0];
                sendNoteToServer();
                break;
            case "addToMyTrip":
                handleAddToMyTrip((Bundle) objs[0]);
                break;
            case "switchToImageActivity":
                switchToImageActivity((ImageView) objs[0], (String) objs[1], (Integer) objs[2]);
                break;
            default:
                System.out.println("TripPlaceActivity switch/case error");
        }
    }

    private void switchToImageActivity(ImageView iv, String imageID, Integer position)
    {
        iv.setTransitionName("robot");
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        // create the transition animation - the images in the layouts
        // of both activities are defined with android:transitionName="robot"
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, iv, "robot");
        // start the new activity
        Bitmap bitmap =  ((BitmapDrawable)iv.getDrawable()).getBitmap();
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bs);
        intent.putExtra("byteArray", bs.toByteArray());
        intent.putExtra("imageID", imageID);
        intent.putExtra("placeServerID", placeServerID);
        intent.putExtra("position", position);
        intent.putExtra("caller", getIntent().getExtras().getString("caller"));
        startActivityForResult(intent, 0, options.toBundle());
    }


    private void handleAddToMyTrip(Bundle bundle)
    {
        bundle.putString("trip_id", SaveSharedPreference.getTripId(this));
        bundle.putString("caller", "TripPlaceActivity");
        TripPlaceUtils.sendTripPlaceToServer(bundle, this);
    }

    private void deletePlaceFromTrip()
    {
        sendDeleteTripPlaceRequestToServer();
        //startTripMapActivity();
    }

    private void sendDeleteTripPlaceRequestToServer()
    {
        JSONObject deletePlaceJSON;
        String url = AppConstants.SERVER_URL + AppConstants.DELETE_PLACE_FROM_TRIP;
        //String url = "http://192.168.206.86:3000/deleteTripPlace";

        try
        {
            deletePlaceJSON = new JSONObject();
            deletePlaceJSON.put("tripPlaceID", this.placeServerID);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, deletePlaceJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    handleDeleteTripPlaceResponse(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on sendDeleteTripPlaceRequestToServer()");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteTripPlaceResponse(JSONObject response)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    startTripMapActivity();
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    Toast.makeText(this, "Couldn't delete place from trip. Server error.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    System.out.println("error on handleDeleteTripPlaceResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startTripMapActivity()
    {
        Bundle bundle = new Bundle();
        bundle.putString("place_server_id", this.placeServerID);
        bundle.putString("caller", "TripPlaceActivity");
        bundle.putBoolean("addToMyTrip", false);
        Intent intent = new Intent(TripPlaceActivity.this, PlanTripActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void sendNoteToServer()
    {
        JSONObject noteJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.UPDATE_PLACE_NOTE;
        //String url = "http://192.168.205.237:3000/updatePlaceNote";

        try
        {
            noteJSON.put("noteText", note.getNoteText());
            noteJSON.put("noteDate", note.getNoteDate());
            noteJSON.put("noteTitle", note.getNoteTitle());
            noteJSON.put("noteID", note.getNoteID());

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, noteJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    handleSaveNoteResponse(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on sendNoteToServer()");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleSaveNoteResponse(JSONObject response)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    System.out.println("##### Successfully saved Note #####");
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    //Toast.makeText(this, "Server Error. Couldn't save note", Toast.LENGTH_LONG).show();
                    System.out.println("##### failed to save Note #####");
                    break;
                default:
                    System.out.println("error on handleSaveNoteResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //passing information to note fragment
    public void sendInformationToFragment(Object obj)
    {
        noteFragment.sendInformation(obj);
    }

    @Override
    public void onSnackBarActionClick()
    {
        initTabsIfInternetConnected();
    }
}
