package com.trippin.androidtrippin.model;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.trippin.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by shaiyahli on 8/19/2015.
 */
public class GooglePlaceLoader
{
    private ArrayList<String> myTripPlacesGoogleIDs;
    private String myTripID;
    private OnGooglePlaceLoadListener mListener;
    private String placeID;
    private Context ctx;

    public GooglePlaceLoader(ArrayList<String> myTripPlacesGoogleIDs, String myTripID, OnGooglePlaceLoadListener mListener, String placeID, Context ctx)
    {
        this.myTripPlacesGoogleIDs = myTripPlacesGoogleIDs;
        this.myTripID = myTripID;
        this.mListener = mListener;
        this.placeID = placeID;
        this.ctx = ctx;
    }

    public void loadGooglePlace()
    {
        JSONObject tripJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.GET_TRIP_PLACES_GOOGLE_IDS;
        //String url = "http://192.168.204.50:3000/getTripPlacesGoogleIDs";

        try
        {
            tripJSON.put("tripID", myTripID);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    onGetTripPlacesGoogleIDsResponse(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    mListener.onGooglePlaceLoadFinished(AppConstants.RESPONSE_FAILURE, null);
                    System.out.println("error response on getMyTripPlacesGoogleIDsFromServer");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onGetTripPlacesGoogleIDsResponse(JSONObject response)
    {
        try {
            switch(response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    parseGoogleIDs(response);
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    mListener.onGooglePlaceLoadFinished(AppConstants.RESPONSE_FAILURE, null);
                    System.out.println("Server error - received failure on onGetTripPlacesGoogleIDsResponse");
                    break;
                default:
                    mListener.onGooglePlaceLoadFinished(AppConstants.RESPONSE_FAILURE, null);
                    System.out.println("error onGetTripPlacesGoogleIDsResponse()");
            }
            if(AppUtils.isNetworkAvailable(ctx))
                getPlaceDetails(placeID);
            else
                mListener.onGooglePlaceLoadFinished(AppConstants.RESPONSE_FAILURE, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseGoogleIDs(JSONObject response)
    {
        JSONArray googleIDsJSONArray;

        if (response.has("googlePlacesIDs")) {
            try {
                googleIDsJSONArray = response.getJSONArray("googlePlacesIDs");

                for (int i = 0; i < googleIDsJSONArray.length(); i++) {
                    String currGoogleID = googleIDsJSONArray.get(i).toString();
                    myTripPlacesGoogleIDs.add(currGoogleID);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getPlaceDetails(final String placeID)
    {

        new AsyncTask()
        {
            String results;

            @Override
            protected Object doInBackground(Object[] params)
            {
                String placeDetailsRequestURL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeID + "&key=" + GoogleServerKey.GOOGLE_SERVER_KEY;
                results = GooglePlacesUtils.makeCall(placeDetailsRequestURL);
                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                GooglePlace place;
                super.onPostExecute(o);

                if(results.equals(AppConstants.EMPTY_STRING)){
                    mListener.onGooglePlaceLoadFinished(AppConstants.RESPONSE_FAILURE, null);
                }
                else{
                    place = GooglePlacesUtils.parseGooglePlacesDetailsJson(results);
                    getPlaceDescription(place);
                }
            }
        }.execute();
    }

    private void getPlaceDescription(final GooglePlace place)
    {
        new AsyncTask()
        {
            String results;

            @Override
            protected Object doInBackground(Object[] params)
            {
                String formattedPlaceName = formatPlaceName(place.getName());
                String placeDescriptionRequestURL = "https://www.googleapis.com/freebase/v1/text/en/"+formattedPlaceName; //TODO: FIND NEW DB FOR PLACE DESCRIPTION
                results = GooglePlacesUtils.makeCall(placeDescriptionRequestURL);
                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                super.onPostExecute(o);

                place.setDescription(GooglePlacesUtils.parseGooglePlaceDescriptionJson(results));
                mListener.onGooglePlaceLoadFinished(AppConstants.RESPONSE_SUCCESS, place);
            }
        }.execute();
    }

    private String formatPlaceName(String name)
    {
        String formattedName = name;

        formattedName = formattedName.replaceAll(" ", "_").toLowerCase();

        return formattedName;
    }
}
