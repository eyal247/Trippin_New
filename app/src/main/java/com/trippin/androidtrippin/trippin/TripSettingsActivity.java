package com.trippin.androidtrippin.trippin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.fragments.DatesPickerFragment;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.DateUtils;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.eTravelingWith;
import com.trippin.androidtrippin.model.eTripType;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class TripSettingsActivity extends MyActionBarActivity implements OnFragmentInteractionListener
{
    private Bitmap tripPhoto;
    private EditText tripNameET;
    private AutoCompleteTextView settingsDestACTV;
    private EditText settingsDepartureET;
    private EditText settingsReturnET;
    private Spinner settingsGoingSpinner;
    private Spinner settingsTypeSpinner;
    private FloatingActionButton editTripSettingsFAB;
    private FloatingActionButton saveFAB;
    private Button deleteButton;
    private CoordinatorLayout mainLayout;
    private ImageView tripCoverPhotoIV;
    private Button changePhotoButton;
    private Switch shareTripSwitch;
    private ArrayAdapter<String> goingAdapter;
    private ArrayAdapter<String> typeAdapter;
    private ArrayList<String> goingOptions = new ArrayList<>();
    private ArrayList<String> typeOptions = new ArrayList<>();
    private Drawable originalBackground;
    private Date tripDepartureDate;
    private Date tripReturnDate;
    private String dateTypeClicked;
    private int originalTextColor;
    private boolean shareTrip;
    private boolean saveClicked;
    private String lastName;
    private String lastDeparture;
    private String lastReturn;
    private int lastGoingWith;
    private int lastTripType;
    private Bitmap lastTripPhoto;
    private boolean lastShare;
    private boolean inEditMode;
    private boolean tripDeleted;
    private boolean userChangedPicture;
    private String tripID;

    private final Activity thisActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_settings);
        getUIComponents();
        getTripIDFromBundle();
        getDatesFromBundle();
        setShareTripSwitch();
        disableEdit();
        initGoingOptionsArray();
        initTypeOptionsArray();
        setGoingSpinnerAdapter();
        setTypeSpinnerAdapter();
        setUIComponents();
        AppUtils.hideKeyboard(this, findViewById(android.R.id.content).getWindowToken());
        setUIListeners();
    }

    private void getTripIDFromBundle()
    {
        tripID = getIntent().getExtras().getString("trip_id");
    }

    private void getDatesFromBundle()
    {
        String departureDate = getIntent().getExtras().getString("trip_departure");
        String returnDate = getIntent().getExtras().getString("trip_return");
        tripDepartureDate = DateUtils.stringToDate(departureDate);
        tripReturnDate = DateUtils.stringToDate(returnDate);
    }

    private void setShareTripSwitch()
    {
        shareTrip = getIntent().getExtras().getBoolean("share");

        if(shareTrip)
            shareTripSwitch.setChecked(true);
        else
            shareTripSwitch.setChecked(false);
    }

    private void setUIListeners()
    {
        editTripSettingsFAB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (inEditMode)
                    restoreLastSettings();
                else
                    enableEdit();
            }
        });

        saveFAB.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (AppUtils.isNetworkAvailable(TripSettingsActivity.this))
                    onSaveClicked();
                else {
                    //AppUtils.showNoInternetConnectionToast(TripSettingsActivity.this);
                    AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, thisActivity, mainLayout);
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (AppUtils.isNetworkAvailable(TripSettingsActivity.this))
                    onDeleteTrip();
                else
                    AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, thisActivity, mainLayout);
            }
        });

        changePhotoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openUploadProfilePicDialog();
            }
        });

        settingsDepartureET.setOnClickListener((
                new Button.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dateTypeClicked = "departureET";
                        handleDatePickerClicked(v);
                    }
                }
        ));

        settingsReturnET.setOnClickListener((
                new Button.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dateTypeClicked = "returnET";
                        handleDatePickerClicked(v);
                    }
                }
        ));

        shareTripSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                handleShareTripSwitch();
            }
        });
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
        startActivityForResult(cameraIntent, AppConstants.CAMERA_REQUEST_CODE + 1);
    }

    private void handleSelect()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), AppConstants.CAMERA_ROLL_REQUEST_CODE + 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap uploadedCoverPhoto;

        if (resultCode == RESULT_OK){
            if (requestCode == (AppConstants.CAMERA_ROLL_REQUEST_CODE+1)){
                try {
                    uploadedCoverPhoto = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    uploadedCoverPhoto = AppUtils.resizeBitmap(uploadedCoverPhoto, AppConstants.IMAGE_MAX_SIZE);
                    tripCoverPhotoIV.setImageBitmap(uploadedCoverPhoto);
                    userChangedPicture = true;
                    AppController.getInstance().getLruBitmapCache().putBitmap(tripID, uploadedCoverPhoto);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(requestCode == (AppConstants.CAMERA_REQUEST_CODE+1)){
                uploadedCoverPhoto = (Bitmap) data.getExtras().get("data");
                uploadedCoverPhoto = AppUtils.resizeBitmap(uploadedCoverPhoto, AppConstants.IMAGE_MAX_SIZE);
                tripCoverPhotoIV.setImageBitmap(uploadedCoverPhoto);
                userChangedPicture = true;
                AppController.getInstance().getLruBitmapCache().putBitmap(tripID, uploadedCoverPhoto);
            }
        }
    }

    private void restoreLastSettings()
    {
        inEditMode = false;
        editTripSettingsFAB.setImageResource(R.drawable.ic_create_black_24dp);

        tripNameET.setText(lastName);
        settingsDepartureET.setText(lastDeparture);
        settingsReturnET.setText(lastReturn);
        settingsGoingSpinner.setSelection(lastGoingWith);
        settingsTypeSpinner.setSelection(lastTripType);
        shareTrip = lastShare;
        shareTripSwitch.setChecked(lastShare);
        tripCoverPhotoIV.setImageBitmap(lastTripPhoto);

        disableEdit();
    }

    private void handleShareTripSwitch()
    {
        if(shareTripSwitch.isChecked())
            shareTrip = true;
        else
            shareTrip = false;
    }

    private void onDeleteTrip()
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE: //Yes button clicked
                        deleteTrip();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE: //No button clicked
                        // Nothin to do
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete trip?").setPositiveButton("Delete", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).show();
    }

    private void deleteTrip()
    {
        JSONObject deletePlaceJSON;
        String url = AppConstants.SERVER_URL + AppConstants.DELETE_TRIP;
        //String url = "http://192.168.207.205:3000/deleteTrip";

        try
        {
            deletePlaceJSON = new JSONObject();
            deletePlaceJSON.put("tripID", getIntent().getExtras().get("trip_id"));

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, deletePlaceJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    handleDeleteTripResponse(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on deleteTrip()");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteTripResponse(JSONObject response)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    tripDeleted = true;
                    //switchToHomeActivity();
                    switchToPreviousActivity();
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    Toast.makeText(this, "Couldn't delete trip. Server error.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    System.out.println("error on handleDeleteTripResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void switchToHomeActivity()
    {
        Intent intent = new Intent(TripSettingsActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    private void handleDatePickerClicked(View v)
    {
        Date otherTypeDate = checkOtherTypeDate();
        Date currDateInET = getCurrDateInET();
        DatesPickerFragment datesFragment = new DatesPickerFragment();
        Bundle argsBundle = setArgsBundle(currDateInET, otherTypeDate);
        datesFragment.setArguments(argsBundle);
        datesFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private Bundle setArgsBundle(Date currDateInEditText, Date otherTypeDate) {
        Bundle bundle = new Bundle();

        bundle.putString("date_type_clicked", dateTypeClicked);
        bundle.putString("curr_date", DateUtils.dateToString(currDateInEditText));
        bundle.putString("other_type_date", DateUtils.dateToString(otherTypeDate));

        return bundle;
    }

    private Date getCurrDateInET()
    {
        Date currDateInET = null;

        if(dateTypeClicked == "departureET" && !settingsDepartureET.getText().toString().equals(AppConstants.EMPTY_STRING))
            currDateInET = DateUtils.stringToDate(settingsDepartureET.getText().toString());
        else if (dateTypeClicked == "returnET" && !settingsReturnET.getText().toString().equals(AppConstants.EMPTY_STRING))
            currDateInET = DateUtils.stringToDate(settingsReturnET.getText().toString());

        return currDateInET;
    }

    private Date checkOtherTypeDate()
    {
        Date othertypeDate = null;

        if(dateTypeClicked == "departureET" && !settingsReturnET.getText().toString().equals(AppConstants.EMPTY_STRING))
            othertypeDate = DateUtils.stringToDate(settingsReturnET.getText().toString());
        else if (dateTypeClicked == "returnET" && !settingsDepartureET.getText().toString().equals(AppConstants.EMPTY_STRING))
            othertypeDate = DateUtils.stringToDate(settingsDepartureET.getText().toString());

        return othertypeDate;
    }

    private void onSaveClicked()
    {
        saveClicked = true;
        inEditMode = false;
        editTripSettingsFAB.setImageResource(R.drawable.ic_create_black_24dp);

        saveNewDetails();
        disableEdit();
        sendUpdatedTripSettingsToServer();
    }

    private void sendUpdatedTripSettingsToServer()
    {
        JSONObject updatedTripSettingsJSON;
        String url = AppConstants.SERVER_URL + AppConstants.UPDATE_TRIP_SETTINGS;
        //String url = "http://192.168.203.2:3000/updateTripSettings";

        updatedTripSettingsJSON = createUpdatedTripSettingsJSON();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, updatedTripSettingsJSON, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handleUpdateTripSettingsResponse(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("error response on sendUpdatedTripSettingsToServer()");
                    }
                });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MainActivity.addRequestToQueue(jsObjRequest);
    }

    private JSONObject createUpdatedTripSettingsJSON()
    {
        JSONObject updatedTripSettingsJSON = new JSONObject();

        String tripID = getIntent().getExtras().getString("trip_id");
        String imageID = getIntent().getExtras().getString("imageID");
        String tripCoverPhotoStr = AppUtils.bitMapToString(((BitmapDrawable) tripCoverPhotoIV.getDrawable()).getBitmap());
        try {
            updatedTripSettingsJSON.put("tripID", tripID);
            updatedTripSettingsJSON.put("name", tripNameET.getText().toString());
            updatedTripSettingsJSON.put("departureDate", DateUtils.dateToString(tripDepartureDate));
            updatedTripSettingsJSON.put("returnDate", DateUtils.dateToString(tripReturnDate));
            updatedTripSettingsJSON.put("goingWith", settingsGoingSpinner.getSelectedItem().toString());
            updatedTripSettingsJSON.put("type", settingsTypeSpinner.getSelectedItem().toString());
            updatedTripSettingsJSON.put("share", shareTrip);
            updatedTripSettingsJSON.put("mainPhoto", tripCoverPhotoStr);
            updatedTripSettingsJSON.put("imageID", imageID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return updatedTripSettingsJSON;
    }


    private void handleUpdateTripSettingsResponse(JSONObject response)
    {
        try
        {
            switch(response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    //handleUserDetailsResponse(response);
                    //Toast.makeText(this, "Trip settings updated successfully", Toast.LENGTH_LONG).show();
                    AppUtils.showSnackBarMsg("Trip settings updated successfully", thisActivity, mainLayout);
                    break;
                case AppConstants.RESPONSE_FAILURE:
//                    Toast.makeText(this, "Update of trip settings failed. Server error.", Toast.LENGTH_LONG).show();
                    AppUtils.showSnackBarMsg("Update of trip settings failed. Server error.", thisActivity, mainLayout);
                    break;
                default:
                    System.out.println("error on handleUpdateTripSettingsResponse();");
                    break;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveNewDetails()
    {
        tripNameET.setText(tripNameET.getText());
        //settingsDestACTV.setText(settingsDestACTV.getText());
        settingsDepartureET.setText(settingsDepartureET.getText());
        settingsReturnET.setText(settingsReturnET.getText());
        settingsGoingSpinner.setSelection(settingsGoingSpinner.getSelectedItemPosition());
        settingsTypeSpinner.setSelection(settingsTypeSpinner.getSelectedItemPosition());
        shareTripSwitch.setChecked(shareTripSwitch.isChecked());
    }

    private void setUIComponents()
    {
        setTripPhotoImageView();
        tripNameET.setText(getIntent().getExtras().getString("trip_name"));
        settingsDestACTV.setText(getIntent().getExtras().getString("trip_dest"));
        settingsDepartureET.setText(getIntent().getExtras().getString("trip_departure"));
        settingsReturnET.setText(getIntent().getExtras().getString("trip_return"));
        int goingPosition = getGoingSelectionPosition();
        int typePosition = getTypeSelectionPosition();
        settingsGoingSpinner.setSelection(goingPosition);
        settingsTypeSpinner.setSelection(typePosition);
    }

    private void setTripPhotoImageView()
    {
        String tripPhotoStr;
        String caller = getIntent().getExtras().getString("caller");

        if(caller != null)
        {
            tripPhotoStr = getIntent().getExtras().getString("trip_photo_str");
            if (tripPhotoStr != null)
                tripPhoto = AppUtils.stringToBitMap(tripPhotoStr);
            else
                tripPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.default_trip_image);
        }
        else
        {
            tripPhotoStr = AppController.getInstance().getCurrTripOnMap().getTripCoverPhotoStr();
            tripPhoto = AppUtils.stringToBitMap(tripPhotoStr);
        }

        tripCoverPhotoIV.setImageBitmap(tripPhoto);
    }

    private void disableEdit()
    {
        tripNameET.setEnabled(false);
        tripNameET.setTextColor(originalTextColor);
        settingsDepartureET.setEnabled(false);
        settingsReturnET.setTextColor(originalTextColor);
        settingsReturnET.setEnabled(false);
        settingsDepartureET.setTextColor(originalTextColor);
        settingsGoingSpinner.setEnabled(false);
        settingsTypeSpinner.setEnabled(false);
        tripNameET.setBackground(null);
        settingsDepartureET.setBackground(null);
        settingsReturnET.setBackground(null);
        settingsGoingSpinner.setBackground(null);
        settingsTypeSpinner.setBackground(null);
        shareTripSwitch.setEnabled(false);
        shareTripSwitch.setClickable(false);


        saveFAB.setVisibility(View.GONE);
        saveFAB.setClickable(false);
        saveFAB.setEnabled(false);


        deleteButton.setVisibility(View.GONE);
        deleteButton.setClickable(false);
        deleteButton.setEnabled(false);

        editTripSettingsFAB.setVisibility(View.VISIBLE);
        editTripSettingsFAB.setEnabled(true);
        editTripSettingsFAB.setClickable(true);

        changePhotoButton.setVisibility(View.INVISIBLE);
        changePhotoButton.setEnabled(false);
        changePhotoButton.setClickable(false);
    }

    private void enableEdit()
    {
        inEditMode = true;
        editTripSettingsFAB.setImageResource(R.drawable.ic_clear_black_24dp);
        saveLastSettings();

        tripNameET.setEnabled(true);
        tripNameET.setTextColor(getResources().getColor(R.color.black));
        settingsDepartureET.setEnabled(true);
        settingsDepartureET.setTextColor(getResources().getColor(R.color.black));
        settingsReturnET.setEnabled(true);
        settingsReturnET.setTextColor(getResources().getColor(R.color.black));
        settingsGoingSpinner.setEnabled(true);
        settingsTypeSpinner.setEnabled(true);
        tripNameET.setBackground(originalBackground);
        settingsDepartureET.setBackground(originalBackground);
        settingsReturnET.setBackground(originalBackground);
        settingsGoingSpinner.setBackground(originalBackground);
        settingsTypeSpinner.setBackground(originalBackground);
        shareTripSwitch.setEnabled(true);
        shareTripSwitch.setClickable(true);

        saveFAB.setVisibility(View.VISIBLE);
        saveFAB.setClickable(true);
        saveFAB.setEnabled(true);
        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setClickable(true);
        deleteButton.setEnabled(true);
        changePhotoButton.setVisibility(View.VISIBLE);
        changePhotoButton.setEnabled(true);
        changePhotoButton.setClickable(true);

//        editTripSettingsFAB.setVisibility(View.INVISIBLE);
//        editTripSettingsFAB.setEnabled(false);
//        editTripSettingsFAB.setClickable(false);
    }

    private void saveLastSettings()
    {
        lastName = tripNameET.getText().toString();
        lastDeparture = DateUtils.dateToString(tripDepartureDate);
        lastReturn = DateUtils.dateToString(tripReturnDate);
        lastTripType = settingsTypeSpinner.getSelectedItemPosition();
        lastGoingWith = settingsGoingSpinner.getSelectedItemPosition();
        lastShare = shareTrip;
        lastTripPhoto = tripPhoto;
    }

    private int getTypeSelectionPosition()
    {
        String type = getIntent().getExtras().getString("trip_type");
        int position = 0;

        switch(eTripType.fromStringToEnum(type))
        {
            case SIGHTSEEING:
                position = 0;
                break;
            case ROMANTIC:
                position = 1;
                break;
            case NATURE:
                position = 2;
                break;
            case BUSINESS:
                position = 3;
                break;
            case BEACHES:
                position = 4;
                break;
            case EXTREME:
                position = 5;
                break;
        }

        return position;
    }

    private int getGoingSelectionPosition()
    {
        String goingWith = getIntent().getExtras().getString("trip_going_with");
        int position = 0;

        switch(eTravelingWith.fromStringToEnum(goingWith))
        {
            case ALONE:
                position = 0;
                break;
            case COUPLE:
                position = 1;
                break;
            case FAMILY:
                position = 2;
                break;
            case FRIENDS:
                position = 3;
                break;
        }

        return position;
    }

    private void getUIComponents()
    {
        tripNameET = (EditText)findViewById(R.id.settings_trip_name_ET);
        settingsDestACTV = (AutoCompleteTextView)findViewById(R.id.settings_destination_AC);
        settingsDepartureET = (EditText)findViewById(R.id.settings_departureDateET);
        settingsReturnET = (EditText)findViewById(R.id.settings_returnDateET);
        settingsGoingSpinner = (Spinner)findViewById(R.id.settings_goingSpinner);
        settingsTypeSpinner = (Spinner)findViewById(R.id.settings_tripType_Spinner);
        saveFAB = (FloatingActionButton)findViewById(R.id.settings_save_FAB);
        deleteButton = (Button)findViewById(R.id.settings_delete_trip_Button);
        editTripSettingsFAB = (FloatingActionButton)findViewById(R.id.settings_edit_FAB);
        shareTripSwitch = (Switch)findViewById(R.id.share_trip_switch);
        tripCoverPhotoIV = (ImageView)findViewById(R.id.settings_cover_photo_IV);
        changePhotoButton = (Button) findViewById(R.id.settings_change_cover_photo_Button);
        mainLayout = (CoordinatorLayout)findViewById(R.id.settings_form_main_layout);

        originalBackground = tripNameET.getBackground();
        originalTextColor = settingsDestACTV.getCurrentTextColor();
    }

    private void setGoingSpinnerAdapter()
    {
        goingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, goingOptions);
        settingsGoingSpinner.setAdapter(goingAdapter);
    }

    private void setTypeSpinnerAdapter()
    {
        typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeOptions);
        settingsTypeSpinner.setAdapter(typeAdapter);
    }

    private void initTypeOptionsArray()
    {
        String[] options = {eTripType.SIGHTSEEING.strValue(),
                eTripType.ROMANTIC.strValue(),
                eTripType.NATURE.strValue(),
                eTripType.BUSINESS.strValue(),
                eTripType.BEACHES.strValue(),
                eTripType.EXTREME.strValue()};
        Collections.addAll(typeOptions, options);
    }

    private void initGoingOptionsArray()
    {
        String[] options = {eTravelingWith.ALONE.strValue(),
                eTravelingWith.COUPLE.strValue(),
                eTravelingWith.FAMILY.strValue(),
                eTravelingWith.FRIENDS.strValue()};
        Collections.addAll(goingOptions, options);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trip_settings, menu);
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
//            onBackPressed();

            switchToPreviousActivity();
            return true;
        }

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void switchToPreviousActivity()
    {
        Bundle bundle;

        if(tripDeleted == true)
            bundle = createBundleAfterTripDeleted();
        else if (inEditMode == true)
            bundle = createBundleFromLastSettings();
        else
            bundle = createBundleFromChangedSettings();

        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setResult(RESULT_OK, intent);
        finish();
    }

    private Bundle createBundleAfterTripDeleted()
    {
        Bundle bundle = new Bundle();

        bundle.putString("caller", "TripSettingsActivity");
        bundle.putBoolean("trip_deleted", true);
        bundle.putString("trip_id", getIntent().getStringExtra("trip_id"));

        return bundle;
    }

    private Bundle createBundleFromLastSettings()
    {
        Bundle bundle = new Bundle();
        String tripCoverPhotoStr = AppUtils.bitMapToString(lastTripPhoto);

        bundle.putString("caller", "TripSettingsActivity");
        bundle.putString("trip_name", lastName);
        bundle.putString("trip_departure", lastDeparture);
        bundle.putString("trip_return", lastReturn);
        bundle.putString("trip_type", settingsTypeSpinner.getItemAtPosition(lastTripType).toString());
        bundle.putString("trip_going_with", settingsGoingSpinner.getItemAtPosition(lastGoingWith).toString());
        bundle.putBoolean("share", lastShare);
        bundle.putString("trip_id", getIntent().getStringExtra("trip_id"));
        bundle.putBoolean("trip_deleted", false);
        bundle.putString("trip_photo", tripCoverPhotoStr);

        return bundle;
    }

    private Bundle createBundleFromChangedSettings()
    {
        Bundle bundle = new Bundle();
        String tripCoverPhotoStr = AppUtils.bitMapToString(((BitmapDrawable) tripCoverPhotoIV.getDrawable()).getBitmap());


        bundle.putString("caller", "TripSettingsActivity");
        bundle.putString("trip_name", tripNameET.getText().toString());
        bundle.putString("trip_departure", DateUtils.dateToString(tripDepartureDate));
        bundle.putString("trip_return", DateUtils.dateToString(tripReturnDate));
        bundle.putString("trip_type", settingsTypeSpinner.getSelectedItem().toString());
        bundle.putString("trip_going_with", settingsGoingSpinner.getSelectedItem().toString());
        bundle.putBoolean("share", shareTrip);
        bundle.putString("trip_id", getIntent().getStringExtra("trip_id"));
        bundle.putBoolean("trip_deleted", false);
        bundle.putString("trip_photo", tripCoverPhotoStr);

        return bundle;
    }

//    @Override
//    public void onBackPressed()
//    {
//        Intent intent = new Intent(this ,HomeActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putString("caller", );
//    }

    @Override
    public void onFragmentInteraction(String event, Object...objs)
    {
        int year = ((Bundle)objs[0]).getInt("year");
        int month = ((Bundle)objs[0]).getInt("month");
        int day = ((Bundle)objs[0]).getInt("day");

        if(dateTypeClicked == "departureET") {
            DateUtils.setDatesEditTexts(year, month, day, settingsDepartureET);
            tripDepartureDate = DateUtils.getDate(year, month, day);
        }
        else
        {
            DateUtils.setDatesEditTexts(year, month, day, settingsReturnET);
            tripReturnDate = DateUtils.getDate(year, month, day);
        }
    }
}
