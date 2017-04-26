package com.trippin.androidtrippin.trippin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.adapters.SimilarTripsListArrayAdapter;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.LoadTripUtils;
import com.trippin.androidtrippin.model.SaveSharedPreference;
import com.trippin.androidtrippin.model.Trip;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


//TODO: Fix bug when going back from similar trip to my trip (in the second time)

public class SimilarTripsActivity extends MyActionBarActivity
{
    private ArrayList<Trip> similarTripsList;
    private ListView similarTripsListView;
    private SimilarTripsListArrayAdapter myAdapter;
    private TextView noSimilarTripsTV;
    private ImageButton refreshSimilarTrips;
    private RelativeLayout activityMainLayout;
    private final ArrayList<Integer> mSelectedItems = new ArrayList<>();  // Where we track the selected items in Filter
    private boolean[] checkedFilterValues = {true, true, true};
    private final Activity thisActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_similar_trips);

        initFilterSelectedItems();
        getUIComponents();
        setUIComponentsListeners();
//        createAndSetFilterSpinnerAdapter();
        createAndSetAdapter();
    }

    private void initFilterSelectedItems()
    {
        mSelectedItems.add(0);
        mSelectedItems.add(1);
        mSelectedItems.add(2);
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

//    private void createAndSetFilterSpinnerAdapter()
//    {
//        // Creating adapter for spinner
//        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(this, R.array.similar_trips_filter_spinner_list, android.R.layout.simple_spinner_dropdown_item);
//        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        // attaching data adapter to spinner
//        filterSpinner.setAdapter(filterAdapter);
//    }

    private void setUIComponentsListeners()
    {
        similarTripsListView.setOnItemClickListener(new SimilarTripsListItemClickListener());

        refreshSimilarTrips.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createAndSetAdapter();
            }
        });
    }

    private void onSimilarityFilterActionClick()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle("Filter Similar Trips By:")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(R.array.similar_trips_filter_spinner_list, checkedFilterValues,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked)
                            {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                    checkedFilterValues[which] = true;
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                    checkedFilterValues[which] = false;
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton("Filter", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                        if(AppUtils.isNetworkAvailable(SimilarTripsActivity.this))
                            requestSimilarTripsWithFilter(mSelectedItems);
                        else{
                            similarTripsListView.setAdapter(null);
                            refreshSimilarTrips.setVisibility(View.VISIBLE);
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void requestSimilarTripsWithFilter(ArrayList<Integer> mSelectedItems)
    {
        JSONObject similarTripsJSON = createSimilarTripsJSON(mSelectedItems);
        requestSimilarTripsFromServer(similarTripsJSON);
    }

    private JSONObject createSimilarTripsJSON(ArrayList<Integer> mSelectedItems)
    {
        Bundle bundle = getIntent().getExtras();
        JSONObject similarTripsJSON = initSimilarTripsJSON();

        try
        {
            if (mSelectedItems.contains(0))
            {
                similarTripsJSON.put("departureDate", bundle.getString("depDate"));
                similarTripsJSON.put("returnDate", bundle.getString("retDate"));
            }
            if (mSelectedItems.contains(1))
            {
                similarTripsJSON.put("goingWith", bundle.getString("goingWith"));
            }
            if (mSelectedItems.contains(2))
            {
                similarTripsJSON.put("type", bundle.getString("type"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return similarTripsJSON;
    }

    private JSONObject initSimilarTripsJSON()
    {
        JSONObject similarTripsJSON = new JSONObject();
        Bundle bundle = getIntent().getExtras();

        try
        {
            similarTripsJSON.put("destination", bundle.getString("dest"));
            similarTripsJSON.put("departureDate", AppConstants.EMPTY_STRING);
            similarTripsJSON.put("returnDate", AppConstants.EMPTY_STRING);
            similarTripsJSON.put("type", AppConstants.EMPTY_STRING);
            similarTripsJSON.put("goingWith", AppConstants.EMPTY_STRING);
            similarTripsJSON.put("username", SaveSharedPreference.getUserName(this));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return similarTripsJSON;
    }

    private void createAndSetAdapter()
    {
        similarTripsList = new ArrayList<>();

        JSONObject similarTripsJSON = createFullSimilarTripsJSON();
        requestSimilarTripsFromServer(similarTripsJSON);
    }

    private JSONObject createFullSimilarTripsJSON()
    {
        JSONObject similarTripsJSON = new JSONObject();
        Bundle bundle = getIntent().getExtras();

        try
        {
            similarTripsJSON.put("destination", bundle.getString("dest"));
            similarTripsJSON.put("departureDate", bundle.getString("depDate"));
            similarTripsJSON.put("returnDate", bundle.getString("retDate"));
            similarTripsJSON.put("type", bundle.getString("type"));
            similarTripsJSON.put("goingWith", bundle.getString("goingWith"));
            similarTripsJSON.put("username", SaveSharedPreference.getUserName(this));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return similarTripsJSON;
    }

    private void requestSimilarTripsFromServer(JSONObject similarTripsJSON)
    {
        String url = AppConstants.SERVER_URL + AppConstants.GET_SIMILAR_TRIPS;
        //String url = "http://192.168.205.224:3000/getSimilarTrips";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, similarTripsJSON, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response) {
                handleSimilarTripsResponse(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                refreshSimilarTrips.setVisibility(View.VISIBLE);
                System.out.println("error response on requestSimilarTripsFromServer()");
            }
        });

        MainActivity.addRequestToQueue(jsObjRequest);
    }

    private void handleSimilarTripsResponse(JSONObject response)
    {
        try {
            switch(response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    refreshSimilarTrips.setVisibility(View.GONE);
                    handleTripsSuccessfulResult(response);
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

    private void handleTripsSuccessfulResult(JSONObject response)
    {
        similarTripsList = LoadTripUtils.parseTripsFromJson(response);
        if (similarTripsList.size() == 0)
            noSimilarTripsTV.setVisibility(View.VISIBLE);
        else
            noSimilarTripsTV.setVisibility(View.GONE);

        myAdapter = new SimilarTripsListArrayAdapter(this, similarTripsList);
        myAdapter.notifyDataSetChanged();
        similarTripsListView.setAdapter(myAdapter);
    }

    private void getUIComponents()
    {
        similarTripsListView = (ListView) findViewById(R.id.similar_trips_list_view);
//        filterSpinner = (Spinner) findViewById(R.id.similar_trips_filter_spinner);
        noSimilarTripsTV = (TextView) findViewById(R.id.no_similar_trips_TV);
        noSimilarTripsTV.setVisibility(View.GONE);
        refreshSimilarTrips = (ImageButton)findViewById(R.id.refresh_similar_trips_IB);
        activityMainLayout = (RelativeLayout)findViewById(R.id.similar_trips_main_layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_similar_trips, menu);
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
        if (id == R.id.action_similar_trips_filter)
        {
            onSimilarityFilterActionClick();
            return true;
        }

        if(id == android.R.id.home){
            Intent intent = NavUtils.getParentActivityIntent(this);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("caller", "SimilarTripsActivity");
            NavUtils.navigateUpTo(this, intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class SimilarTripsListItemClickListener implements android.widget.AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            if(AppUtils.isNetworkAvailable(SimilarTripsActivity.this))
                loadOtherUserTrip(similarTripsList.get(position));
            else
                AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, thisActivity, activityMainLayout);
        }
    }

    private void loadOtherUserTrip(Trip trip)
    {
        String tripID = trip.getID();

        Bundle bundle = new Bundle();
        bundle.putString("tripID", tripID);

        Intent intent = new Intent(this, OtherUserTripActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);

        startActivity(intent);
    }
}
