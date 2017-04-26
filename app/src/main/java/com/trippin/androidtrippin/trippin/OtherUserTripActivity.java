package com.trippin.androidtrippin.trippin;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.fragments.TripMapFragment;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.LoadTripUtils;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.Trip;

import org.json.JSONException;
import org.json.JSONObject;


public class OtherUserTripActivity extends MyActionBarActivity implements OnFragmentInteractionListener
{
    private TripMapFragment tripMapFragment;
    private FragmentManager fragmentManager;
    private Intent myIntent;
    private Trip currTrip;


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
        System.out.println("In onCreate OtherUserTripActivity");

        fragmentManager = getSupportFragmentManager();
        myIntent = getIntent();

        if(myIntent.getExtras() != null)
        {
            displayMapFragment();
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

    private void displayMapFragment()
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
                showOtherUserTripMap();
            }

        }.execute();

    }

    private void showOtherUserTripMap()
    {
        String tripID = myIntent.getExtras().getString("tripID");

        tripMapFragment.hideNearbyAndSimilarButtons();
        tripMapFragment.showSimilarTripModeBar();
        loadOtherUserTripFromServer(tripID);
    }



    private void loadOtherUserTripFromServer(String tripID)
    {
        JSONObject tripJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.GET_TRIP;
        //String url = "http://10.0.0.5:3000/getTrip";

        try
        {
            tripJSON.put("tripID", tripID);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    onGetTripResponse(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on loadOtherUserTrip");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onGetTripResponse(JSONObject response)
    {
        try {
            switch(response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    currTrip = LoadTripUtils.parseOneTripFromJson(response);
                    setActionBarTitle(currTrip.getName());
                    LoadTripUtils.requestTripPlacesFromServer(currTrip, this, tripMapFragment);
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    Toast.makeText(this, "Couldn't get similar trips. server Error", Toast.LENGTH_LONG).show();
                    break;
                default:
                    System.out.println("error handleLoginResponse()");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_other_user_trip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    @Override
    public void onFragmentInteraction(String event, Object...objs)
    {
        switch (event)
        {
            case "switchToTripPlaceActivity":
                Bundle bundle = (Bundle)objs[0];
                switchToTripPlaceActivity(bundle);
                break;
            case "switchToPlanTripActivity":
                switchToPlanTripActivity();
        }
    }

    private void switchToPlanTripActivity()
    {
        Bundle bundle = new Bundle();
        bundle.putString("caller", "OtherUserTripActivity");

        Intent intent = new Intent(this, PlanTripActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }

    private void switchToTripPlaceActivity(Bundle bundle)
    {
        bundle.putString("trip_id", currTrip.getID());
        bundle.putString("caller", "OtherUserTripActivity");
        Intent intent = new Intent(this, TripPlaceActivity.class);
        intent.putExtras(bundle);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
