package com.trippin.androidtrippin.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.adapters.UserTripsListArrayAdapter;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.DateUtils;
import com.trippin.androidtrippin.model.LoadTripUtils;
import com.trippin.androidtrippin.model.Logger;
import com.trippin.androidtrippin.model.OnAdapterChangeListener;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.OnSnackBarActionClickListener;
import com.trippin.androidtrippin.model.SaveSharedPreference;
import com.trippin.androidtrippin.model.Trip;
import com.trippin.androidtrippin.model.TripPlace;
import com.trippin.androidtrippin.model.eTravelingWith;
import com.trippin.androidtrippin.model.eTripType;
import com.trippin.androidtrippin.trippin.MainActivity;
import com.trippin.androidtrippin.R;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeOfUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeOfUserFragment extends Fragment implements OnAdapterChangeListener, OnSnackBarActionClickListener
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //*****************
    private LinearLayout planTripButton;
    private FrameLayout homeFragmentMainLayout;
    private ArrayList<Trip> userTrips;
    private ListView userTripsListView;
    private UserTripsListArrayAdapter myAdapter;
    private TextView noUserTripsTV;
    private ImageButton myTripsRefreshIB;
    private Logger logger;

    private final OnSnackBarActionClickListener snackBarListener = this;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeOfUserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeOfUserFragment newInstance(String param1, String param2)
    {
        HomeOfUserFragment fragment = new HomeOfUserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeOfUserFragment()
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
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_home_of_user, container, false);
        userTrips = new ArrayList<>();

        getUIComponents(mainView);
        setUIComponentsListeners(mainView);

        requestUserTrips();
        userTripsListView.setOnItemClickListener(new UserTripsListItemClickListener());

        return mainView;
    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState)
