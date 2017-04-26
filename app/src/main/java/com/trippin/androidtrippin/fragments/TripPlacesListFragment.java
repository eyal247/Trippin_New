package com.trippin.androidtrippin.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.trippin.androidtrippin.adapters.TripPlacesListArrayAdapter;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.Trip;
import com.trippin.androidtrippin.model.TripPlace;
import com.trippin.androidtrippin.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TripPlacesListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripPlacesListFragment extends Fragment
{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String CURR_TRIP = "currTrip";

    private View mainView;
    private Trip currTrip;
    private ListView tripPlacesListView;
    private TripPlacesListArrayAdapter myAdapter;
    private OnFragmentInteractionListener mListener;
    private TextView noTripPlacesTV;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param currTrip Parameter 1.
     * @return A new instance of fragment TripPlacesListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TripPlacesListFragment newInstance(Trip currTrip)
    {
        TripPlacesListFragment fragment = new TripPlacesListFragment();
        Bundle args = new Bundle();
        args.putParcelable(CURR_TRIP, currTrip);
        fragment.setArguments(args);
        return fragment;
    }

    public TripPlacesListFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            currTrip = getArguments().getParcelable(CURR_TRIP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_trip_places_list, container, false);

        getUIComponents();
        tripPlacesListView.setOnItemClickListener(new TripPlacesListItemClickListener());
        createAndSetAdapter();

        return mainView;
    }

    private void createAndSetAdapter()
    {
        ArrayList<String> placesNames = new ArrayList<>();
        ArrayList<Bitmap> placesPhotos = new ArrayList<>();
        int numOfPlaces = currTrip.getNumOfTripPlaces();

        if (numOfPlaces == 0)
        {
            noTripPlacesTV.setVisibility(View.VISIBLE);
        }
        else
        {
            for (int i = 0; i < numOfPlaces; i++)
            {
                System.out.println("numOfPlaces: " + numOfPlaces);
                TripPlace place = currTrip.getTripPlace(i);
                placesNames.add(place.getName());
                placesPhotos.add(place.getMainPhotoBitmap());
            }
        }

        myAdapter = new TripPlacesListArrayAdapter(getActivity(), placesNames, placesPhotos);
        tripPlacesListView.setAdapter(myAdapter);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        System.out.println("TRIP PLACES LIST FRAG ON RESUME");
    }

    private void getUIComponents()
    {
        tripPlacesListView = (ListView) mainView.findViewById(R.id.trip_places_list_view_frag);
        noTripPlacesTV = (TextView) mainView.findViewById(R.id.no_trip_places_TV);
        noTripPlacesTV.setVisibility(View.GONE);
    }

    private class TripPlacesListItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            System.out.println("List item number "+ position +" was clicked!!!!");
            handleTripPlaceItemClick(position);
        }
    }

    private void handleTripPlaceItemClick(int position)
    {
        TripPlace place = currTrip.getTripPlace(position);
        Bundle bundle = new Bundle();
        bundle.putString("caller", "InfoWindowClick");
        bundle.putString("place_id", place.getPlaceID());
        bundle.putString("place_name", place.getName());
        bundle.putString("place_server_id", place.getServerID());
        bundle.putString("note_title", place.getNote().getNoteTitle());
        bundle.putString("note_date", place.getNote().getNoteDate());
        bundle.putString("note_text", place.getNote().getNoteText());
        bundle.putString("note_id", place.getNote().getNoteID());
        System.out.println(bundle.toString());
        mListener.onFragmentInteraction("switchToTripPlaceActivity", bundle);
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    public void removeTripPlace(){
        createAndSetAdapter();
    }
}
