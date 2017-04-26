package com.trippin.androidtrippin.trippin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.trippin.androidtrippin.trippin.MyActionBarActivity;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.TripPlaceUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AddPlaceRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Collections;

public class LongClickPlaceFormActivity extends MyActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    //TODO: Watch all trip photos - sorted by places or not...
    //TODO: Rank your top 3/5 places of your trip

    private String newPlaceID;
    private String tripID;
    private Button addNewPlaceToTripButton;
    private EditText newPlaceNameET;
    private EditText newPlaceAddressET;
    private EditText newPlacePhoneET;
    private EditText newPlaceWebsiteET;
    private ImageView newPlaceIV;
    private Button addNewPlacePhotoButton;
    private Bitmap newPlaceImageBitmap;
    private GoogleApiClient mGoogleApiClient;
    private final static String TAG = "LongClickActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_click_place_form);

        initGoogleApiClient();
        getTripID();
        getUIComponents();
        setUIListeners();
    }

    private void getTripID()
    {
        this.tripID =  getIntent().getExtras().getString("trip_id");
    }

    private void initGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void setUIListeners()
    {
        addNewPlacePhotoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openUploadProfilePicDialog();
            }
        });

        addNewPlaceToTripButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkIfValidInput()) {
                    addPlaceToGoogleDbAndToTrip();
                }
            }
        });
    }

    private void addPlaceToGoogleDbAndToTrip()
    {
        final Double lat = getIntent().getExtras().getDouble("lat");
        final Double lng = getIntent().getExtras().getDouble("lng");
        final String placeName = newPlaceNameET.getText().toString();
        final String address = newPlaceAddressET.getText().toString();
        final String website = newPlaceWebsiteET.getText().toString();
        final String phoneNumber = newPlacePhoneET.getText().toString();
        LatLng latlng = new LatLng(lat, lng);


        AddPlaceRequest place =
                new AddPlaceRequest(
                        placeName, // Name
                        latlng, // Latitude and longitude
                        address, // Address
                        Collections.singletonList(Place.TYPE_POINT_OF_INTEREST), // Place types
                        phoneNumber, // Phone number
                        Uri.parse(website) // Website
                );

        Places.GeoDataApi.addPlace(mGoogleApiClient, place).setResultCallback(new ResultCallback<PlaceBuffer>()
        {
            @Override
            public void onResult(PlaceBuffer places)
            {

                if (!places.getStatus().isSuccess()) {

                    Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                    places.release();
                    return;
                }

                final Place place = places.get(0);
                newPlaceID = place.getId();
                addNewPlaceTotrip(place.getName().toString(), place.getLatLng().latitude, place.getLatLng().longitude);
                Log.i(TAG, "Place add result: " + place.getName());
                places.release();
            }
        });
    }

    private void addNewPlaceTotrip(String placeName, Double lat, Double lng)
    {
        addNewPlaceToTripButton.setClickable(false);
        Bundle bundle = new Bundle();

        String placeImageDecodedString = getPlaceImageStringIfHasImage();
        bundle.putString("image", placeImageDecodedString);
        bundle.putString("place_id", newPlaceID);
        bundle.putString("place_name", placeName);
        bundle.putDouble("lat", lat);
        bundle.putDouble("lng", lng);
        bundle.putString("trip_id", tripID);
        bundle.putString("caller", "PlaceDetailsActivity");
        TripPlaceUtils.sendTripPlaceToServer(bundle, this);
    }

    private String getPlaceImageStringIfHasImage()
    {
        String placeImageDecodedString;
        Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_photo_available);
        Bitmap placeImageBitmap = ((BitmapDrawable) newPlaceIV.getDrawable()).getBitmap();

        if(placeImageBitmap != null) {
            placeImageBitmap = Bitmap.createScaledBitmap(placeImageBitmap, 150, 150, false);
            placeImageDecodedString = AppUtils.bitMapToString(placeImageBitmap);
        }
        else
            placeImageDecodedString = AppUtils.bitMapToString(defaultBitmap);


        return placeImageDecodedString;
    }

    private boolean checkIfValidInput()
    {
        boolean validInput = true;

        if(newPlaceNameET.getText().toString().equals(AppConstants.EMPTY_STRING)){
            validInput = false;
            Toast.makeText(this, "Place name field is required", Toast.LENGTH_LONG).show();
        }
        if(newPlaceAddressET.getText().toString().equals(AppConstants.EMPTY_STRING)){
            validInput = false;
            Toast.makeText(this, "Address field is required", Toast.LENGTH_LONG).show();
        }

        return validInput;
    }

    public void startPlanTripActivity(Bundle bundle)
    {
        Intent mapIntent = new Intent(LongClickPlaceFormActivity.this, PlanTripActivity.class);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mapIntent.putExtras(bundle);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mapIntent);
    }

    private void getUIComponents()
    {
        addNewPlaceToTripButton = (Button) findViewById(R.id.add_long_click_place_BTN);
        newPlaceNameET = (EditText) findViewById(R.id.new_place_name_ET);
        newPlaceAddressET = (EditText) findViewById(R.id.new_place_adress_ET);
        newPlacePhoneET = (EditText) findViewById(R.id.new_place_phone_ET);
        newPlaceWebsiteET = (EditText) findViewById(R.id.new_place_website_ET);
        newPlaceIV = (ImageView) findViewById(R.id.new_place_image);
        addNewPlacePhotoButton = (Button) findViewById(R.id.add_new_place_photo_Button);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == this.RESULT_OK){
            if (requestCode == AppConstants.CAMERA_ROLL_REQUEST_CODE){
                try {
                    newPlaceImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    newPlaceImageBitmap = AppUtils.resizeBitmap(newPlaceImageBitmap, AppConstants.IMAGE_MAX_SIZE);
                    newPlaceIV.setImageBitmap(newPlaceImageBitmap);
                    editAddPhotoButtonText();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(requestCode == (AppConstants.CAMERA_REQUEST_CODE)){
                newPlaceImageBitmap = (Bitmap) data.getExtras().get("data");
                newPlaceImageBitmap = AppUtils.resizeBitmap(newPlaceImageBitmap, AppConstants.IMAGE_MAX_SIZE);
                newPlaceIV.setImageBitmap(newPlaceImageBitmap);
                editAddPhotoButtonText();
                AppController.getInstance().getLruBitmapCache().putBitmap(tripID, newPlaceImageBitmap);
            }
        }
    }

    private void editAddPhotoButtonText()
    {
        addNewPlacePhotoButton.setText("Edit Place Photo");
    }

    private void openUploadProfilePicDialog()
    {
        String[] imageOptions = {"Camera Roll", "Take a Photo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.changeProfilePicOptions)
                .setItems(imageOptions, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //disableAddPhotoButton();
                        executeSelectedOption(which);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void executeSelectedOption(int which)
    {
        switch (which){
            case AppConstants.CAMERA_ROLL_REQUEST_CODE-1:
                handleSelect();
                break;
            case AppConstants.CAMERA_REQUEST_CODE-1:
                handleOpenCamera();
                break;
            default:
                System.out.println("in executeSelectedOption default myProfileFragment");
        }
    }

    private void handleOpenCamera()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, AppConstants.CAMERA_REQUEST_CODE);
    }

    private void handleSelect()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), AppConstants.CAMERA_ROLL_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_long_click_place_form, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle)
    {

    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }
}
