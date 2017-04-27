package com.trippin.androidtrippin.trippin;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.daimajia.androidanimations.library.Techniques;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.fragments.DatesPickerFragment;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.DateUtils;
import com.trippin.androidtrippin.model.GooglePlace;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.PlaceJSONParser;
import com.trippin.androidtrippin.model.SaveSharedPreference;
import com.trippin.androidtrippin.model.Trip;
import com.trippin.androidtrippin.model.eTravelingWith;
import com.trippin.androidtrippin.model.eTripType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class NewTripForm extends MyActionBarActivity implements OnFragmentInteractionListener
{
    private static final ArrayList<String> destinations = new ArrayList<>();
    private ArrayList<String> goingOptions = new ArrayList<>();
    private ArrayList<String> typeOptions = new ArrayList<>();
    private ArrayAdapter<String> goingAdapter;
    private ArrayAdapter<String> typeAdapter;
    private AutoCompleteTextView destACTV;
    private EditText tripNameET;
    private EditText departureET;
    private EditText returnET;
    private Spinner goingSpinner;
    private Spinner typeSpinner;
    private Button createTripButton;
    private Trip trip;
    private Double destLat;
    private Double destLng;
    private Date tripDepartureDate;
    private Date tripReturnDate;
    private String dateTypeClicked;
    private GooglePlace actvChosenPlace;
    private PlacesTask placesTask;
    private ParserTask parserTask;
    private String destination;
    private RelativeLayout mainLayout;
    private final Activity currActivity = this;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip_form);
        getUIComponents();
        //initDestinationsArray();
        initGoingOptionsArray();
        initTypeOptionsArray();
        setGoingSpinnerAdapter();
        setTypeSpinnerAdapter();
        //createDestinationAutoComplete();
        setUIComponentsListeners();
    }

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
        mainLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                setAutoCompleteTVListener();
            }
        });

        createTripButton.setOnClickListener(
                new Button.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (isInputValid())
                        {
                            if (AppUtils.isNetworkAvailable(NewTripForm.this))
                                handleCreateTripClick(v);
                            else
                                AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, currActivity, mainLayout);
                        }
                    }
                }
        );

        departureET.setOnClickListener((
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

        returnET.setOnClickListener((
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
    }

    private void setAutoCompleteTVListener()
    {
        destACTV.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                //placesTask = new PlacesTask();
                if (AppUtils.isNetworkAvailable(NewTripForm.this))
                    showDropDown(s);
                else
                    AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, currActivity, mainLayout);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after)
            {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // TODO Auto-generated method stub
            }

        });

        destACTV.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId)
            {
                HashMap<String, String> resultHM = (HashMap<String, String>) parent.getItemAtPosition(position);
                myDismissDropDown();
                AppUtils.hideKeyboard(currActivity, findViewById(android.R.id.content).getWindowToken());
            }
        });
    }

    private void myDismissDropDown()
    {
        new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] params)
            {
                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                super.onPostExecute(o);

                destACTV.dismissDropDown();
                destACTV.clearComposingText();
            }
        }.execute();
    }

    private void showDropDown(CharSequence s)
    {
        final String sAsString = s.toString();

        new AsyncTask(){

            @Override
            protected Object doInBackground(Object[] params)
            {
                //placesTask.execute(sAsString.toString());
                new PlacesTask().execute(sAsString.toString());
                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                destACTV.showDropDown();
            }
        }.execute();
    }

    private boolean isInputValid()
    {
        if(tripNameET.getText().toString().matches(AppConstants.EMPTY_STRING))
        {
            AppUtils.showSnackBarMsg("Please enter trip name", this, mainLayout);
            AppUtils.animateView(Techniques.Shake, 700, tripNameET);
            return false;
        }
        if(destACTV.getText().toString().matches(AppConstants.EMPTY_STRING))
        {
            AppUtils.showSnackBarMsg("Please enter a destination", this, mainLayout);
            AppUtils.animateView(Techniques.Shake, 700, destACTV);
            return false;
        }
        if(departureET.getText().toString().matches(AppConstants.EMPTY_STRING))
        {
            AppUtils.showSnackBarMsg("Please enter a departure date", this, mainLayout);
            AppUtils.animateView(Techniques.Shake, 700, departureET);
            return false;
        }
        if(returnET.getText().toString().matches(AppConstants.EMPTY_STRING))
        {
            AppUtils.showSnackBarMsg("Please enter a return date", this, mainLayout);
            AppUtils.animateView(Techniques.Shake, 700, returnET);
            return false;
        }
        if(goingSpinner.getSelectedItem().toString().matches(AppConstants.EMPTY_STRING))
        {
            AppUtils.showSnackBarMsg("Please enter who are you traveling with", this, mainLayout);
            AppUtils.animateView(Techniques.Shake, 700, goingSpinner);
            return false;
        }
        if(typeSpinner.getSelectedItem().toString().matches(AppConstants.EMPTY_STRING))
        {
            AppUtils.showSnackBarMsg("Please enter what is your trip type", this, mainLayout);
            AppUtils.animateView(Techniques.Shake, 700, typeSpinner);
            return false;
        }

        return true;
    }

    private void handleDatePickerClicked(View v)
    {
        String otherTypeDate = checkOtherTypeDate();
        String currDateInEditText = checkCurrDateInEditText();
        DatesPickerFragment datesFragment = new DatesPickerFragment();
        Bundle argsBundle = setArgsBundle(currDateInEditText, otherTypeDate);
        datesFragment.setArguments(argsBundle);
        datesFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private Bundle setArgsBundle(String currDateInEditText, String otherTypeDate) {
        Bundle bundle = new Bundle();

        bundle.putString("date_type_clicked", dateTypeClicked);
        bundle.putString("curr_date", currDateInEditText);
        bundle.putString("other_type_date", otherTypeDate);

        return bundle;
    }

    private String checkCurrDateInEditText()
    {
        String currDateInEditText = AppConstants.EMPTY_STRING;
        String currDateStr = getCurrDateStr();

        if(dateTypeClicked == "departureET" && !departureET.getText().toString().equals(AppConstants.EMPTY_STRING))
            currDateInEditText = departureET.getText().toString();
        else if (dateTypeClicked == "returnET" && !returnET.getText().toString().equals(AppConstants.EMPTY_STRING))
            currDateInEditText = returnET.getText().toString();

        return currDateInEditText;
    }

    private String getCurrDateStr() {
        Date now = Calendar.getInstance().getTime();
        String formattedDate = DateUtils.dateToString(now);

        return formattedDate;
    }

    private String checkOtherTypeDate()
    {
        String othertypeDate = AppConstants.EMPTY_STRING;
        String currDateStr = getCurrDateStr();

        if(dateTypeClicked == "departureET" && !returnET.getText().toString().equals(AppConstants.EMPTY_STRING))
            othertypeDate = returnET.getText().toString();
        if (dateTypeClicked == "returnET" && !departureET.getText().toString().equals(AppConstants.EMPTY_STRING))
            othertypeDate = departureET.getText().toString();

        return othertypeDate;
    }

    private void handleCreateTripClick(View v)
    {
        Geocoder geocoder;

        createTripButton.setClickable(false);
//        createTripButton.setVisibility(View.VISIBLE);
        final eTravelingWith travelingWith = eTravelingWith.fromStringToEnum(goingSpinner.getSelectedItem().toString());
        final eTripType tripType = eTripType.fromStringToEnum(typeSpinner.getSelectedItem().toString());
        final String destStr = destACTV.getText().toString();
        final String tripNameStr = tripNameET.getText().toString();

        //geocoder = new Geocoder(this, Locale.getDefault());

        new AsyncTask(){
            JSONObject result = null;
            List<Address> addresses;

            @Override
            protected Object doInBackground(Object[] params)
            {
                result = getLocationInfo(destStr);

                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                super.onPostExecute(o);
                addresses = getAddrByWeb(result);
                destLat = addresses.get(0).getLatitude();
                destLng = addresses.get(0).getLongitude();
                String username = SaveSharedPreference.getUserName(getApplicationContext());

                trip = new Trip(tripNameStr, destStr, tripDepartureDate, tripReturnDate, travelingWith, tripType, destLat, destLng, username);
                System.out.println("######## " + trip.getNumOfDays() + " ###########");
                // **************************
                sendTripToServer();
                // **************************

                // in the meantime until logic is added to the server:
                //startPlanTripActivity();

            }
        }.execute();
    }

    private void sendTripToServer()
    {
        JSONObject tripJSON;
        String url = AppConstants.SERVER_URL + AppConstants.ADD_NEW_TRIP_URL;
        //String url = "http://192.168.203.2:3000/addNewTrip";

        try
        {
            tripJSON = trip.createJSONInNewTripForm();
            //tripJSON.put("username", SaveSharedPreference.getUserName(getApplicationContext())); // adding username

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripJSON, new Response.Listener<JSONObject>()
            {
                        @Override
                        public void onResponse(JSONObject response) {
                            handleCreateTripResponse(response);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("error response on sendTripToServer()");
                        }
                    });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleCreateTripResponse(JSONObject response)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    startPlanTripActivity(response.getString("trip_id"));
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    Toast.makeText(this, "Create trip failed. Server error.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    System.out.println("error on handleCreateTripResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startPlanTripActivity(String tripID)//String tripID)
    {
        Bundle bundle = new Bundle();

        trip.setID(tripID);
        SaveSharedPreference.setCurrTripID(getApplicationContext(), tripID);
        bundle.putString("caller", "NewTripForm");
        bundle.putParcelable("tripObject", trip);

        Intent planTripIntent = new Intent(NewTripForm.this, PlanTripActivity.class);
        planTripIntent.putExtras(bundle);
        startActivity(planTripIntent);
    }

    private void setTypeSpinnerAdapter()
    {
        typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeOptions);
        typeSpinner.setAdapter(typeAdapter);
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

    private void setGoingSpinnerAdapter()
    {
        goingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, goingOptions);
        goingSpinner.setAdapter(goingAdapter);
    }

    private void initGoingOptionsArray()
    {
        String[] options = {eTravelingWith.ALONE.strValue(),
                            eTravelingWith.COUPLE.strValue(),
                            eTravelingWith.FAMILY.strValue(),
                            eTravelingWith.FRIENDS.strValue()};
        Collections.addAll(goingOptions, options);
    }

//    private void createDestinationAutoComplete()
//    {
//        destAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, destinations);
//        destACTV.setThreshold(1); //will start working from first character
//        destACTV.setAdapter(destAdapter); //setting the adapter data into the AutoCompleteTextView
//        destACTV.setTextColor(Color.BLACK);
//    }

//    private void initDestinationsArray()
//    {
//        String[] countryCodes = Locale.getISOCountries();
//
//        for (String countryCode : countryCodes)
//        {
//            Locale locale = new Locale("", countryCode);
//            destinations.add(locale.getDisplayCountry());
//            Collections.sort(destinations);
//        }
//    }

    private void getUIComponents()
    {
        destACTV = (AutoCompleteTextView)findViewById(R.id.destination_AC);
        goingSpinner = (Spinner) findViewById(R.id.goingSpinner);
        typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        departureET = (EditText)findViewById(R.id.departureDateET);
        returnET = (EditText)findViewById(R.id.returnDateET);
        createTripButton = (Button) findViewById(R.id.createTripButton);
        tripNameET = (EditText) findViewById(R.id.trip_name_ET);
        mainLayout = (RelativeLayout)findViewById(R.id.form_main_layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_trip, menu);
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


    public static JSONObject getLocationInfo(String address) {
        StringBuilder stringBuilder = new StringBuilder();

        try {

            address = address.replaceAll(" ","%20");

            HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            stringBuilder = new StringBuilder();


            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonObject;
    }

    private static List<Address> getAddrByWeb(JSONObject jsonObject)
    {
        List<Address> res = new ArrayList<Address>();
        try {
            JSONArray array = (JSONArray) jsonObject.get("results");
            for (int i = 0; i < array.length(); i++) {
                Double lng = new Double(0);
                Double lat = new Double(0);
                String name = "";
                try {
                    lng = array.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                    lat = array.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    name = array.getJSONObject(i).getString("formatted_address");
                    Address addr = new Address(Locale.getDefault());
                    addr.setLatitude(lat);
                    addr.setLongitude(lng);
                    addr.setAddressLine(0, name != null ? name : "");
                    res.add(addr);
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }

        return res;
    }

    @Override
    public void onFragmentInteraction(String event, Object...objs)
    {


        int year = ((Bundle)objs[0]).getInt("year");
        int month = ((Bundle)objs[0]).getInt("month");
        int day = ((Bundle)objs[0]).getInt("day");

        if(dateTypeClicked == "departureET") {
            DateUtils.setDatesEditTexts(year, month, day, departureET);
            tripDepartureDate = DateUtils.getDate(year, month, day);
        }
        else
        {
            DateUtils.setDatesEditTexts(year, month, day, returnET);
            tripReturnDate = DateUtils.getDate(year, month, day);
        }
    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyAD9nllP0DMAuh-HD0WXz-WwxcVim1W5nU";

            String input="";
            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }


            // place type to be searched
            //String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";
            String types = "types=(regions)";
            String components = "components=country";

            // Building the parameters to the web service
            String parameters = input+"&"+types+"&"+sensor+"&"+key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

            try{
                // Fetching the data from web service in background
                data = downloadUrl(url);
                System.out.println("!!!!!!!!" + data.toString());
            }catch(Exception e){
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        private String downloadUrl(String strUrl) throws IOException
        {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try{
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb  = new StringBuffer();

                String line = "";
                while( ( line = br.readLine())  != null){
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            }catch(Exception e){
                Log.d("Exception", e.toString());
            }finally{
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);


                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);
                System.out.println("******************" + places.toString());
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            destACTV.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}
