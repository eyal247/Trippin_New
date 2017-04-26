package com.trippin.androidtrippin.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.fragments.TripMapFragment;
import com.trippin.androidtrippin.trippin.AppController;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by shaiyahli on 8/9/2015.
 */
public class LoadTripUtils
{
    public static Trip parseOneTripFromJson(JSONObject response)
    {
        Trip trip = null;
        JSONObject tripJSON;
        Date departureDate, returnDate;

        try
        {
            if (response.has("trip"))
            {
                tripJSON = response.getJSONObject("trip");

                String name = tripJSON.optString("name");
                String destination = tripJSON.optString("destination");
                //int numOfDays = Integer.parseInt(tripsJSONArray.getJSONObject(i).optString("numOfDays"));
                eTravelingWith goingWith = eTravelingWith.fromStringToEnum(tripJSON.optString("goingWith"));
                eTripType type = eTripType.fromStringToEnum(tripJSON.optString("type"));
                String id = tripJSON.optString("_id");
                String departureDateStr = tripJSON.optString("departureDate");
                String returnDateStr = tripJSON.optString("returnDate");
                String username = tripJSON.optString("username");

                double mainLat = Double.parseDouble(tripJSON.optString("mainLat"));
                double mainLng = Double.parseDouble(tripJSON.optString("mainLng"));
                departureDate = DateUtils.stringToDate(departureDateStr);
                returnDate = DateUtils.stringToDate(returnDateStr);
//                    System.out.println("&&&&&&&&&&&&&&&" + departureDate.toString() + " - " + returnDate.toString());

                //add departureDate and returnDate to Trip Constructor
                trip = new Trip(name, destination, departureDate, returnDate, goingWith, type, mainLat, mainLng, username);
                trip.setID(id);
            }
        }
        catch (JSONException e1) {
            e1.printStackTrace();
        }

        return trip;
    }

    public static ArrayList<Trip> parseTripsFromJson(JSONObject response)
    {
        ArrayList<Trip> trips = new ArrayList<>();
        JSONArray tripsJSONArray;
        Date departureDate, returnDate;
        float tripRating;

        try
        {
            if (response.has("trips"))
            {
                tripsJSONArray = response.getJSONArray("trips");

                for(int i = 0; i < tripsJSONArray.length(); i++) // for each trip in json array
                {
                    String name = tripsJSONArray.getJSONObject(i).optString("name");
                    String destination = tripsJSONArray.getJSONObject(i).optString("destination");
                    //int numOfDays = Integer.parseInt(tripsJSONArray.getJSONObject(i).optString("numOfDays"));
                    eTravelingWith goingWith = eTravelingWith.fromStringToEnum(tripsJSONArray.getJSONObject(i).optString("goingWith"));
                    eTripType type = eTripType.fromStringToEnum(tripsJSONArray.getJSONObject(i).optString("type"));
                    String id = tripsJSONArray.getJSONObject(i).optString("_id");
                    String departureDateStr = tripsJSONArray.getJSONObject(i).optString("departureDate");
                    String returnDateStr = tripsJSONArray.getJSONObject(i).optString("returnDate");
                    String username = tripsJSONArray.getJSONObject(i).optString("username");
//                    String fname = tripsJSONArray.getJSONObject(i).optString("fname");
//                    String lname = tripsJSONArray.getJSONObject(i).optString("lname");
                    double mainLat = Double.parseDouble(tripsJSONArray.getJSONObject(i).optString("mainLat"));
                    double mainLng = Double.parseDouble(tripsJSONArray.getJSONObject(i).optString("mainLng"));
                    departureDate = DateUtils.stringToDate(departureDateStr);
                    returnDate = DateUtils.stringToDate(returnDateStr);
                    boolean share = tripsJSONArray.getJSONObject(i).optBoolean("share");
                    if(tripsJSONArray.getJSONObject(i).has("tripRating"))
                        tripRating = Float.parseFloat(tripsJSONArray.getJSONObject(i).optString("tripRating"));
                    else
                        tripRating = AppConstants.MIN_RATING;

                    Trip currTrip = new Trip(name, destination, departureDate, returnDate, goingWith, type, mainLat, mainLng, username);
//                    currTrip.setTripperName(AppUtils.createTripperDisplayName(fname, lname));
                    currTrip.setID(id);
                    currTrip.setShare(share);
                    currTrip.setRating(tripRating);
                    trips.add(currTrip);
                }
            }
        }
        catch (JSONException e1) {
            e1.printStackTrace();
        }

        return trips;
    }

    public static void requestTripPlacesFromServer(final Trip tripToLoad, Context context, final TripMapFragment tripMapFragment)
    {
        JSONObject tripPlacesJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.GET_TRIP_PLACES;
        //String url = "http://192.168.205.237:3000/getTripPlaces";

        try
        {
            final ProgressDialog progress = new ProgressDialog(context, R.style.progress_theme);
            showLoadingTripProgressDialog(progress, tripToLoad.getName().toUpperCase());

            tripPlacesJSON.put("trip_id", tripToLoad.getID());
            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripPlacesJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    handleTripPlacesResponse(response, tripToLoad, progress, tripMapFragment);
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

    private static void showLoadingTripProgressDialog(ProgressDialog progress, String tripName)
    {
        progress.setTitle("Loading Trip");
        progress.setMessage("Loading \""+ tripName +"\"...");
        progress.show();
        progress.setCancelable(false);
    }

    private static void handleTripPlacesResponse(JSONObject response, final Trip tripToLoad, final ProgressDialog progress, final TripMapFragment tripMapFragment)
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
                tripMapFragment.handleCallFromHomeActivity(tripToLoad);
//                mListener.onFragmentInteraction("onLoadExistingTrip", tripToLoad);
            }
        }.execute();
    }

    private static void getPlacesImagesFromServer(Trip currTrip)
    {
        for (int i = 0 ; i < currTrip.getNumOfTripPlaces() ; i++)
        {
            StringBuilder key = new StringBuilder(currTrip.getTripPlace(i).getPlaceID()).append(currTrip.getTripPlace(i).getName());
            Bitmap bitmap = AppController.getInstance().getLruBitmapCache().getBitmap(key.toString());

            if(bitmap == null)
                getMainPhotoFromServer(i, currTrip);
            else
                currTrip.getTripPlace(i).setMainPhotoBitmap(bitmap);
        }
    }

    private static void getMainPhotoFromServer(int placeIndexInTrip, Trip currTrip)
    {
        String url = AppConstants.SERVER_URL + AppConstants.GET_PLACE_IMAGE;
        //String url = "http://10.0.0.4:3000/addTripPlace";

        Bitmap mainPhotoBitmap = makePlaceImageCall(url, currTrip.getTripPlace(placeIndexInTrip).getServerID());
        currTrip.getTripPlace(placeIndexInTrip).setMainPhotoBitmap(mainPhotoBitmap);

        StringBuilder key = new StringBuilder(currTrip.getTripPlace(placeIndexInTrip).getPlaceID()).append(currTrip.getTripPlace(placeIndexInTrip).getName());
        AppController.getInstance().getLruBitmapCache().putBitmap(key.toString(), mainPhotoBitmap);
    }

    private static Bitmap makePlaceImageCall(String url, String serverID)
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
//            System.out.println("*****!!!!!$$$$$ " +mainPhotoString + " $$$$$!!!!!*****");
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
}
