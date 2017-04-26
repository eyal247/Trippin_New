package com.trippin.androidtrippin.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.trippin.androidtrippin.adapters.NearbyPlacesListArrayAdapter;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.GooglePlace;
import com.trippin.androidtrippin.model.GooglePlacesUtils;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.PhotoInfo;
import com.trippin.androidtrippin.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NearbyPlacesTypeFragment#newInstance} ffmactory method to
 * create an instance of this fragment.
 */
public class NearbyPlacesTypeFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TYPE = "type";
    private static final String LAT = "lat";
    private static final String LNG = "lng";
    private static final String RESULTS = "results";

    // TODO: Rename and change types of parameters
    private String type;
    private Double lat;
    private Double lng;
    private String results;
    private OnFragmentInteractionListener mListener;
    private View mainView;

    private ArrayList nearbyPlacesList = null;
    private ListView nearbyPlacesListView;
    private NearbyPlacesListArrayAdapter myAdapter;
    private List<String> placesNamesList = new ArrayList();
    private List<String> placesIDs = new ArrayList();
    private TextView noNearbyPlacesTV;
    private FrameLayout fragMainLayout;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param type Parameter 1.
     * @param lat Parameter 2.
     * @param lng Parameter 3.
     * @param results Parameter 4.
     * @return A new instance of fragment NearbyPlacesTypeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NearbyPlacesTypeFragment newInstance(String type, Double lat, Double lng, String results)
    {
        NearbyPlacesTypeFragment fragment = new NearbyPlacesTypeFragment();
        Bundle args = new Bundle();
        args.putString(TYPE, type);
        args.putDouble(LAT, lat);
        args.putDouble(LNG, lng);
        args.putString(RESULTS, results);
        fragment.setArguments(args);
        return fragment;
    }

    public NearbyPlacesTypeFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        System.out.println("In nearby fragment oncreate!");
        if (getArguments() != null) {
            type = getArguments().getString(TYPE);
            lat = getArguments().getDouble(LAT);
            lng = getArguments().getDouble(LNG);
            results = getArguments().getString(RESULTS);
        }
    }

    private void getUIComponents()
    {
        nearbyPlacesListView = (ListView) mainView.findViewById(R.id.nearby_places_list_frag);
        noNearbyPlacesTV = (TextView) mainView.findViewById(R.id.no_nearby_places_TV);
        noNearbyPlacesTV.setVisibility(View.GONE);
        fragMainLayout = (FrameLayout)mainView.findViewById(R.id.fragment_nearby_type_main_layout);
    }

    private class NearbyListItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            if(AppUtils.isNetworkAvailable(getActivity())){
                System.out.println("List item number "+ position +" was clicked!!!!");
                handleNearbyItemClick(position);
            }
            else{
                //AppUtils.showNoInternetConnectionToast(getActivity());
                AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, getActivity(), fragMainLayout);
            }
        }
    }

    private void handleNearbyItemClick(int position)
    {
        Bundle bundle = new Bundle();

        bundle.putString("caller", "NearbyPlacesActivity");
        bundle.putString("place_id", placesIDs.get(position));
//        bundle.putString("place_name", placesNamesList.get(position));
        bundle.putBoolean("addPlace", true);
        //bundle.putParcelable("tripObject", currTrip);
        mListener.onFragmentInteraction("switchToPlaceDetails", bundle);


//        Intent placeDetailsIntent = new Intent(NearbyPlacesActivity.this, PlaceDetailsActivity.class);
//        placeDetailsIntent.putExtras(bundle);
//        placeDetailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        startActivity(placeDetailsIntent);
//        //startActivityForResult(placeDetailsIntent, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_nearby_places_type, container, false);

        getUIComponents();
        nearbyPlacesListView.setOnItemClickListener(new NearbyListItemClickListener());
//        if (nearbyPlacesList == null)
//            getPlacesList();
        prepareInputForListAdapterAndSetAdapter(results);

        return mainView;
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }


