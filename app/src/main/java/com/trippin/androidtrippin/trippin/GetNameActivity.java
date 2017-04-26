package com.trippin.androidtrippin.trippin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.trippin.androidtrippin.trippin.MyActionBarActivity;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;


public class GetNameActivity extends MyActionBarActivity
{
    private EditText firstNameTB;
    private EditText lastNameTB;
    private Button nextButton;
    private RelativeLayout getNameRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_name);

        getUIComponents();
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
        nextButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if(isInputValid())
                            handleNextClick(v);
                    }
                }
        );

        getNameRelativeLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AppUtils.hideKeyboard(GetNameActivity.this, findViewById(android.R.id.content).getWindowToken());
            }
        });
    }

    private boolean isInputValid()
    {
        if(firstNameTB.getText().toString().matches(AppConstants.EMPTY_STRING))
        {
            Toast.makeText(this, "Please enter your first name", Toast.LENGTH_LONG).show();
            return false;
        }
        if (lastNameTB.getText().toString().matches(AppConstants.EMPTY_STRING))
        {
            Toast.makeText(this,"Please enter your last name", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void getUIComponents()
    {
        nextButton = (Button) findViewById(R.id.getNameNextButton);
        firstNameTB = (EditText) findViewById(R.id.firstNameTB);
        lastNameTB = (EditText) findViewById(R.id.lastNameTB);
        getNameRelativeLayout = (RelativeLayout)findViewById(R.id.getNameRelativeLayout);
    }

    private void handleNextClick(View v)
    {
        String fname = firstNameTB.getText().toString();
        String lname = lastNameTB.getText().toString();
        boolean badInput = false;

        if (fname.isEmpty())
        {
            Toast.makeText(this,"Please enter your first name", Toast.LENGTH_LONG).show();
            badInput = true;
        }
        if (lname.isEmpty())
        {
            Toast.makeText(this,"Please enter your last name", Toast.LENGTH_LONG).show();
            badInput = true;
        }

        if (!badInput)
            switchToAdditionalInfoActivity(fname,lname);
    }

    private void switchToAdditionalInfoActivity(String fname, String lname)
    {
        Intent intent = getIntent();
        intent.putExtra(AppConstants.SIGN_UP_USER_FNAME, fname);
        intent.putExtra(AppConstants.SIGN_UP_USER_LNAME, lname);
        intent.setClass(GetNameActivity.this, AdditionalInformationActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_get_name, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
