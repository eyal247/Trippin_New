package com.trippin.androidtrippin.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.DateUtils;
import com.trippin.androidtrippin.model.Trip;
import com.trippin.androidtrippin.trippin.AppController;
import com.trippin.androidtrippin.trippin.MainActivity;
import com.trippin.androidtrippin.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by User on 03/08/2015.
 */
public class SimilarTripsListArrayAdapter extends ArrayAdapter<Trip>
{
    private ArrayList<Trip> similarTrips;
    private final View[] rowsViews;
    private Context context;
    private RatingBar similarTripRatingBar;

    public SimilarTripsListArrayAdapter(Context context, ArrayList<Trip> similarTrips)
    {
        super(context, R.layout.similar_trips_list_item, similarTrips);

        this.context = context;
        this.similarTrips = similarTrips;
        this.rowsViews = new View[similarTrips.size()];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (rowsViews[position] != null)
            return rowsViews[position];

        Trip currTrip = similarTrips.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View rowView = inflater.inflate(R.layout.similar_trips_list_item, parent, false);
        final TextView tripNameTV = (TextView) rowView.findViewById(R.id.similar_trip_name);
        final TextView tripDestinationTV = (TextView) rowView.findViewById(R.id.similar_trip_destination);
        final TextView tripTripperTV = (TextView) rowView.findViewById(R.id.similar_trip_tripper);
        final TextView tripDateTV = (TextView) rowView.findViewById(R.id.similar_trip_date);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.similar_trip_IV);
        similarTripRatingBar = (RatingBar)rowView.findViewById(R.id.similar_trip_list_item_ratingBar);
        setRatingAsterisksColor();
        tripNameTV.setText(currTrip.getName());
        tripDestinationTV.setText(currTrip.getDestination());
        tripTripperTV.setText("Tripper: " + getTripperString(currTrip.getUsername()));//TODO + currTrip.getTripperName());
        similarTripRatingBar.setRating(currTrip.getRating());
        String shortDate = DateUtils.dateToShortFormatString(currTrip.getDepartureDate());
        tripDateTV.setText(shortDate);


        StringBuilder key = new StringBuilder(currTrip.getID());//.append(currTrip.getName());
        Bitmap bitmap = AppController.getInstance().getLruBitmapCache().getBitmap(key.toString());
        if (bitmap == null)
            getTripCoverPhotoFromServer(position, imageView);
        else
            imageView.setImageBitmap(bitmap);        //setImage(imageView, rowView, position);

        return rowView;
    }

    private void setRatingAsterisksColor()
    {
        LayerDrawable stars = (LayerDrawable) similarTripRatingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(context.getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_ATOP);
    }

    private String getTripperString(String username)
    {
        String tripper = null;
        int index = username.indexOf('@');
        if(index != AppConstants.NOT_FOUND)
            tripper = username.substring(0, index);

        return tripper;
    }
//
//
//    // TODO: Need To send request to server.
//    private void setImage(ImageView imageView, View rowView, int position)
//    {
//        imageView.setImageResource(R.drawable.nyc);
//    }

    private void getTripCoverPhotoFromServer(final int position, final ImageView tripImageView)
    {
        JSONObject imageJSON;
        String url = AppConstants.SERVER_URL + AppConstants.GET_TRIP_MAIN_PHOTO;
        //String url = "http://192.168.207.205:3000/getTripMainPhoto";

        try
        {
            imageJSON = new JSONObject();
            imageJSON.put("tripID", similarTrips.get(position).getID());

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, imageJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    handleMainPhotoResponse(response, position, tripImageView);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on deletePhoto()");
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
            Bitmap mainPhotoBitmap = AppUtils.stringToBitMap(mainPhoto);
//            userTrips.get(position).setTripMainImage(mainPhotoBitmap);
            tripImageView.setImageBitmap(mainPhotoBitmap);
            tripImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            StringBuilder key = new StringBuilder(similarTrips.get(position).getID());//.append(similarTrips.get(position).getName());
            AppController.getInstance().getLruBitmapCache().putBitmap(key.toString(), mainPhotoBitmap);
        }
        else
        {
            tripImageView.setImageResource(R.drawable.default_trip_image);
        }
    }
}
