package com.trippin.androidtrippin.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trippin.androidtrippin.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 01/07/2015.
 */
public class TripPlacesListArrayAdapter extends ArrayAdapter<String>
{
    private List placesNames;
    private ArrayList<Bitmap> placesPhotos;
    private final View[] rowsViews;
    private Context context;

    public TripPlacesListArrayAdapter(Context context, List placesNames, ArrayList<Bitmap> placesPhotos)
    {
        super(context, R.layout.trip_place_list_item ,placesNames);

        this.placesNames = placesNames;
        this.placesPhotos = placesPhotos;
        this.context = context;
        rowsViews = new View[placesNames.size()];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (rowsViews[position] != null)
            return rowsViews[position];

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.trip_place_list_item, parent, false);
        final TextView titleTV = (TextView) rowView.findViewById(R.id.trip_place_list_item_name_TV);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.trip_place_list_item_icon);

        imageView.setImageBitmap(placesPhotos.get(position));
        titleTV.setText(placesNames.get(position).toString());

        rowsViews[position] = rowView;

        return rowView;
    }
}
