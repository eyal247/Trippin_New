package com.trippin.androidtrippin.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shaiyahli on 5/27/2015.
 */
public class TripPlace implements Parcelable, ClusterItem
{
    private String name;
    private String placeID;
    private Double latitude;
    private Double longitude;
    private String serverID;
    private Bitmap mainPhotoBitmap = null;
    private Note note;

    public TripPlace(String name, String placeID, Double latitude, Double longitude, String serverID, Bitmap mainPhotoBitmap, Note note)
    {
        this.name = name;
        this.placeID = placeID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.serverID = serverID;
        this.mainPhotoBitmap = mainPhotoBitmap;
        this.note = note;
    }

    public Note getNote()
    {
        return note;
    }

    public void setNote(Note note)
    {
        this.note = note;
    }

    public String getName()
    {
        return name;
    }
    
    public TripPlace(Parcel in){
        readFromParcel(in);
    }

    public TripPlace(){

    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Bitmap getMainPhotoBitmap()
    {
        return mainPhotoBitmap;
    }

    public void setMainPhotoBitmap(Bitmap mainPhotoBitmap)
    {
        this.mainPhotoBitmap = mainPhotoBitmap;
    }

    public String getPlaceID()
    {
        return placeID;
    }

    public void setPlaceID(String placeID)
    {
        this.placeID = placeID;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    public String getServerID()
    {
        return serverID;
    }

    public void setServerID(String serverID)
    {
        this.serverID = serverID;
    }

    public Double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(name);
        dest.writeString(placeID);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeParcelable(mainPhotoBitmap, flags);
        dest.writeString(serverID);
    }

    private void readFromParcel(Parcel in){
        name = in.readString();
        placeID = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
        mainPhotoBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        serverID = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public TripPlace createFromParcel(Parcel in) { return new TripPlace(in); }

        @Override
        public Object[] newArray(int size)
        {
            return new Object[size];
        }

        public TripPlace newTripPlace() {return new TripPlace();}
    };

    @Override
    public LatLng getPosition()
    {
        return new LatLng(latitude, longitude);
    }

    public static TripPlace parsePlaceFromJSON(JSONObject response)
    {
        TripPlace place = new TripPlace();
        JSONObject placeJSONObject;

        try
        {
            if (response.has("place"))
            {
                placeJSONObject = response.getJSONObject("place");
                String name = placeJSONObject.optString("name");
                String placeID = placeJSONObject.optString("placeID");
                Double latitude = Double.parseDouble(placeJSONObject.optString("latitude"));
                Double longitude = Double.parseDouble(placeJSONObject.optString("longitude"));
                String serverID = placeJSONObject.optString("_id");
                String noteID = placeJSONObject.optString("note");
                String mainPhoto = placeJSONObject.optString("mainPhoto");
                Note note = new Note("","","",noteID);
                place.setName(name);
                place.setPlaceID(placeID);
                place.setLatitude(latitude);
                place.setLongitude(longitude);
                place.setServerID(serverID);
                place.setMainPhotoBitmap(AppUtils.stringToBitMap(mainPhoto));
                place.setNote(note);
            }
        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
        }

        return place;
    }
}
