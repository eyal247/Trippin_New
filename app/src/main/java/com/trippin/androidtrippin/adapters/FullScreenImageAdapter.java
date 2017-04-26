package com.trippin.androidtrippin.adapters;

/**
 * Created by EyalEngel on 27/09/15.
 */
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.TouchImageView;
import com.trippin.androidtrippin.R;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class FullScreenImageAdapter extends PagerAdapter {

    private Activity activity;
    private ArrayList<String> imagesStrings;
    private LayoutInflater inflater;
    private TouchImageView imgDisplay;

    // constructor
    public FullScreenImageAdapter(Activity activity,
                                  ArrayList<String> imagePaths) {
        this.activity = activity;
        this.imagesStrings = imagePaths;
    }

    @Override
    public int getCount() {
        return this.imagesStrings.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_full_screen_image, container,
                false);

        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.expanded_image_IV);
        Bitmap bitmap = AppUtils.stringToBitMap(imagesStrings.get(position));
        imgDisplay.setImageBitmap(bitmap);

        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}
