package com.trippin.androidtrippin.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.trippin.androidtrippin.model.ImageItem;
import com.trippin.androidtrippin.R;

import java.util.ArrayList;


public class PhotosGridViewAdapter extends ArrayAdapter
{
    private Context context;
    private int layoutResourceId;
    private ArrayList<View> rows;
    private ArrayList<ImageItem> data = new ArrayList();

    public PhotosGridViewAdapter(Context context, int layoutResourceId, ArrayList data)
    {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.rows = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        View row = convertView;
        ViewHolder holder = null;
        int tempPosition;



//        tempPosition = getCorrectPosition(parent, position);
//        if(tempPosition != -1){
//            position = tempPosition;
//        }

        if (row == null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
//            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

//        if(position >= 0 && position < data.size())
//            row.setBackgroundColor(context.getResources().getColor(R.color.transparent));

        ImageItem item = data.get(position);
//        holder.imageTitle.setText(item.getTitle());
        holder.image.setImageBitmap(item.getImage());


        return row;
    }

    private int getCorrectPosition(ViewGroup gridView, int position)
    {
        int firstPosition = ((GridView)gridView).getFirstVisiblePosition();
        int lastPosition = ((GridView)gridView).getLastVisiblePosition();

        if ((position < firstPosition) || (position > lastPosition))
            return -1;

        return position - firstPosition;
    }

    static class ViewHolder
    {
//        TextView imageTitle;
        ImageView image;
    }
}
