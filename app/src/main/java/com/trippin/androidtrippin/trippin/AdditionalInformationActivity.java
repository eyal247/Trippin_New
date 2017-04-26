package com.trippin.androidtrippin.trippin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.trippin.androidtrippin.trippin.MyActionBarActivity;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.trippin.SignUpProfilePictureActivity;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;

import java.util.ArrayList;
import java.util.Locale;

public class AdditionalInformationActivity extends MyActionBarActivity
{
    private Spinner countriesSpinner;
    private Spinner ageSpinner;
    private EditText motoET;
    private Button nextButton;
    private RelativeLayout additionalInformationRL;
    private ArrayList<String> countriesList = new ArrayList<>();
    private ArrayList<Integer> agesList = new ArrayList<>();
    private ArrayAdapter<String> countriesAdapter;
    private ArrayAdapter<Integer> agesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_information);

        getUIComponents();
        initCountriesOptionsArray();
        initAgesOptionsArray();
        setAgeSpinnerAdapter();
        setCountriesSpinnerAdapter();
        setUIListeneres();
    }

    private void setUIListeneres()
    {
        nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                handleNextClick();
            }
        });

        additionalInformationRL.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AppUtils.hideKeyboard(AdditionalInformationActivity.this, findViewById(android.R.id.content).getWindowToken());
            }
        });
    }

    private void handleNextClick()
    {
        String country = countriesSpinner.getSelectedItem().toString();
        String age = ageSpinner.getSelectedItem().toString();
        String moto = motoET.getText().toString();
        switchToSignUpProfilePictureActivity(country, age, moto);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        super.startCheckInternetConnection();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        super.stopCheckInternetConnection();
    }

    private void switchToSignUpProfilePictureActivity(String country, String age, String moto)
    {
        Intent intent = getIntent();
        intent.putExtra(AppConstants.SIGN_UP_USER_COUNTRY, country);
        intent.putExtra(AppConstants.SIGN_UP_USER_AGE, age);
        intent.putExtra(AppConstants.SIGN_UP_USER_MOTO, moto);
        intent.setClass(AdditionalInformationActivity.this, SignUpProfilePictureActivity.class);
        startActivity(intent);
    }

    private void initAgesOptionsArray()
    {
        for(int i = AppConstants.MIN_AGE ; i < AppConstants.MAX_AGE ; i++)
        {
            agesList.add(i-AppConstants.MIN_AGE, i);
        }

    }

    private void initCountriesOptionsArray()
    {
        countriesList = AppUtils.getCountriesList();
    }

    private void setAgeSpinnerAdapter()
    {
        agesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, agesList);
        ageSpinner.setAdapter(agesAdapter);
    }

    private void setCountriesSpinnerAdapter()
    {
        Locale myLocale = Locale.getDefault();
        String defaultCountry = myLocale.getDisplayCountry();

        countriesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countriesList);
        countriesSpinner.setAdapter(countriesAdapter);
        countriesSpinner.setSelection(countriesAdapter.getPosition(defaultCountry));
    }

    private void getUIComponents()
    {
        countriesSpinner = (Spinner)findViewById(R.id.countries_spinner);
        ageSpinner = (Spinner)findViewById(R.id.age_spinner);
        motoET = (EditText)findViewById(R.id.user_moto_ET);
        nextButton = (Button)findViewById(R.id.nextButtonInAdditionalInfo);
        additionalInformationRL = (RelativeLayout)findViewById(R.id.additional_information_rl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_additional_information, menu);
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


}
