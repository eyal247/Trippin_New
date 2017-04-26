package com.trippin.androidtrippin.model;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.trippin.LongClickPlaceFormActivity;
import com.trippin.androidtrippin.trippin.MainActivity;
import com.trippin.androidtrippin.trippin.PlaceDetailsActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by User on 11/08/2015.
 */
public class TripPlaceUtils
{

    public static void sendTripPlaceToServer(final Bundle bundle, final Context activity)
    {
        JSONObject tripPlaceJSON;
        String url = AppConstants.SERVER_URL + AppConstants.ADD_PLACE_TO_TRIP;
        //String url = "http://192.168.206.86:3000/addTripPlace";

        try
        {
            tripPlaceJSON = createJSONFromTripPlaceBundle(bundle);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripPlaceJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    handleCreateTripPlaceResponse(response, bundle, activity);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on sendTripPlaceToServer()");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void handleCreateTripPlaceResponse(JSONObject response, Bundle bundle, final Context activity)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    String noteID = response.getString("noteID");
                    String tripPlaceID = response.getString("trip_place_id");
                    String placeImage = bundle.getString("image");
                    bundle.putString("trip_place_id", tripPlaceID);
                    bundle.putString("note_id", noteID);
                    sendPlaceImageToServer(placeImage, tripPlaceID);

                    if (activity instanceof PlaceDetailsActivity) // not similar trip
                        ((PlaceDetailsActivity)activity).startPlanTripActivity(bundle);
                    else if(activity instanceof LongClickPlaceFormActivity)
                        ((LongClickPlaceFormActivity)activity).startPlanTripActivity(bundle);
                    else
                        Toast.makeText(activity,"Place was added successfully to your trip.", Toast.LENGTH_LONG).show();

                    break;
                case AppConstants.RESPONSE_FAILURE:
                    Toast.makeText(activity, "Couldn't add place to trip. Server error.", Toast.LENGTH_LONG).show();
                    break;
                case AppConstants.RESPONSE_EXISTS:
                    System.out.println("trip place already exists in trip - handleCreateTripPlaceResponse()");
                    break;
                default:
                    System.out.println("error on handleCreateTripPlaceResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void sendPlaceImageToServer(String image, String tripPlaceID)
    {
        JSONObject tripPlaceJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.SEND_PLACE_IMAGE;
        //String url = "http://10.0.0.4:3000/addTripPlace";

        try
        {
            tripPlaceJSON.put("server_id", tripPlaceID);
            tripPlaceJSON.put("place_image", image);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripPlaceJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    if(response.has("success")){
                        System.out.println("Success: Place image saved on server");
                    } else{
                        System.out.println("Failure: Place image wasn't saved on server");
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on sendTripToServer()");
                }
            });
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS*3,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject createJSONFromTripPlaceBundle(Bundle bundle) throws JSONException
    {
        JSONObject tripPlaceJsonObject = new JSONObject();

        tripPlaceJsonObject.put("name", bundle.getString("place_name"));
        tripPlaceJsonObject.put("mainPhoto", bundle.getString("image"));
        tripPlaceJsonObject.put("placeID", bundle.getString("place_id"));
        tripPlaceJsonObject.put("latitude", String.valueOf(bundle.getDouble("lat")));
        tripPlaceJsonObject.put("longitude", String.valueOf(bundle.getDouble("lng")));
        tripPlaceJsonObject.put("note", "");
        tripPlaceJsonObject.put("parentTripID", bundle.getString("trip_id"));

        return tripPlaceJsonObject;
    }
}
