package com.trippin.androidtrippin.model;

public class AppConstants
{
    public static final String EMPTY_STRING = "";
    public static final String MY_TRIPS = "My Trips";
    public static final String LOGIN_USERNAME_ERROR = "username_error";
    public static final String LOGIN_PASSWORD_ERROR = "password_error";
    public static final String LOGIN_SUCCESS = "success";
    public static final String LOGIN_URL = "/login";
    public static final String GET_TRIP_PLACES = "/getTripPlaces";
    public static final String GET_USER_PLACE_PHOTOS = "/getPlaceUploadedImagesIDs";
    public static final String SAVE_PLACE_IMAGE = "/savePlaceImage";
    public static final String GET_USER_PLACE_IMAGE = "/getUserPlaceImage";
    public static final String ADD_NEW_TRIP_URL = "/addNewTrip";
    public static final String GET_USER = "/getUser";
    public static final String GET_USER_TRIPS = "/getUserTrips";
    public static final String ADD_PLACE_TO_TRIP = "/addTripPlace";
    public static final String SEND_PLACE_IMAGE = "/updatePlaceImage";
    public static final String GET_PLACE_IMAGE = "/getPlaceImage";
    public static final String SERVER_URL = "http://default-environment.bvprxj2p3x.us-east-1.elasticbeanstalk.com"; //"http://ec2-52-10-214-138.us-west-2.compute.amazonaws.com:3000";
    public static final String CHECK_IF_USER_EXISTS_URL = "/checkIfUserExists";
    public static final String SIGN_UP_URL = "/signUp";
    public final static String SIGN_UP_USERNAME = "com.example.eyalengel.trippin.user_email";
    public final static String SIGN_UP_USER_PASSWORD = "com.example.eyalengel.trippin.user_password";
    public final static String SIGN_UP_USER_FNAME = "com.example.eyalengel.trippin.user_fname";
    public final static String SIGN_UP_USER_LNAME = "com.example.eyalengel.trippin.user_lname";
    public static final String SIGN_UP_USER_COUNTRY = "com.example.eyalengel.trippin.user_country";
    public static final String SIGN_UP_USER_AGE = "com.example.eyalengel.trippin.user_age";
    public static final String SIGN_UP_USER_MOTO = "com.example.eyalengel.trippin.user_moto";

    public final static String SIGN_UP_USER_EXISTS = "userExists";
    public final static String RESPONSE_SUCCESS = "success";
    public final static String RESPONSE_FAILURE = "failure";
    public final static String ADD_TO_TRIP = "Add to Trip";
    public final static String REMOVE_FROM_TRIP = "Add to Trip";
    public final static float USE_OLD_ZOOM = -1.0f;
    public final static int FIRST_PHOTO_INDEX = 0;
    public final static int MY_TRIPS_HOME_NAV_ITEM = 0;

    public final static int MY_PROFILE_HOME_NAV_ITEM = 1;
    public final static int PLACE_PICKER_REQUEST = 1;
    public final static int SETTINGS_HOME_NAV_ITEM = 2;
    public final static int ABOUT_TRIPPIN_HOME_NAV_ITEM = 3;
    public final static int LOGOUT_HOME_NAV_ITEM = 4;
    public final static int HOME_PLAN_TRIP_NAV_ITEM = 0;
    public final static int MAP_PLAN_TRIP_NAV_ITEM = 1;
    public final static int SIMILAR_TRIPS_PLAN_TRIP_NAV_ITEM = 2;
    public final static int MY_PROFILE_PLAN_TRIP_NAV_ITEM = 3;
    public final static int SETTINGS_PLAN_TRIP_NAV_ITEM = 4;
    public final static int ABOUT_TRIPPIN_PLAN_TRIP_NAV_ITEM = 5;
    public final static int LOGOUT_PLAN_TRIP_NAV_ITEM = 6;
    public final static float ZOOM_SIX = 6.0f;
    public final static float ZOOM_CITY = 12.0f;
    public final static float ZOOM_COUNTRY = 6.0f;
    public final static float ZOOM_LARGE_COUNTRY = 3.0f;
    public final static int GOOGLE_PLACE_MAX_IMAGES = 7;
    public static final int MIN_AGE = 12;
    public static final int MAX_AGE = 101;
    public final static boolean RIGHT_ARROW_CLICKED = true;
    public static final int NOT_FOUND = -1;
    public static final int DELAY_START_ACTIVITY = 250;
    public static final int CAMERA_ROLL_REQUEST_CODE = 1;
    public static final int CAMERA_REQUEST_CODE = 2;
    public static final int NUM_OF_SIMILARITY_FILTERS = 3;
    public static int NumOfNavListItemsInHomeActivity = 4;
    public static final float MIN_RATING = 0.0f;
    public static final int IMAGE_MAX_SIZE = 1024;
    public static final String ACTV_HINT = "Search a Place";
    public static final String SIGNUP_TITLE = "Sign Up";
    public static final String UPDATE_PLACE_NOTE = "/updatePlaceNote";
    public static final String GET_PLACE_NOTE = "/getPlaceNote";
    public static final String GET_SIMILAR_TRIPS = "/getSimilarTrips";
    public static final String GET_TRIP = "/getTrip";
    public static final String DELETE_PLACE_FROM_TRIP = "/deleteTripPlace";
    public static final String DELETE_USER_PLACE_IMAGE = "/deleteUserPlaceImage";
    public static final String DELETE_TRIP = "/deleteTrip";
    public static final String UPDATE_TRIP_MAIN_PHOTO = "/updateTripMainPhoto";
    public static final String GET_TRIP_MAIN_PHOTO = "/getTripMainPhoto";
    public static final String GET_TRIP_PLACES_GOOGLE_IDS = "/getTripPlacesGoogleIDs";
    public static final String RESPONSE_EXISTS = "exists";
    public static final String GET_TRIP_PLACES_SERVER_IDS = "/getTripPlacesServerIDs";
    public static final String GET_TRIP_PLACE = "/getTripPlace";
    public static final String DEFAULT_MOTO = "Just trippin'";
    public static final String NO_PLACE_DESCRIPTION = "No Description Available";
    public static final String UPDATE_USER_INFO = "/updateUserInfo";
    public static final String UPDATE_TRIP_SETTINGS = "/updateTripSettings";
    public static final String UPDATE_TRIP_RATING = "/updateTripRating";
    public static final String RETRIEVE_PASSWORD = "/retrievePassword";
    public static final String NO_INTERNET_CONNECTION = "No internet connection!";
    //public static final String SEND_GOOGLE_USER_DETAILS = "/sendGoogleUserDetails";

}
