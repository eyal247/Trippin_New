package com.trippin.androidtrippin.model;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by shaiyahli on 5/17/2015.
 */
public class GooglePlacesUtils
{
    public static String createPlaceImageUrl(PhotoInfo photoInfo)
    {
        String url = null;

//        System.out.println("PHOTO REFERENCE - " + photoInfo.getPhotoRef());
        if(photoInfo != null)
        {
            if (photoInfo.getPhotoRef() == null || photoInfo.getPhotoRef() == "")
                return url;

            if(photoInfo.getMaxHeight() != null && photoInfo.getMaxHeight() != ""){
                url = "https://maps.googleapis.com/maps/api/place/photo?maxheight=" +
                        photoInfo.getMaxHeight() + "&photoreference=" +
                        photoInfo.getPhotoRef() + "&key="+ GoogleServerKey.GOOGLE_SERVER_KEY;
            }
            else if(photoInfo.getMaxWidth() != null && photoInfo.getMaxWidth() != "")
            {
                url = "https://maps.googleapis.com/maps/api/place/photo?maxWidtht=" +
                        photoInfo.getMaxWidth() + "&photoreference=" +
                        photoInfo.getPhotoRef() + "&key="+ GoogleServerKey.GOOGLE_SERVER_KEY;
            }
            else
            {

//            url = "https://maps.googleapis.com/maps/api/place/photo?maxheight=100&photoreference=" +
//                    photoInfo.getPhotoRef() + "&key="+ GoogleServerKey.GOOGLE_SERVER_KEY;
            }
        }

        return url;
    }

