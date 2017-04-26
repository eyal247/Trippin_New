package com.trippin.androidtrippin.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.DateUtils;
import com.trippin.androidtrippin.model.OnAdapterChangeListener;
import com.trippin.androidtrippin.model.Trip;
import com.trippin.androidtrippin.trippin.AppController;
import com.trippin.androidtrippin.trippin.MainActivity;
import com.trippin.androidtrippin.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by EyalEngel on 12/07/15.
 */
public class UserTripsListArrayAdapter extends ArrayAdapter<Trip>
{
    private final OnAdapterChangeListener mListener; // listener is HomeOfUserFragment (extends OnAdapterChangeListener)
    private ArrayList<Trip> userTrips;
    private final View[] rowsViews;
    private Context context;
    private String mainPhotoForEditSettings;
    private TextView nameTv;
    private TextView destTv;
    private TextView numOfDaysTv;
    private TextView goingWithTv;
    private TextView typeTv;
    private FloatingActionButton tripSettingsFAB;
    private Bitmap tripBitmap;
    private String tripCoverPhotoID;
    //private StringBuilder key;

    public UserTripsListArrayAdapter(Context context, ArrayList<Trip> userTrips, OnAdapterChangeListener listener)
    {
        super(context, R.layout.user_trips_list_item ,userTrips);

        this.userTrips = userTrips;
        this.mListener = listener;
        this.context = context;
        rowsViews = new View[userTrips.size()];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (rowsViews[position] != null)
            return rowsViews[position];

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.user_trips_list_item, parent, false);
        final ImageView tripImageView = (ImageView) rowView.findViewById(R.id.user_trip_img);
        nameTv = (TextView) rowView.findViewById(R.id.trip_name_tv);
        destTv = (TextView) rowView.findViewById(R.id.user_trip_dest);
        numOfDaysTv = (TextView) rowView.findViewById(R.id.user_trip_dates_tv);
        goingWithTv = (TextView) rowView.findViewById(R.id.user_trips_with_tv);
        typeTv = (TextView) rowView.findViewById(R.id.user_trips_type_tv);
        tripSettingsFAB = (FloatingActionButton) rowView.findViewById(R.id.trip_settings_FAB);
        tripSettingsFAB.setFocusable(false);
        setOnClicktripSettingsFAB(tripSettingsFAB, position);

        attachContentToView(position, tripImageView);
        rowsViews[position] = rowView;

