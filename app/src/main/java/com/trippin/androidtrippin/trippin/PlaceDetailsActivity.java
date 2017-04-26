package com.trippin.androidtrippin.trippin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.GooglePlace;
import com.trippin.androidtrippin.model.GooglePlaceLoader;
import com.trippin.androidtrippin.model.GooglePlacesUtils;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.OnGooglePlaceLoadListener;
import com.trippin.androidtrippin.model.PhotoInfo;
import com.trippin.androidtrippin.model.SaveSharedPreference;
import com.trippin.androidtrippin.model.TripPlaceUtils;
import com.trippin.androidtrippin.trippin.MyActionBarActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlaceDetailsActivity extends MyActionBarActivity implements OnFragmentInteractionListener, OnGooglePlaceLoadListener
{
    private GooglePlace place = new GooglePlace();
    //private Trip currTrip;
    private TextView placeNameTV;
    private String tripID;
    private ImageView mainPlaceImage;
    private ImageButton leftArrow;
    private ImageButton rightArrow;
    private TextView placeAddressTV;
    private TextView placePhoneNumTV;
    private TextView placeRatingTV;
    private TextView placePriceLevelTV;
    private TextView placeWebsiteTV;
    private TextView descriptionTV;
    private TextView inYourTripTV;
    private RelativeLayout contentRL;
    private RelativeLayout progressBarRL;
    private Button placeDetailsButton;
    private int photosIndex = 0;
    private GooglePlaceLoader googlePlaceLoader;
    private ArrayList<String> myTripPlacesGoogleIDs = new ArrayList<>();
    private String placeID;
    private Boolean fullDescriptionIsShown;
    private String shortDescription;
    private String fullDescription;
    private NetworkImageView placeNetworkImageView;
    private ImageButton placeDetailsRefreshIB;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        //currTrip = getIntent().getParcelableExtra("tripObject");
        setContentView(R.layout.activity_place_details);
        getUIComponents();
        contentRL.setVisibility(View.INVISIBLE);
        setUIComponentsListeners();

        placeID = getPlaceID();
        //getPlaceDetails(placeID);
        String myTripID = SaveSharedPreference.getTripId(getApplicationContext());
        googlePlaceLoader = new GooglePlaceLoader(myTripPlacesGoogleIDs, myTripID, this, placeID, this);
        googlePlaceLoader.loadGooglePlace();
    }

//    private void getPlaceDetailsFromIntent()
//    {
//        String rating, address, website, phoneNumber, price_level;
//        Double lat, lng;
//        Bitmap placePhoto;
//        rating = getIntent().getExtras().getString("rating");
//        website = getIntent().getExtras().getString("website");
//        phoneNumber = getIntent().getExtras().getString("phone_number");
//        address = getIntent().getExtras().getString("address");
//        price_level = getIntent().getExtras().getString("price_level");
//        lat = getIntent().getExtras().getDouble("lat");
//        lng = getIntent().getExtras().getDouble("lng");
//        place.setLatitude(lat);
//        place.setLongitude(lng);
//        place.setRating(rating);
//        place.setWebsite(website);
//        place.setPriceLevel(price_level);
//        place.setInternationalPhoneNum(phoneNumber);
//        place.setAddress(address);
//        setUIComponents();
//    }