    public static Bitmap makePhotoCall(String photoReference, String maxHeight)
    {
        InputStream is;
        Bitmap photo = null;
        String placesUrl = "https://maps.googleapis.com/maps/api/place/photo?maxheight=" + maxHeight + "&photoreference=" + photoReference + "&key="+ GoogleServerKey.GOOGLE_SERVER_KEY;

        StringBuffer buffer_string = new StringBuffer(placesUrl);
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(buffer_string.toString());

        try {
            // get the response of the httpclient execution of the url
            HttpResponse response = httpclient.execute(httpget);
            is = response.getEntity().getContent();
            photo = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return photo;
    }

    public static PhotoInfo getMainPhotoInfoFromDetailsJson(final String detailsJson)
    {
        PhotoInfo photoInfo = new PhotoInfo();
        JSONArray photosJSONArray;

        try {
            JSONObject jsonObject = new JSONObject(detailsJson);
            JSONObject resultsJsonObject = new JSONObject();

            if (jsonObject.has("result")) {
                resultsJsonObject = jsonObject.getJSONObject("result");
                if (resultsJsonObject.has("photos")){
                    photosJSONArray = resultsJsonObject.getJSONArray("photos");
                    photoInfo.setMembers(photosJSONArray.getJSONObject(0).getString("photo_reference"), photosJSONArray.getJSONObject(0).getString("height"), photosJSONArray.getJSONObject(0).getString("width"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return photoInfo;
    }

    public static ArrayList parseGooglePlacesNearbyJson(final String response)
    {
        ArrayList placesList = new ArrayList();
        JSONArray photosJSONArray;
        JSONArray typesJSONArray;

        try
        {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("results"))
            {
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jsonArray.length(); i++)
                {
                    GooglePlace place = new GooglePlace();
                    if (jsonArray.getJSONObject(i).has("name")) {
                        place.setName(jsonArray.getJSONObject(i).optString("name"));

                        if (jsonArray.getJSONObject(i).has("place_id")){
                            place.setPlaceID(jsonArray.getJSONObject(i).optString("place_id"));
                        }

                        if(jsonArray.getJSONObject(i).has("photos")) {
                            photosJSONArray = jsonArray.getJSONObject(i).getJSONArray("photos");
                            place.setMainPhotoInfo(photosJSONArray.getJSONObject(0).getString("photo_reference"), photosJSONArray.getJSONObject(0).getString("height"), photosJSONArray.getJSONObject(0).getString("width"));
                        }
                        if(jsonArray.getJSONObject(i).has("types")) {
                            typesJSONArray = jsonArray.getJSONObject(i).getJSONArray("types");
                            place.setMainType(typesJSONArray.getString(0));
                        }
                        if (jsonArray.getJSONObject(i).has("vicinity")){
                            place.setVicinity(jsonArray.getJSONObject(i).optString("vicinity"));
                        }
                    }

                    placesList.add(place);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }
        return placesList;
    }

    public static String makeCall(String url)
    {
        // string buffers the url
        StringBuffer buffer_string = new StringBuffer(url);
        String replyString = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(buffer_string.toString());

        try
        {
            // get the response of the httpclient execution of the url
            HttpResponse response = httpclient.execute(httpget);
            InputStream is = response.getEntity().getContent();

            // buffer input stream the result
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(20);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            // the result as a string is ready for parsing
            replyString = new String(baf.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(replyString);

        // trim the whitespaces
        return replyString.trim();
    }

    public static String parseGooglePlaceDescriptionJson(String descriptionJson){
        String description = "";

        try {
            JSONObject jsonObject = new JSONObject(descriptionJson);

            if (jsonObject.has("result")) {
                description = jsonObject.optString("result");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return description;
    }

    public static GooglePlace parseGooglePlacesDetailsJson(String detailsJson)
    {
        JSONArray photosJSONArray;
        GooglePlace placeResult = null;

        try {
            JSONObject jsonObject = new JSONObject(detailsJson);
            JSONObject resultsJsonObject = new JSONObject();

            if (jsonObject.has("result")) {

                placeResult = new GooglePlace();
                resultsJsonObject = jsonObject.getJSONObject("result");

                if (resultsJsonObject.has("name")){
                    placeResult.setName(resultsJsonObject.optString("name"));
                }

                if(resultsJsonObject.has("geometry")){
                    JSONObject jsonLocation = resultsJsonObject.getJSONObject("geometry").getJSONObject("location");
                    String latStr = jsonLocation.getString("lat");
                    String lngStr = jsonLocation.getString("lng");
                    Double latitude = Double.parseDouble(latStr);
                    Double longitude = Double.parseDouble(lngStr);
                    placeResult.setLatitude(latitude);
                    placeResult.setLongitude(longitude);
                }

                if(resultsJsonObject.has("rating")){
                    placeResult.setRating(resultsJsonObject.optString("rating"));
                }

                if(resultsJsonObject.has("place_id")){
                    placeResult.setPlaceID((resultsJsonObject.optString("place_id")));
                }

                if(resultsJsonObject.has("photos")){
                    photosJSONArray = resultsJsonObject.getJSONArray("photos");

                    for(int i = 0; i < photosJSONArray.length(); i++)
                    {
                        PhotoInfo currPhotoInfo = new PhotoInfo();
                        currPhotoInfo.setPhotoRef(photosJSONArray.getJSONObject(i).optString("photo_reference"));
                        currPhotoInfo.setMaxHeight(photosJSONArray.getJSONObject(i).optString("height"));
                        currPhotoInfo.setMaxHeight(photosJSONArray.getJSONObject(i).optString("width"));
                        placeResult.addPlacePhotoInfoToList(currPhotoInfo);
                    }
                }

                if(resultsJsonObject.has("formatted_address")){
                    placeResult.setAddress(resultsJsonObject.optString("formatted_address"));
                }

                if(resultsJsonObject.has("international_phone_number")){
                    placeResult.setInternationalPhoneNum(resultsJsonObject.optString("international_phone_number"));
                }

                if(resultsJsonObject.has("price_level")){
                    placeResult.setPriceLevel(resultsJsonObject.optString("price_level"));
                }

                if(resultsJsonObject.has("website")){
                    placeResult.setWebsite(resultsJsonObject.optString("website"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }

        return placeResult;
    }

    public static String getPlaceIdByLatLng(String results)
    {
        String place_id = null;
        try {
            JSONObject jsonObject = new JSONObject(results);
            JSONObject resultsJsonObject = new JSONObject();

            if (jsonObject.has("results")) {
                resultsJsonObject = jsonObject.getJSONObject("results");

                if(resultsJsonObject.has("place_id"))
                    place_id = resultsJsonObject.optString("place_id");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return place_id;
    }
}