        return rowView;
    }

    private void attachContentToView(int position, ImageView tripImageView)
    {
        Trip currTrip = userTrips.get(position);
        StringBuilder datesAndDays = getDatesAndDaysStr(position);
        nameTv.setText(currTrip.getName());
        destTv.setText(currTrip.getDestination());
        numOfDaysTv.setText(datesAndDays); // (datesAndDays)
        goingWithTv.setText(currTrip.getGoingWith().strValue());
        typeTv.setText(currTrip.getType().strValue());
        setTripImageBitmap(currTrip, position, tripImageView);
    }

    private void setTripImageBitmap(Trip currTrip, int position, ImageView tripImageView)
    {
        StringBuilder key = new StringBuilder(currTrip.getID());//.append(currTrip.getName());

        tripBitmap = AppController.getInstance().getLruBitmapCache().getBitmap(key.toString());

        if (tripBitmap == null) //bitmap not in cache
            getTripCoverPhotoFromServer(position, tripImageView);
        else //bitmap is already in cache
        {
            tripImageView.setImageBitmap(tripBitmap);
            this.getItem(position).setTripCoverPhotoStr(AppUtils.bitMapToString(tripBitmap));
        }
    }

    private void getTripCoverPhotoFromServer(final int position, final ImageView tripImageView)
    {
        JSONObject deleteImageJSON;
        String url = AppConstants.SERVER_URL + AppConstants.GET_TRIP_MAIN_PHOTO;
        //String url = "http://192.168.207.205:3000/getTripMainPhoto";

        try
        {
            deleteImageJSON = new JSONObject();
            deleteImageJSON.put("tripID", userTrips.get(position).getID());

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, deleteImageJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    handleMainPhotoResponse(response, position, tripImageView);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on getTripCoverPhotoFromServer()");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleMainPhotoResponse(JSONObject response, int position, ImageView tripImageView)
    {
        String mainPhoto = null;
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    mainPhoto = response.getString("mainPhoto");
                    tripCoverPhotoID = response.getString("imageID");
                    setTripCoverPhoto(position, mainPhoto, tripImageView);
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    Toast.makeText(context, "Couldn't load trip cover photo. Server error.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    System.out.println("error on handleMainPhotoResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setTripCoverPhoto(int position, String mainPhoto, ImageView tripImageView)
    {
        if (!mainPhoto.equals(AppConstants.EMPTY_STRING))
        {
            tripBitmap = AppUtils.stringToBitMap(mainPhoto);
//            userTrips.get(position).setTripMainImage(mainPhotoBitmap);
            tripImageView.setImageBitmap(tripBitmap);
            this.getItem(position).setTripCoverPhotoStr(mainPhoto);

            StringBuilder key = new StringBuilder(userTrips.get(position).getID());//.append(userTrips.get(position).getName());
            AppController.getInstance().getLruBitmapCache().putBitmap(key.toString(), tripBitmap);
            this.getItem(position).setTripCoverPhotoStr(AppUtils.bitMapToString(tripBitmap));
        }
        else
        {
            tripImageView.setImageResource(R.drawable.default_trip_image);
        }
    }

    private void setOnClicktripSettingsFAB(FloatingActionButton tripSettingsFAB, final int position)
    {
        tripSettingsFAB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goToTripSettings(position);
            }
        });
    }

    private void goToTripSettings(int position)
    {
//        Intent intent = new Intent(context, TripSettingsActivity.class);

//        mainPhotoForEditSettings = this.getItem(position).getTripCoverPhotoStr();

        Bundle bundle = userTrips.get(position).createTripDetailsBundle();
        bundle.putString("imageID", tripCoverPhotoID);
        bundle.putString("caller", "HomeActivity");
//        intent.putExtras(bundle);
//
//        context.startActivity(intent);


        mListener.onAdapterChange(bundle);
    }

//    private void onDeleteTrip(final int position)
//    {
//        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                switch (which){
//                    case DialogInterface.BUTTON_POSITIVE: //Yes button clicked
//                        deleteTrip(position);
//                        break;
//
//                    case DialogInterface.BUTTON_NEGATIVE: //No button clicked
//                        // Nothin to do
//                        break;
//                }
//            }
//        };
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setMessage("Delete trip?").setPositiveButton("Delete", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).show();
//    }
//
//    private void deleteTrip(final int position)
//    {
//        JSONObject deletePlaceJSON;
//        String url = AppConstants.SERVER_URL + AppConstants.DELETE_TRIP;
//        //String url = "http://192.168.207.205:3000/deleteTrip";
//
//        try
//        {
//            deletePlaceJSON = new JSONObject();
//            deletePlaceJSON.put("tripID", userTrips.get(position).getID());
//
//            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, deletePlaceJSON, new Response.Listener<JSONObject>()
//            {
//                @Override
//                public void onResponse(JSONObject response) {
//                    handleDeleteTripResponse(response, position);
//                }
//            }, new Response.ErrorListener() {
//
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    System.out.println("error response on deleteTrip()");
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
//    private void handleDeleteTripResponse(JSONObject response, int position)
//    {
//        try
//        {
//            switch (response.getString("result"))
//            {
//                case AppConstants.RESPONSE_SUCCESS:
//                    removeTripAndNotifyAdapter(position);
//                    break;
//                case AppConstants.RESPONSE_FAILURE:
//                    Toast.makeText(context, "Couldn't delete trip. Server error.", Toast.LENGTH_LONG).show();
//                    break;
//                default:
//                    System.out.println("error on handleDeleteTripResponse() (result)");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private void removeTripAndNotifyAdapter(int position)
    {
        mListener.onAdapterChange(Integer.valueOf(position));
    }

    private StringBuilder getDatesAndDaysStr(int position)
    {
        String departureDate = DateUtils.dateToString(userTrips.get(position).getDepartureDate());
        String returnDate = DateUtils.dateToString(userTrips.get(position).getReturnDate());
        int numOfDays = userTrips.get(position).getNumOfDays();
        StringBuilder tripDates = new StringBuilder()
                .append(departureDate)
                .append(" - ")
                .append(returnDate)
                .append(" (")
                .append(numOfDays)
                .append(" DAYS)");

        return tripDates;
    }
}