//    {
//        super.onActivityCreated(savedInstanceState);
//
//        if (savedInstanceState != null)
//        {
//            userTrips = savedInstanceState.getParcelableArrayList("userTrips");
//        }
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState)
//    {
//        super.onSaveInstanceState(outState);
//
//        outState.putParcelableArrayList("userTrips", userTrips);
//    }

    private void requestUserTrips()
    {
        JSONObject tripJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.GET_USER_TRIPS;
        //String url = "http://10.0.0.5:3000/getUserTrips";
        String username = SaveSharedPreference.getUserName(getActivity().getApplicationContext());

        try
        {
            tripJSON.put("username", username); // adding username

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    userTrips = LoadTripUtils.parseTripsFromJson(response);
                    displayUserTrips();
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    noUserTripsTV.setVisibility(View.GONE);
                    //myTripsRefreshIB.setVisibility(View.VISIBLE);
                    AppUtils.showSnackBarMsgWithAction(getActivity(), homeFragmentMainLayout, snackBarListener);
                    System.out.println("error response on requestUserTrips");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayUserTrips()
    {
        if (userTrips.size() == 0)
            noUserTripsTV.setVisibility(View.VISIBLE);

//        if(myAdapter == null)
        myAdapter = new UserTripsListArrayAdapter(getActivity(), userTrips, this);
        userTripsListView.setAdapter(myAdapter);
    }

    private void setUIComponentsListeners(View mainView)
    {
        planTripButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switchToPlanTripActivity();
            }
        });

        myTripsRefreshIB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                myTripsRefreshIB.setVisibility(View.GONE);
                requestUserTrips();
            }
        });
    }

    private void switchToPlanTripActivity()
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction("onPlanTripClick", (Object[]) null);
        }
    }

    public void getUIComponents(View mainView)
    {
        planTripButton = (LinearLayout) mainView.findViewById(R.id.plan_new_trip_button);
        userTripsListView = (ListView) mainView.findViewById(R.id.user_trips_listview);
        noUserTripsTV = (TextView) mainView.findViewById(R.id.no_user_trips_TV);
        noUserTripsTV.setVisibility(View.GONE);
        myTripsRefreshIB = (ImageButton)mainView.findViewById(R.id.my_trips_list_refresh_IB);
        homeFragmentMainLayout = (FrameLayout)mainView.findViewById(R.id.home_of_user_fragment_layout);
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
    public void onAdapterChange(Object... objects)
    {
        for (Object o: objects)
        {
            if (o instanceof Integer)
            {
                Integer position = (Integer)o;
                userTrips.remove(position);
                if (userTrips.size() == 0)
                    noUserTripsTV.setVisibility(View.VISIBLE);

                myAdapter = new UserTripsListArrayAdapter(getActivity(), userTrips, this);
                userTripsListView.setAdapter(myAdapter);
            }
            else if (o instanceof Bundle)
            {
                mListener.onFragmentInteraction("switchToTripSettings", o);
            }
        }
    }

    public void updateUserTripsList(Bundle bundle)
    {
        int position = findTripPositionById(bundle.getString("trip_id"));
        if (position != AppConstants.NOT_FOUND)
        {
            if(bundle.getBoolean("trip_deleted"))
                removeTripFromUserTrips(position);
            else
                updateSingleTripInList(position, bundle);

            userTripsListView.setAdapter(null);
            myAdapter = new UserTripsListArrayAdapter(getActivity(), userTrips, this);
            userTripsListView.setAdapter(myAdapter);
        }
    }

    private void removeTripFromUserTrips(int position)
    {
        userTrips.remove(position);
    }

    private void updateSingleTripInList(int position, Bundle bundle)
    {
        Trip currTrip = userTrips.get(position);
        currTrip.setName(bundle.getString("trip_name"));
        currTrip.setDepartureDate(DateUtils.stringToDate(bundle.getString("trip_departure")));
        currTrip.setReturnDate(DateUtils.stringToDate(bundle.getString("trip_return")));
        currTrip.setTripType(eTripType.fromStringToEnum(bundle.getString("trip_type")));
        currTrip.setTravelingWith(eTravelingWith.fromStringToEnum(bundle.getString("trip_going_with")));
        currTrip.setShare(bundle.getBoolean("share"));
        currTrip.setTripCoverPhotoStr(bundle.getString("trip_photo"));
    }

    private int findTripPositionById(String tripId)
    {
        for (int i = 0; i < userTrips.size(); i++)
        {
            if (userTrips.get(i).getID().equals(tripId))
                return i;
        }

        return AppConstants.NOT_FOUND;
    }

    @Override
    public void onSnackBarActionClick()
    {
        requestUserTrips();
    }

    private class UserTripsListItemClickListener implements android.widget.AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            if(AppUtils.isNetworkAvailable(getActivity()))
                loadExistingTrip(position);
            else
                AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, getActivity(), homeFragmentMainLayout);
        }
    }

    private void loadExistingTrip(int position)
    {
        System.out.println("in loadExistingTrip()");
        mListener.onFragmentInteraction("onLoadExistingTrip", userTrips.get(position));
        // requestTripPlacesFromServer(userTrips.get(position));
    }

    private void requestTripPlacesFromServer(final Trip tripToLoad)
    {
        JSONObject tripPlacesJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.GET_TRIP_PLACES;
        //String url = "http://10.0.0.4:3000/getTripPlaces";

        try
        {
            final ProgressDialog progress = new ProgressDialog(getActivity());
            showLoadingTripProgressDialog(progress, tripToLoad.getName());

            tripPlacesJSON.put("trip_id", tripToLoad.getID());
            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripPlacesJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    handleTripPlacesResponse(response, tripToLoad, progress);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on requestTripPlacesFromServer()");
                    progress.dismiss();
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showLoadingTripProgressDialog(ProgressDialog progress, String tripName)
    {
        progress.setTitle("Loading");
        progress.setMessage("Loading trip \""+ tripName +"\"...");
        progress.show();
        progress.setCancelable(false);
    }

    private void handleTripPlacesResponse(JSONObject response, final Trip tripToLoad, final ProgressDialog progress)
    {
        tripToLoad.parsePlacesFromJson(response);
        new AsyncTask()
        {

            @Override
            protected Object doInBackground(Object[] params)
            {
                getPlacesImagesFromServer(tripToLoad);

                return null;
            }
            @Override
            protected void onPostExecute(Object o)
            {
                super.onPostExecute(o);
                progress.dismiss();
                System.out.println("*****!!!!!$$$$$ IN ONPOST");
//                mListener.onFragmentInteraction("onLoadExistingTrip", tripToLoad);
            }
        }.execute();
    }

    private void getPlacesImagesFromServer(Trip tripToLoad)
    {
        for (int i = 0 ; i < tripToLoad.getNumOfTripPlaces() ; i++)
        {
            getMainPhotoFromServer(tripToLoad.getTripPlace(i));
        }
    }

    private void getMainPhotoFromServer(final TripPlace place)
    {
        JSONObject placeImageJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.GET_PLACE_IMAGE;
        //String url = "http://192.168.205.237:/addTripPlace";

        Bitmap mainPhotoBitmap = makePlaceImageCall(url, place.getServerID());
        place.setMainPhotoBitmap(mainPhotoBitmap);
    }

    private Bitmap makePlaceImageCall(String url, String serverID)
    {
        Bitmap mainPhotoBitmap = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        JSONObject json = new JSONObject();

        try {
            // Add your data
            json.put("server_id", serverID);
            StringEntity se = new StringEntity(json.toString());
            se.setContentType("application/json;charset=UTF-8");
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
            httppost.setEntity(se);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String jsonString = reader.readLine();
            JSONTokener tokener = new JSONTokener(jsonString);

            JSONObject finalResult = new JSONObject(tokener);
            String mainPhotoString = finalResult.getString("place_image");
            System.out.println("*****!!!!!$$$$$ " +mainPhotoString + " $$$$$!!!!!*****");
            mainPhotoBitmap = AppUtils.stringToBitMap(mainPhotoString);

        } catch (ClientProtocolException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mainPhotoBitmap;
    }

    @Override
    public void onResume()
    {
        super.onResume();

//        userTripsListView.setAdapter(null);
//        requestUserTrips();
    }
}
