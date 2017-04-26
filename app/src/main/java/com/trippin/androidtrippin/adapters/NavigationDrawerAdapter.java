package com.trippin.androidtrippin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trippin.androidtrippin.R;

import java.util.ArrayList;

/**
 * Created by EyalEngel on 26/08/15.
 */
public class NavigationDrawerAdapter extends ArrayAdapter<String>
{
    private ArrayList<Integer> icons;
    private ArrayList<String> navItemStrings;
    private Context ctx;
    private View[] rowsViews;


    public NavigationDrawerAdapter(Context context, int resource, int textViewResourceId,
                                   ArrayList<String> navItemStrings, ArrayList<Integer> icons)
    {
        super(context, resource, textViewResourceId, navItemStrings);

        this.ctx = context;
        this.navItemStrings = navItemStrings;
        this.rowsViews = new View[icons.size()];
        this.icons = icons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (rowsViews[position] != null)
            return rowsViews[position];

        LayoutInflater inflater = LayoutInflater.from(this.ctx);
        final View rowView = inflater.inflate(R.layout.drawer_list_item_1, parent, false);
        final TextView rowText = (TextView) rowView.findViewById(R.id.rowText);
        final ImageView rowImage = (ImageView) rowView.findViewById(R.id.rowIcon);

        rowText.setText(navItemStrings.get(position));
        rowImage.setImageResource(icons.get(position));

        return rowView;
    }
}
