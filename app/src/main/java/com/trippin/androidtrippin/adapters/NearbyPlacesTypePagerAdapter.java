package com.trippin.androidtrippin.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.trippin.androidtrippin.fragments.NearbyPlacesTypeFragment;

public class NearbyPlacesTypePagerAdapter extends FragmentStatePagerAdapter
{
    private static final int NEARBY_PLACES_TABS_NUM = 3;
    private Double lat;
    private Double lng;

    private String accommodationsResults;
    private String foodDrinksResults;
    private String attractionsResults;

    private Fragment accommodationsFragment;
    private Fragment foodDrinksFragment;
    private Fragment attractionsFragment;

    public NearbyPlacesTypePagerAdapter(FragmentManager fm)
    {
        super(fm);
    }


    public void setLng(Double lng)
    {
        this.lng = lng;
    }

    public void setLat(Double lat)
    {
        this.lat = lat;
    }

    public void setAccommodationsResults(String accommodationsResults)
    {
        this.accommodationsResults = accommodationsResults;
    }

    public void setFoodDrinksResults(String foodDrinksResults)
    {
        this.foodDrinksResults = foodDrinksResults;
    }

    public void setAttractionsResults(String attractionsResults)
    {
        this.attractionsResults = attractionsResults;
    }

    @Override
    public Fragment getItem(int position)
    {
        Fragment fragment = null;

        System.out.println("nearby places tab position: " + position);

        if (position == 0){
            if (attractionsFragment == null)
                attractionsFragment = new NearbyPlacesTypeFragment().newInstance("Attractions" ,lat, lng, attractionsResults);
            fragment = attractionsFragment;
        }
            //fragment = new NearbyPlacesTypeFragment().newInstance("Attractions" ,lat, lng, attractionsResults);

        if (position == 1) {
            if (foodDrinksFragment == null)
                foodDrinksFragment = new NearbyPlacesTypeFragment().newInstance("Food & Drinks", lat, lng, foodDrinksResults);
            fragment = foodDrinksFragment;
        }
            //fragment = new NearbyPlacesTypeFragment().newInstance("Food & Drinks" ,lat, lng, foodDrinksResults);

        if (position == 2){
            if (accommodationsFragment == null)
                accommodationsFragment = new NearbyPlacesTypeFragment().newInstance("Accommodations" ,lat, lng, accommodationsResults);
            fragment = accommodationsFragment;
        }
            //fragment = new NearbyPlacesTypeFragment().newInstance("Accommodations" ,lat, lng,accommodationsResults);

        return fragment;
    }

    @Override
    public int getCount()
    {
        return NEARBY_PLACES_TABS_NUM;
    }
}
