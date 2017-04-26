package com.trippin.androidtrippin.adapters;

/**
 * Created by EyalEngel on 13/05/15.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.GooglePlace;
import com.trippin.androidtrippin.model.GooglePlacesUtils;
import com.trippin.androidtrippin.model.GoogleServerKey;
import com.trippin.androidtrippin.model.PhotoInfo;
import com.trippin.androidtrippin.trippin.AppController;
import com.trippin.androidtrippin.R;

import java.util.ArrayList;
import java.util.List;

public class NearbyPlacesListArrayAdapter extends ArrayAdapter<String>
{
    private final Context context;
    private final List<GooglePlace> googleNearbyPlaces;
    private final List placesIDs;
    private final View[] rowsViews;
    private ArrayList<PhotoInfo> photoInfoList;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public NearbyPlacesListArrayAdapter(Context context, List googleNearbyPlaces, List placesIDs, ArrayList<PhotoInfo> photoInfoList)
    {
        super(context, R.layout.nearby_list_item, googleNearbyPlaces);
        this.context = context;
        this.googleNearbyPlaces = googleNearbyPlaces;
        this.placesIDs = placesIDs;
        this.rowsViews = new View[googleNearbyPlaces.size()];
        this.photoInfoList = photoInfoList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        String mainPhotoRef;
        String mainPhotoMaxHeight;

        if (rowsViews[position] != null)
            return rowsViews[position];

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.nearby_list_item, parent, false);
        final TextView titleTV = (TextView) rowView.findViewById(R.id.firstLine);
        final TextView subtitleTV = (TextView) rowView.findViewById(R.id.secondLine);
        final TextView thirdTitleTV = (TextView) rowView.findViewById(R.id.third_line);
        final TextView bottomRightCornerTV = (TextView) rowView.findViewById(R.id.bottom_right_corner);

        //************************************************************
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        final NetworkImageView imageView = (NetworkImageView) rowView.findViewById(R.id.icon);

        String placeImageUrl = GooglePlacesUtils.createPlaceImageUrl(photoInfoList.get(position));

        imageView.setDefaultImageResId(R.drawable.grey_square);
        imageView.setErrorImageResId(R.drawable.no_photo_available);

        if (placeImageUrl == null)
        {
            imageView.setDefaultImageResId(R.drawable.no_photo_available);
        }
        else{
            imageView.setImageUrl(placeImageUrl, imageLoader);
        }
        //************************************************************

        GooglePlace currPlaceInNerabyList = googleNearbyPlaces.get(position);

        titleTV.setText(currPlaceInNerabyList.getName());
        subtitleTV.setText(currPlaceInNerabyList.getMainType());
        thirdTitleTV.setText(currPlaceInNerabyList.getVicinity());
        //bottomRightCornerTV.setText(googleNearbyPlaces.get(position).);

        //setImageWithDetailsRequest(imageView, rowView, position);
//        setImageWithoutDetailsRequest(imageView, rowView, position);

        return rowView;
    }

    private void setImageWithoutDetailsRequest(final ImageView imageView, final View rowView, final int position){
        setImage(null, imageView, rowView, position, false);
    }

    private void setImageWithDetailsRequest(final ImageView imageView, final View rowView, final int position){
        new AsyncTask()
        {
            String results;
            PhotoInfo mainPhotoInfo = new PhotoInfo();

            @Override
            protected Object doInBackground(Object[] params)
            {
                String placeDetailsRequestURL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placesIDs.get(position).toString() + "&key=" + GoogleServerKey.GOOGLE_SERVER_KEY;
                results = GooglePlacesUtils.makeCall(placeDetailsRequestURL);
                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                super.onPostExecute(o);
                mainPhotoInfo = GooglePlacesUtils.getMainPhotoInfoFromDetailsJson(results);

                setImage(mainPhotoInfo, imageView, rowView, position, true);

            }
        }.execute();

    }

    private void setImage(final PhotoInfo mainPhotoInfo, final ImageView imageView, final View rowView, final int position, final Boolean gotPhotoFromDetailsRequest){

        new AsyncTask() {
            Bitmap photoBitmap = null;

            @Override
            protected Object doInBackground(Object[] params)
            {
                if (gotPhotoFromDetailsRequest == true)
                {
                    if (mainPhotoInfo.getPhotoRef() != "")
                        photoBitmap = GooglePlacesUtils.makePhotoCall(mainPhotoInfo.getPhotoRef(), mainPhotoInfo.getMaxHeight());
                }else{
                    if(photoInfoList.get(position).getPhotoRef() != "")
                        photoBitmap = GooglePlacesUtils.makePhotoCall(photoInfoList.get(position).getPhotoRef(), photoInfoList.get(position).getMaxHeight());
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                super.onPostExecute(o);

                if(photoBitmap != null)
                    imageView.setImageBitmap(photoBitmap);
                else
                    imageView.setImageResource(R.drawable.no_photo_available);

                rowsViews[position] = rowView;
            }
        }.execute();
    }
}
