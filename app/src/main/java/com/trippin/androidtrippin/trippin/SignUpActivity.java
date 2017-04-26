package com.trippin.androidtrippin.trippin;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.SignUpUtils;

import org.json.JSONObject;


public class SignUpActivity extends MyActionBarActivity
{
    private EditText usernameTB;
    private EditText retypeUsernameTB;
    private EditText passwordTB;
    private EditText retypePasswordTB;
    private Button nextButton;
    private RelativeLayout signUpRelativeLayout;
    private JSONObject newUserJSON = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
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
                        handleNextClick(v);
                    }
                }
        );

        signUpRelativeLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AppUtils.hideKeyboard(SignUpActivity.this, findViewById(android.R.id.content).getWindowToken());
            }
        });
    }

    private void getUIComponents()
    {
        nextButton = (Button) findViewById(R.id.nextButton);
        usernameTB = (EditText) findViewById(R.id.usernameTB);
        retypeUsernameTB = (EditText) findViewById(R.id.user_moto_ET);
        passwordTB = (EditText) findViewById(R.id.passwordTB);
        retypePasswordTB = (EditText) findViewById(R.id.retypePasswordTB);
        signUpRelativeLayout = (RelativeLayout)findViewById(R.id.signUpRelativeLayout);
    }

    public void handleNextClick(View v)
    {
        boolean isEmailValid;
        boolean badInput = false;
        boolean signUpWithGoogle = false;
        String username = usernameTB.getText().toString();
        String retypeUsername = retypeUsernameTB.getText().toString();
        String password = passwordTB.getText().toString();
        String retypePassword = retypePasswordTB.getText().toString();

        isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches();
        if(!isEmailValid)  {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show();
            badInput = true;
        }
        else if(username.equals(AppConstants.EMPTY_STRING)){
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_LONG).show();
            badInput = true;
        }
        else if (!username.equals(retypeUsername)) {
            Toast.makeText(this,"Email doesn't match", Toast.LENGTH_LONG).show();
            badInput = true;
        }
        else if(password.equals("")) {
            Toast.makeText(this,"Please enter your password", Toast.LENGTH_LONG).show();
            badInput = true;
        }
        if (!password.equals(retypePassword)) {
            Toast.makeText(this,"Password doesn't match", Toast.LENGTH_LONG).show();
            badInput = true;
        }
        if (!badInput){
            SignUpUtils.checkUserWithServer(SignUpActivity.this, username, password, signUpWithGoogle, null);
        }
    }

//    private void checkUserWithServer(final String username, final String password)
//    {
//        String url = AppConstants.SERVER_URL + AppConstants.SIGN_UP_URL;
//        //String url = "http://10.0.0.5:3000/signup";
//
//        try {
//            newUserJSON.put("username", username);
//            newUserJSON.put("password", password);
//            JsonObjectRequest jsObjRequest = new JsonObjectRequest
//                    (Request.Method.POST, url, newUserJSON, new Response.Listener<JSONObject>() {
//
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            handleCheckUserResponse(response);
//                        }
//                    }, new Response.ErrorListener() {
//
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            System.out.println("error response on checkUserWithServer()");
//                        }
//                    });
//            MainActivity.addRequestToQueue(jsObjRequest);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void handleCheckUserResponse(JSONObject response)
//    {
//        try
//        {
//            switch(response.getString("result"))
//            {
//                case AppConstants.RESPONSE_SUCCESS:
//                    switchToGetNameActivity(newUserJSON.getString("username"), newUserJSON.getString("password"));
//                    break;
//                case AppConstants.SIGN_UP_USER_EXISTS:
//                    handleUserExists();
//                    break;
//                default:
//                    System.out.println("error handleCheckUserResponse()");
//                    break;
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void handleUserExists()
//    {
//        Toast.makeText(this, "User already exists", Toast.LENGTH_LONG).show();
//    }

//    private void switchToGetNameActivity(String username, String password)
//    {
//        Intent intent = getIntent();
//        intent.putExtra(AppConstants.SIGN_UP_USERNAME, username);
//        intent.putExtra(AppConstants.SIGN_UP_USER_PASSWORD, password);
//        intent.setClass(SignUpActivity.this, GetNameActivity.class);
//        startActivity(intent);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
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