//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//        super.startCheckInternetConnection();
//    }
//
//    @Override
//    protected void onPause()
//    {
//        super.onPause();
//        super.stopCheckInternetConnection();
//    }

    private void setUIComponentsListeners()
    {
        placeDetailsButton.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(AppUtils.isNetworkAvailable(PlaceDetailsActivity.this))
                            addToTripMap();
                    }
                }
        );

        placeDetailsRefreshIB.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        googlePlaceLoader.loadGooglePlace();
                    }
                }
        );

        rightArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onPictureArrowClicked(AppConstants.RIGHT_ARROW_CLICKED);
            }
        });

        leftArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onPictureArrowClicked(!AppConstants.RIGHT_ARROW_CLICKED);
            }
        });

        descriptionTV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!descriptionTV.getText().equals(AppConstants.NO_PLACE_DESCRIPTION)) {
                    if (!fullDescriptionIsShown) {
                        descriptionTV.setText(fullDescription);
                        fullDescriptionIsShown = true;
                    } else {
                        descriptionTV.setText(shortDescription);
                        fullDescriptionIsShown = false;
                    }
                }
            }
        });

        placeAddressTV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showNavigateDialog();
            }
        });
    }

    private void showNavigateDialog()
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: //Yes button clicked
                        openGoogleMapsForNavigation();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE: //No button clicked
                        // Nothing to do
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Would you like to navigate here?").setPositiveButton("Navigate", dialogClickListener).setNegativeButton("Not now", dialogClickListener).show();
    }

    private void openGoogleMapsForNavigation()
    {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + place.getLatitude() + ", " + place.getLongitude() + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }


    private void onPictureArrowClicked(boolean arrowDirection)
    {
        System.out.println("onClick of mainImageView!!!");
        changePlacePhoto(arrowDirection);
    }

    private void changePlacePhoto(boolean arrowDirection)
    {
        Bitmap photo;

        setPhotoIndex(arrowDirection);

//        System.out.println("On changePlacePhoto()!!!");
//        photo = place.getPhoto(photosIndex);
//
//        System.out.println("photos index: " + photosIndex);
//
//        if (photo != null)
//        {
//            System.out.println("photo bitmap: " + photo.toString());
//            mainPlaceImage.setImageBitmap(photo);
//        }
//        else
//        {
//            System.out.println("photo is null");
//            loadImage(photosIndex);
//        }

        loadNetworkImage(photosIndex);
    }

    @Override
    public void onFragmentInteraction(String event, Object...objs)
    {
        if(event.equals("switchToPlaceDetails")){
            Bundle bundle = (Bundle)objs[0];

            Intent intent = new Intent(this, TripPlaceActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private void setPhotoIndex(boolean arrowDirection)
    {
        if(arrowDirection == AppConstants.RIGHT_ARROW_CLICKED)
        {
            if (photosIndex < place.getPlacePhotosInfo().size() - 1)
                photosIndex++;
            else
                photosIndex = 0;
        }

        else //left arrow was clicked
        {
            if (photosIndex == AppConstants.FIRST_PHOTO_INDEX)
                photosIndex = place.getPlacePhotosInfo().size() - 1;
            else
                photosIndex--;
        }
    }

    private void addToTripMap()
    {
        placeDetailsButton.setClickable(false);
        Bundle bundle = new Bundle();
        tripID = getIntent().getExtras().getString("trip_id");

        Bitmap placeImageBitmap = ((BitmapDrawable) placeNetworkImageView.getDrawable()).getBitmap();
        placeImageBitmap = Bitmap.createScaledBitmap(placeImageBitmap, 150, 150, false);
        String placeImageDecodedString = AppUtils.bitMapToString(placeImageBitmap);
        bundle.putString("image", placeImageDecodedString);
        bundle.putString("place_id", place.getPlaceID());
        bundle.putString("place_name", place.getName());
        bundle.putDouble("lat", place.getLatitude());
        bundle.putDouble("lng", place.getLongitude());
        bundle.putString("trip_id", tripID);
        bundle.putString("caller", "PlaceDetailsActivity");
        TripPlaceUtils.sendTripPlaceToServer(bundle, this);
    }

    public void startPlanTripActivity(Bundle bundle)
    {
        Intent mapIntent = new Intent(PlaceDetailsActivity.this, PlanTripActivity.class);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mapIntent.putExtras(bundle);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mapIntent);
    }

    private JSONObject createJSONFromTripPlaceBundle(Bundle bundle) throws JSONException
    {
        JSONObject tripPlaceJsonObject = new JSONObject();

        tripPlaceJsonObject.put("name", bundle.getString("place_name"));
//      tripPlaceJsonObject.put("mainPhoto", bundle.getString("image"));
        tripPlaceJsonObject.put("placeID", bundle.getString("place_id"));
        tripPlaceJsonObject.put("latitude", String.valueOf(bundle.getDouble("lat")));
        tripPlaceJsonObject.put("longitude", String.valueOf(bundle.getDouble("lng")));
        tripPlaceJsonObject.put("note", "");
        tripPlaceJsonObject.put("parentTripID", bundle.getString("trip_id"));
        tripPlaceJsonObject.put("note", "");

        return tripPlaceJsonObject;
    }

    private void getUIComponents()
    {
        contentRL = (RelativeLayout) findViewById(R.id.place_details_content_rl);
        progressBarRL = (RelativeLayout) findViewById(R.id.loading_panel_place_details);
        placeNameTV = (TextView)findViewById(R.id.place_name_tv);
        inYourTripTV = (TextView)findViewById(R.id.in_your_trip_activity_TV);
        descriptionTV = (TextView)findViewById(R.id.place_description_tv);
//        mainPlaceImage = (ImageView)findViewById(R.id.main_place_image);
        placeNetworkImageView = (NetworkImageView) findViewById(R.id.main_place_image);
        setPlaceNetworkIVDefaults();
        placeAddressTV = (TextView)findViewById(R.id.place_address_tv);
        placePhoneNumTV = (TextView)findViewById(R.id.place_phone_tv);
        placeRatingTV = (TextView)findViewById(R.id.place_rating_tv);
        placePriceLevelTV = (TextView)findViewById(R.id.place_price_level_tv);
        placeWebsiteTV = (TextView)findViewById(R.id.place_website_tv);
        leftArrow = (ImageButton)findViewById(R.id.left_arrow);
        rightArrow = (ImageButton)findViewById(R.id.right_arrow);
        placeDetailsButton = (Button)findViewById(R.id.place_details_button);
        placeDetailsRefreshIB = (ImageButton)findViewById(R.id.place_details_activity_refresh_button);
    }

    private void setPlaceNetworkIVDefaults()
    {
        placeNetworkImageView.setDefaultImageResId(R.drawable.loading);
        placeNetworkImageView.setErrorImageResId(R.drawable.no_photo_available);
    }

//    private void getPlaceDetails(final String placeID)
//    {
//        new AsyncTask()
//        {
//            String results;
//
//            @Override
//            protected Object doInBackground(Object[] params)
//            {
//                String placeDetailsRequestURL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeID + "&key=" + GoogleServerKey.GOOGLE_SERVER_KEY;
//                results = GooglePlacesUtils.makeCall(placeDetailsRequestURL);
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Object o)
//            {
//                super.onPostExecute(o);
//
//                place = GooglePlacesUtils.parseGooglePlacesDetailsJson(results);
//                setUIComponents();
//                progressBarRL.setVisibility(View.GONE);
//                contentRL.setVisibility(View.VISIBLE);
//            }
//        }.execute();
//    }

    private void setUIComponents()
    {
        setPlaceDetailsButtonText();
        setActionBarTitle(place.getName());
        placeNameTV.setText(place.getName());
        //placeAddressTV.setText(place.getAddress());
        setAndUnderlineAddressTV();

        placePhoneNumTV.setText(place.getInternationalPhoneNum());
        setDescriptionGravity();
        createDescriptionSubString();
        setDescriptionTvText();

        Linkify.addLinks(placePhoneNumTV, Linkify.PHONE_NUMBERS);
        placeRatingTV.setText(place.getRating());
        placeWebsiteTV.setText(place.getWebsite());
        Linkify.addLinks(placeWebsiteTV, Linkify.WEB_URLS);

        setPriceLevelTV();
        loadNetworkImage(0);
        //loadAllImages();
    }

    private void setAndUnderlineAddressTV()
    {
        SpannableString address = new SpannableString(place.getAddress());
        address.setSpan(new UnderlineSpan(), 0, address.length(), 0);
        placeAddressTV.setText(address);
    }

    private void setDescriptionTvText()
    {
        if(!fullDescription.equals(AppConstants.EMPTY_STRING)) {
            if (fullDescriptionIsShown)
                descriptionTV.setText(fullDescription);
            else
                descriptionTV.setText(shortDescription);
        }
        else
            descriptionTV.setText(AppConstants.NO_PLACE_DESCRIPTION);
    }

    private void createDescriptionSubString()
    {
        fullDescription = place.getDescription();
        shortDescription = fullDescription;

        if(fullDescription.length() > 100) {
            shortDescription = fullDescription.substring(0, 100) + "...";
            fullDescriptionIsShown = false;
        } else {
            fullDescriptionIsShown = true;
        }
    }

    private void setDescriptionGravity()
    {
        if(place.getDescription().equals(""))
            descriptionTV.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    private void setPlaceDetailsButtonText()
    {
        boolean found = false;

        for (int i = 0; i < myTripPlacesGoogleIDs.size() && !found; i++)
        {
            if (placeID.equals(myTripPlacesGoogleIDs.get(i)))
            {
                placeDetailsButton.setVisibility(View.INVISIBLE);
                inYourTripTV.setVisibility(View.VISIBLE);
                placeDetailsButton.setClickable(false);
                found = true;
            }
        }
        if (!found) {
            placeDetailsButton.setVisibility(View.VISIBLE);
            inYourTripTV.setVisibility(View.INVISIBLE);
        }
    }

    private void loadAllImages()
    {
        //place.loadAllImages();
    }

    private void setPriceLevelTV()
    {
        String priceLevel = place.getPriceLevel();

        switch (priceLevel)
        {
            case "0":
                placePriceLevelTV.append(" Free");
                break;
            case "1":
                placePriceLevelTV.append(" Inexpensive");
                break;
            case "2":
                placePriceLevelTV.append(" Moderate");
                break;
            case "3":
                placePriceLevelTV.append(" Expensive");
                break;
            case "4":
                placePriceLevelTV.append(" Very Expensive");
                break;
            default:
                placePriceLevelTV.append(" Not Available");
                break;
        }
    }

    private void loadImage(final int index)
    {
        new AsyncTask()
        {
            Bitmap mainPhotoBitmap = null;
            PhotoInfo mainPhotoInfo = place.getPhotoInfoFromList(index);

            @Override
            protected Object doInBackground(Object[] params)
            {
                if (mainPhotoInfo != null)
                    mainPhotoBitmap = GooglePlacesUtils.makePhotoCall(mainPhotoInfo.getPhotoRef(), mainPhotoInfo.getMaxHeight());

                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                super.onPostExecute(o);

                if(mainPhotoBitmap != null)
                {
                    mainPlaceImage.setImageBitmap(mainPhotoBitmap);
                    place.addPhoto(mainPhotoBitmap);
                }else{
                    mainPlaceImage.setImageResource(R.drawable.no_photo_available);
                }
            }
        }.execute();
    }

    private void loadNetworkImage(int index)
    {
        String imageUrl = GooglePlacesUtils.createPlaceImageUrl(place.getPhotoInfoFromList(index));

        if (imageUrl == null)
        {
            placeNetworkImageView.setDefaultImageResId(R.drawable.no_photo_available);
            placeNetworkImageView.setImageResource(R.drawable.no_photo_available);
        }
        else{
            placeNetworkImageView.setImageUrl(imageUrl, AppController.getInstance().getImageLoader());
        }
    }

    private String getPlaceID()
    {
        String placeId = getIntent().getExtras().getString("place_id");

        return placeId;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            Intent intent = NavUtils.getParentActivityIntent(this);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavUtils.navigateUpTo(this, intent);

            return true;
        }

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGooglePlaceLoadFinished(String event, Object obj)
    {
        switch(event){
            case AppConstants.RESPONSE_SUCCESS:
                place = (GooglePlace)obj;
                showArrowsIfNeeded();
                setUIComponents();
                placeDetailsRefreshIB.setVisibility(View.GONE);
                progressBarRL.setVisibility(View.GONE);
                contentRL.setVisibility(View.VISIBLE);
                break;
            case AppConstants.RESPONSE_FAILURE:
                progressBarRL.setVisibility(View.GONE);
                placeDetailsRefreshIB.setVisibility(View.VISIBLE);
                //placeDetailsRefreshIB.setZ(1.0f);
                break;
            default:
                System.out.println("error on onGooglePlaceLoadFinished place details activity switch case");
        }

    }

    private void showArrowsIfNeeded()
    {
        if(place.getPlacePhotosInfo().size() > 1){
            leftArrow.setVisibility(View.VISIBLE);
            rightArrow.setVisibility(View.VISIBLE);
        }
    }
}
