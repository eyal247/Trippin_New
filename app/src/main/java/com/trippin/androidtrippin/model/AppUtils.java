package com.trippin.androidtrippin.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.trippin.androidtrippin.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by shaiyahli on 5/27/2015.
 */
public class AppUtils
{
    public static String bitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        byte[] b = baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public static Bitmap stringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static Bitmap resizeBitmap(Bitmap image, int maxSize)
    {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1)
        {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else
        {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static ArrayList<String> getCountriesList()
    {
        ArrayList<String> countriesList = new ArrayList<>();
        Locale[] locale = Locale.getAvailableLocales();
        String country;

        for( Locale loc : locale ){
            country = loc.getDisplayCountry();
            if( country.length() > 0 && !countriesList.contains(country) ){
                countriesList.add(country);
            }
        }

        Collections.sort(countriesList, String.CASE_INSENSITIVE_ORDER);

        return countriesList;
    }

    public static String createTripperDisplayName(String fname, String lname)
    {
        StringBuilder displayName = new StringBuilder();
        displayName.append(fname)
                .append(" ")
                .append(lname.substring(0, 1).toUpperCase())
                .append(".");

        return displayName.toString();
    }

    public static Bitmap fromUrlToBitmap(String encodedProfilePic)
    {
        URL url = null;
        Bitmap image = null;

        try {
            url = new URL(encodedProfilePic);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public static void showSnackBarMsg(String msg, Context context, ViewGroup mainActivityLayout)
    {
        Snackbar snackbar = Snackbar
                .make(mainActivityLayout, msg, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        textView.setGravity(Gravity.CENTER);
        snackbar.show();
    }

    public static void showSnackBarMsgWithAction(Context context, ViewGroup mainActivityLayout, final OnSnackBarActionClickListener listener)
    {
        Snackbar snackbar = Snackbar
                .make(mainActivityLayout, "No internet connection!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onSnackBarActionClick();
                    }
                });

        // Changing message text color
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        textView.setGravity(Gravity.CENTER);
        snackbar.show();
    }

    public static void animateView(Techniques technique, int duration, View view)
    {
        YoYo.with(technique)
                .duration(duration)
                .playOn(view);
    }

    private class GetProfileImage extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {

                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {

        }
    }

    public static boolean isNetworkAvailable(Context ctx) {
        if(ctx == null)
            return false;

        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void showNoInternetConnectionToast(Context ctx)
    {
        if(ctx != null)
            Toast.makeText(ctx, "No Internet Connection", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(ctx, "Error: Activity null", Toast.LENGTH_LONG).show();

    }

    public static void hideKeyboard(Context ctx, IBinder viewById)
    {
        View currView = ((Activity)ctx).getCurrentFocus();
        if (currView != null) {

            InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewById, 0);
        }

        ((Activity)ctx).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /*
     * getting screen width
     */
    public int getScreenWidth(Context context) {
        int columnWidth;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }

//    public static Trip parseOneTripFromJson(JSONObject response)
//    {
//        Trip trip = null;
//        JSONObject tripJSON;
//        Date departureDate, returnDate;
//
//        try
//        {
//            if (response.has("trip"))
//            {
//                tripJSON = response.getJSONObject("trip");
//
//                String name = tripJSON.optString("name");
//                String destination = tripJSON.optString("destination");
//                //int numOfDays = Integer.parseInt(tripsJSONArray.getJSONObject(i).optString("numOfDays"));
//                eTravelingWith goingWith = eTravelingWith.fromStringToEnum(tripJSON.optString("goingWith"));
//                eTripType type = eTripType.fromStringToEnum(tripJSON.optString("type"));
//                String id = tripJSON.optString("_id");
//                String departureDateStr = tripJSON.optString("departureDate");
//                String returnDateStr = tripJSON.optString("returnDate");
//                String username = tripJSON.optString("username");
//
//                double mainLat = Double.parseDouble(tripJSON.optString("mainLat"));
//                double mainLng = Double.parseDouble(tripJSON.optString("mainLng"));
//                departureDate = AppUtils.stringToDate(departureDateStr);
//                returnDate = AppUtils.stringToDate(returnDateStr);
////                    System.out.println("&&&&&&&&&&&&&&&" + departureDate.toString() + " - " + returnDate.toString());
//
//                //add departureDate and returnDate to Trip Constructor
//                trip = new Trip(name, destination, departureDate, returnDate, goingWith, type, mainLat, mainLng, username);
//                trip.setID(id);
//            }
//        }
//        catch (JSONException e1) {
//            e1.printStackTrace();
//        }
//
//        return trip;
//    }
//
//    public static ArrayList<Trip> parseTripsFromJson(JSONObject response)
//    {
//        ArrayList<Trip> trips = new ArrayList<>();
//        JSONArray tripsJSONArray;
//        Date departureDate, returnDate;
//
//        try
//        {
//            if (response.has("trips"))
//            {
//                tripsJSONArray = response.getJSONArray("trips");
//
//                for(int i = 0; i < tripsJSONArray.length(); i++) // for each trip in json array
//                {
//                    String name = tripsJSONArray.getJSONObject(i).optString("name");
//                    String destination = tripsJSONArray.getJSONObject(i).optString("destination");
//                    //int numOfDays = Integer.parseInt(tripsJSONArray.getJSONObject(i).optString("numOfDays"));
//                    eTravelingWith goingWith = eTravelingWith.fromStringToEnum(tripsJSONArray.getJSONObject(i).optString("goingWith"));
//                    eTripType type = eTripType.fromStringToEnum(tripsJSONArray.getJSONObject(i).optString("type"));
//                    String id = tripsJSONArray.getJSONObject(i).optString("_id");
//                    String departureDateStr = tripsJSONArray.getJSONObject(i).optString("departureDate");
//                    String returnDateStr = tripsJSONArray.getJSONObject(i).optString("returnDate");
//                    String username = tripsJSONArray.getJSONObject(i).optString("username");
//
//                    double mainLat = Double.parseDouble(tripsJSONArray.getJSONObject(i).optString("mainLat"));
//                    double mainLng = Double.parseDouble(tripsJSONArray.getJSONObject(i).optString("mainLng"));
//                    departureDate = AppUtils.stringToDate(departureDateStr);
//                    returnDate = AppUtils.stringToDate(returnDateStr);
////                    System.out.println("&&&&&&&&&&&&&&&" + departureDate.toString() + " - " + returnDate.toString());
//
//                    //add departureDate and returnDate to Trip Constructor
//                    Trip currTrip = new Trip(name, destination, departureDate, returnDate, goingWith, type, mainLat, mainLng, username);
//                    currTrip.setID(id);
//                    trips.add(currTrip);
//                }
//            }
//        }
//        catch (JSONException e1) {
//            e1.printStackTrace();
//        }
//
//        return trips;
//    }
//
//    public static void requestTripPlacesFromServer(final Trip tripToLoad, Context context, final TripMapFragment tripMapFragment)
//    {
//        JSONObject tripPlacesJSON = new JSONObject();
//        String url = AppConstants.SERVER_URL + AppConstants.GET_TRIP_PLACES;
//        //String url = "http://192.168.205.237:3000/getTripPlaces";
//
//        try
//        {
//            final ProgressDialog progress = new ProgressDialog(context, R.style.progress_theme);
//            showLoadingTripProgressDialog(progress, tripToLoad.getName().toUpperCase());
//
//            tripPlacesJSON.put("trip_id", tripToLoad.getID());
//            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripPlacesJSON, new Response.Listener<JSONObject>()
//            {
//                @Override
//                public void onResponse(JSONObject response) {
//                    handleTripPlacesResponse(response, tripToLoad, progress, tripMapFragment);
//                }
//            }, new Response.ErrorListener() {
//
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    System.out.println("error response on requestTripPlacesFromServer()");
//                    progress.dismiss();
//                }
//            });
//
//            MainActivity.addRequestToQueue(jsObjRequest);
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void showLoadingTripProgressDialog(ProgressDialog progress, String tripName)
//    {
//        progress.setTitle("Loading");
//        progress.setMessage("Loading trip \""+ tripName +"\"...");
//        progress.show();
//        progress.setCancelable(false);
//    }
//
//    private static void handleTripPlacesResponse(JSONObject response, final Trip tripToLoad, final ProgressDialog progress, final TripMapFragment tripMapFragment)
//    {
//        tripToLoad.parsePlacesFromJson(response);
//
//        new AsyncTask()
//        {
//
//            @Override
//            protected Object doInBackground(Object[] params)
//            {
//                getPlacesImagesFromServer(tripToLoad);
//
//                return null;
//            }
//            @Override
//            protected void onPostExecute(Object o)
//            {
//                super.onPostExecute(o);
//                progress.dismiss();
//                System.out.println("*****!!!!!$$$$$ IN ONPOST");
//                tripMapFragment.handleCallFromHomeActivity(tripToLoad);
////                mListener.onFragmentInteraction("onLoadExistingTrip", tripToLoad);
//            }
//        }.execute();
//    }
//
//    private static void getPlacesImagesFromServer(Trip currTrip)
//    {
//        for (int i = 0 ; i < currTrip.getNumOfTripPlaces() ; i++)
//        {
//            getMainPhotoFromServer(i, currTrip);
//        }
//    }
//
//    private static void getMainPhotoFromServer(int placeIndexInTrip, Trip currTrip)
//    {
//        String url = AppConstants.SERVER_URL + AppConstants.GET_PLACE_IMAGE;
//        //String url = "http://10.0.0.4:3000/addTripPlace";
//
//        Bitmap mainPhotoBitmap = makePlaceImageCall(url, currTrip.getTripPlace(placeIndexInTrip).getServerID());
//        currTrip.getTripPlace(placeIndexInTrip).setMainPhotoBitmap(mainPhotoBitmap);
//    }
//
//    private static Bitmap makePlaceImageCall(String url, String serverID)
//    {
//        Bitmap mainPhotoBitmap = null;
//        HttpClient httpclient = new DefaultHttpClient();
//        HttpPost httppost = new HttpPost(url);
//        JSONObject json = new JSONObject();
//
//        try {
//            // Add your data
//            json.put("server_id", serverID);
//            StringEntity se = new StringEntity(json.toString());
//            se.setContentType("application/json;charset=UTF-8");
//            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
//            httppost.setEntity(se);
//
//            // Execute HTTP Post Request
//            HttpResponse response = httpclient.execute(httppost);
//
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
//            String jsonString = reader.readLine();
//            JSONTokener tokener = new JSONTokener(jsonString);
//
//            JSONObject finalResult = new JSONObject(tokener);
//            String mainPhotoString = finalResult.getString("place_image");
//            System.out.println("*****!!!!!$$$$$ " +mainPhotoString + " $$$$$!!!!!*****");
//            mainPhotoBitmap = AppUtils.stringToBitMap(mainPhotoString);
//
//        } catch (ClientProtocolException | UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        return mainPhotoBitmap;
//    }
}
