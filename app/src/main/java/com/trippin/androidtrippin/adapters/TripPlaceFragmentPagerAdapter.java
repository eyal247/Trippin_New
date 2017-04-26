package com.trippin.androidtrippin.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.trippin.androidtrippin.fragments.MyPlacePhotosFragment;
import com.trippin.androidtrippin.fragments.NoteFragment;
import com.trippin.androidtrippin.fragments.PlaceDetailsFragment;


public class TripPlaceFragmentPagerAdapter extends FragmentPagerAdapter
{
    public static final int NUM_OF_PAGES = 3;
    private String placeID;
    private String placeName;
    private String noteTitle;
    private String noteDate;
    private String noteText;
    private String noteID;
    private Fragment placeDetailsFragment;
    private Fragment myPlacePhotosFragment;
    private Fragment noteFragment;

    public String getNoteTitle()
    {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle)
    {
        this.noteTitle = noteTitle;
    }

    public String getNoteDate()
    {
        return noteDate;
    }

    public void setNoteDate(String noteDate)
    {
        this.noteDate = noteDate;
    }

    public String getNoteText()
    {
        return noteText;
    }

    public void setNoteText(String noteText)
    {
        this.noteText = noteText;
    }

    public void setNoteID(String noteID)
    {
        this.noteID = noteID;
    }

    public String getPlaceName()
    {
        return placeName;
    }

    public void setPlaceName(String placeName)
    {
        this.placeName = placeName;
    }

    public String getPlaceID()
    {

        return placeID;
    }

    public Fragment getMyPlacePhotosFragment()
    {
        return myPlacePhotosFragment;
    }

    public void setPlaceID(String placeID)
    {
        this.placeID = placeID;
    }

    public TripPlaceFragmentPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        Fragment fragment = null;

        if (position == 0) {
            if (placeDetailsFragment == null)
                placeDetailsFragment = new PlaceDetailsFragment().newInstance(placeID, placeName);

            fragment = placeDetailsFragment;
        }
        else if (position == 1) {
            if (myPlacePhotosFragment == null)
                myPlacePhotosFragment = new MyPlacePhotosFragment();

            fragment = myPlacePhotosFragment;
        }
        else if (position == 2) {
            if (noteFragment == null)
                noteFragment = new NoteFragment().newInstance(noteTitle, noteDate, noteText, noteID);

            fragment = noteFragment;
        }

        return fragment;
    }

    @Override
    public int getCount()
    {
        return NUM_OF_PAGES;
    }

}