//    public void getPlacesList()
//    {
//        new AsyncTask(){
//
//            private String results;
//
//            @Override
//            protected Object doInBackground(Object[] params)
//            {
//                String types = getStringTypesForURL();
//
//                results = GooglePlacesUtils.makeCall("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lng + "&radius=10000&rankby=prominence&types="+ types + "&key=" + GoogleServerKey.GOOGLE_SERVER_KEY);
//
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Object o)
//            {
//                super.onPostExecute(o);
//
//
//                if (results == null)
//                {
//                    // we have an error to the call
//                    // we can also stop the progress bar
//                }
//                else
//                {
//                    ArrayList<PhotoInfo> photosInfoList = new ArrayList<>();
//                    PhotoInfo currPhotoInfo;
//                    // parse Google places search result
//                    nearbyPlacesList = GooglePlacesUtils.parseGooglePlacesNearbyJson(results);
//
//                    for (int i = 0; i < nearbyPlacesList.size(); i++)
//                    {
//                        placesNamesList.add(i, ((GooglePlace)nearbyPlacesList.get(i)).getName());
//                        placesIDs.add(i, ((GooglePlace)nearbyPlacesList.get(i)).getPlaceID());
//                        currPhotoInfo = ((GooglePlace)nearbyPlacesList.get(i)).getMainPhotoInfo();
//                        photosInfoList.add(i, currPhotoInfo);
//                    }
//
//                    setAdapterToNearbyPlaceListView(photosInfoList);
//                }
//            }
//        }.execute();
//    }

    private void prepareInputForListAdapterAndSetAdapter(String results){
        ArrayList<PhotoInfo> photosInfoList = new ArrayList<>();
        PhotoInfo currPhotoInfo;
        // parse Google places search result
        nearbyPlacesList = GooglePlacesUtils.parseGooglePlacesNearbyJson(results);

        for (int i = 0; i < nearbyPlacesList.size(); i++)
        {
            placesNamesList.add(i, ((GooglePlace)nearbyPlacesList.get(i)).getName());
            placesIDs.add(i, ((GooglePlace)nearbyPlacesList.get(i)).getPlaceID());
            currPhotoInfo = ((GooglePlace)nearbyPlacesList.get(i)).getMainPhotoInfo();
            photosInfoList.add(i, currPhotoInfo);
        }
        setAdapterToNearbyPlaceListView(photosInfoList);
    }

    private void setAdapterToNearbyPlaceListView(ArrayList<PhotoInfo> photoInfoList)
    {
        if(AppUtils.isNetworkAvailable(getActivity()))
        {
            if (placesNamesList.size() == 0)
                noNearbyPlacesTV.setVisibility(View.VISIBLE);

            if (myAdapter == null)
                myAdapter = new NearbyPlacesListArrayAdapter(getActivity(), nearbyPlacesList, placesIDs, photoInfoList);

            nearbyPlacesListView.setAdapter(myAdapter);
        }
        else
        {
            //AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, getActivity(), fragMainLayout);
        }
    }

//    private String getStringTypesForURL()
//    {
//        String types = null;
//
//        switch (type){
//            case "Accommodations":
//                types = "lodging|campground";
//                break;
//            case "Food & Drinks":
//                types = "food|restaurant|bakery|bar|meal_delivery|meal_takeaway|cafe|convenience_store|grocery_or_supermarket|liquor_store";
//                break;
//            case "Attractions":
//                types = "amusement_park|aquarium|art_gallery|library|bowling_alley|mosque|movie_theater|museum|night_club|" +
//                        "casino|park|cemetery|church|city_hall|place_of_worship|courthouse|embassy|shopping_mall|" +
//                        "spa|stadium|synagogue|university|hindu_temple|zoo";
//                break;
//            default:
//                System.out.println("Places Types error");
//                break;
//        }
//
//        try {
//
//            // DONT EVER FORGET URLENCODER
//
//            types = URLEncoder.encode(types, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return types;
//    }
}
