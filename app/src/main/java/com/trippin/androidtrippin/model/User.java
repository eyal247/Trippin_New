package com.trippin.androidtrippin.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class User {
    private final static String GOOGLE_USER_PASSWORD = "signedUpWithGoogle";
    private String username;
    private String password;
    private String fname;
    private String lname;
    private String age;
    private String country;
    private String encodedProfilePic = null;
    private int numOfTrips;
    private String moto;
    private String dateJoinedStr;
    private boolean signedUpWithGoogle;
    private boolean signedUpWithFacebook;
    private boolean isGoogleProfilePic;
    private boolean isFacebookProfilePic;
    private boolean userHasInfo;

    public void loadDetailsFromIntent(Intent intent) {
        username = intent.getStringExtra(AppConstants.SIGN_UP_USERNAME);
        password = intent.getStringExtra(AppConstants.SIGN_UP_USER_PASSWORD);
        fname = intent.getStringExtra(AppConstants.SIGN_UP_USER_FNAME);
        lname = intent.getStringExtra(AppConstants.SIGN_UP_USER_LNAME);
        country = intent.getStringExtra(AppConstants.SIGN_UP_USER_COUNTRY);
        age = intent.getStringExtra(AppConstants.SIGN_UP_USER_AGE);
        moto = intent.getStringExtra(AppConstants.SIGN_UP_USER_MOTO);
        if (moto == AppConstants.EMPTY_STRING)
            moto = AppConstants.DEFAULT_MOTO;
        signedUpWithGoogle = false;
        signedUpWithFacebook = false;
        isGoogleProfilePic = false;
        isFacebookProfilePic = false;

        userHasInfo = false;
    }

    public boolean loadDetailsFromGooglePerson(Person currentPerson, String email) {
        boolean userUpdatedSuccessfully = false;

        if (currentPerson != null) {
            username = email;
            password = GOOGLE_USER_PASSWORD;
            fname = currentPerson.getName().getGivenName();
            lname = currentPerson.getName().getFamilyName();
            getGoogleUserCountry(currentPerson);
            age = calcGoogleUserAge(currentPerson.getBirthday());
            moto = AppConstants.EMPTY_STRING;
            encodedProfilePic = currentPerson.getImage().getUrl();
            signedUpWithGoogle = true;
            isGoogleProfilePic = true;
            signedUpWithFacebook = false;
            isFacebookProfilePic = false;
            userUpdatedSuccessfully = true;
        }

        return userUpdatedSuccessfully;

    }

    public boolean loadDetailsFromFacebookProfile(Context ctx, Profile currentProfile, String email, String location) {
        boolean userUpdatedSuccessfully = false;

        if (currentProfile != null) {
            username = email;
            password = currentProfile.getId();
            fname = currentProfile.getFirstName();
            lname = currentProfile.getLastName();
            getFacebookUserCountry(location);
            age = "18";
            moto = AppConstants.EMPTY_STRING;
            encodedProfilePic = getFacebookProfilePicture(currentProfile.getId());
            signedUpWithFacebook = true;
            isFacebookProfilePic = true;
            isGoogleProfilePic = false;
            userUpdatedSuccessfully = true;
        }

        return userUpdatedSuccessfully;

    }

    private String getFacebookProfilePicture(String userID){
        String imageURL = "https://graph.facebook.com/" + userID + "/picture?type=large";
        return imageURL;
    }

    private void getFacebookUserCountry(String location) {
        country = location;
    }

    private void getGoogleUserCountry(Person currentPerson) {
        if (currentPerson.hasPlacesLived()) {
            country = currentPerson.getPlacesLived().get(0).getValue();
            setCountryFullNameIfGoogleUser();
        } else {
            country = Locale.getDefault().getCountry();
        }
    }

    private String calcGoogleUserAge(String birthdayStr) {
        String ageStr = "";
        Integer ageInt;

        if (birthdayStr != null && birthdayStr != AppConstants.EMPTY_STRING) {
            Calendar myCal = new GregorianCalendar();
            myCal.setTime(DateUtils.stringToDate(birthdayStr));
            Calendar now = new GregorianCalendar();
            ageInt = now.get(Calendar.YEAR) - myCal.get(Calendar.YEAR);
            if ((myCal.get(Calendar.MONTH) > now.get(Calendar.MONTH))
                    || (myCal.get(Calendar.MONTH) == now.get(Calendar.MONTH) && myCal.get(Calendar.DAY_OF_MONTH) > now
                    .get(Calendar.DAY_OF_MONTH))) {
                ageInt--;
            }
            ageStr = ageInt.toString();
        }

        return ageStr;
    }

    public boolean isGoogleProfilePic() {
        return isGoogleProfilePic;
    }

    public void setIsGoogleProfilePic(boolean isGoogleProfilePic) {
        this.isGoogleProfilePic = isGoogleProfilePic;
    }

    public String getAge() {
        return age;
    }

    public Integer getAgeNumber() {
        if (this.age.equals(AppConstants.EMPTY_STRING)) //if no age because it's Google User
            return AppConstants.MIN_AGE;
        else if (Integer.valueOf(this.age) < AppConstants.MIN_AGE) //if Google User has age<12 (we don't allow)
            return AppConstants.MIN_AGE;
        else
            return Integer.valueOf(this.age);  // if valid age between 12-100
    }

    public String getCountry() {
        return country;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getEncodedProfilePic() {
        return encodedProfilePic;
    }

    public void setEncodedProfilePic(String newVal) {
        encodedProfilePic = newVal;
    }


    //TODO: add a field in the server for a user, named isFacebookProfilePic, and send it to the server when creating a new Facebook user (and false for isGoogleProfilePic)
    public void parseDetailsFromJSON(JSONObject response) {
        // TODO: in time we should add user details to parse here.
        JSONObject userJSONObject = null;
        if (response.has("user")) {
            userHasInfo = true;
            try {
                userJSONObject = response.getJSONObject("user");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (userJSONObject.has("username")) {
                username = userJSONObject.optString("username");
            }

            if (userJSONObject.has("signedUpWithGoogle")) {
                signedUpWithGoogle = userJSONObject.optBoolean("signedUpWithGoogle");
            }
            if (userJSONObject.has("encodedProfilePic")) {
                encodedProfilePic = userJSONObject.optString("encodedProfilePic");
            }
            if (userJSONObject.has("isGoogleProfilePic")) {
                isGoogleProfilePic = userJSONObject.optBoolean("isGoogleProfilePic");
            }
            if (userJSONObject.has("isFacebookProfilePic")) {
                isFacebookProfilePic = userJSONObject.optBoolean("isFacebookProfilePic");
            }

            setUserFirstAndLastName(userJSONObject);
            setUserAge(userJSONObject);
            setUserDateJoined(userJSONObject);
            setUserMoto(userJSONObject);
            setUserNumOfTrips(userJSONObject);
            setUserCountry(userJSONObject);
        } else {
            userHasInfo = false;
        }
    }

    private void setUserAge(JSONObject userJSONObject) {
        if (userJSONObject.has("age")) {
            age = userJSONObject.optString("age");
            if (age == null)
                age = AppConstants.EMPTY_STRING;
        } else
            age = AppConstants.EMPTY_STRING;
    }

    private void setUserFirstAndLastName(JSONObject userJSONObject) {
        if (userJSONObject.has("fname")) {
            fname = userJSONObject.optString("fname");
            if (fname == null)
                fname = AppConstants.EMPTY_STRING;
        }
        if (userJSONObject.has("lname")) {
            lname = userJSONObject.optString("lname");
            if (lname == null)
                lname = AppConstants.EMPTY_STRING;
        }
    }

    private void setUserCountry(JSONObject userJSONObject) {
        if (userJSONObject.has("country")) {
            country = userJSONObject.optString("country");
            if (country == null)
                country = AppConstants.EMPTY_STRING;
            else
                setCountryFullNameIfGoogleUser();
        } else {
            country = AppConstants.EMPTY_STRING;
        }
    }

    private void setCountryFullNameIfGoogleUser() {
        String[] locales = Locale.getISOCountries();
        for (String countryCode : locales) {
            Locale obj = new Locale("", countryCode);
            if (obj.getCountry().equals(country)) {
                country = obj.getDisplayCountry();
                break;
            }
        }
    }

    private void setUserDateJoined(JSONObject userJSONObject) {
        if (userJSONObject.has("dateJoined")) {
            dateJoinedStr = userJSONObject.optString("dateJoined");
        } else
            dateJoinedStr = AppConstants.EMPTY_STRING;
    }

    private void setUserMoto(JSONObject userJSONObject) {
        if (userJSONObject.has("moto")) {
            moto = userJSONObject.optString("moto");
            if (moto == null)
                moto = AppConstants.DEFAULT_MOTO;
        } else
            moto = AppConstants.DEFAULT_MOTO;
    }

    private void setUserNumOfTrips(JSONObject userJSONObject) {
        if (userJSONObject.has("trips")) {
            try {
                JSONArray trips = userJSONObject.getJSONArray("trips");
                if (trips == null) {
                    numOfTrips = 0;
                } else {
                    numOfTrips = trips.length();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSignedUpWithGoogle() {
        return signedUpWithGoogle;
    }

    public String getDateJoinedStr() {
        return dateJoinedStr;
    }

    public void setDateJoinedStr(String dateJoinedStr) {
        this.dateJoinedStr = dateJoinedStr;
    }

    public String getMoto() {
        return moto;
    }

    public int getNumOfTrips() {
        return numOfTrips;
    }

    public void setNumOfTrips(int numOfTrips) {
        this.numOfTrips = numOfTrips;
    }

    public void addJoinedDate() {
        Calendar cal = Calendar.getInstance();
        dateJoinedStr = DateUtils.dateToString(cal.getTime());
    }

    public void setNoInfo (boolean userHasInfo) {
        this.userHasInfo = userHasInfo;
    }

    public boolean getUserHasInfo()
    {
        return this.userHasInfo;
    }

    public boolean isFBProfilePic() {
        return isFacebookProfilePic;
    }

    public void setIsFacebookProfilePic(boolean isFacebookProfilePic) {
        this.isFacebookProfilePic = isFacebookProfilePic;
    }
}
