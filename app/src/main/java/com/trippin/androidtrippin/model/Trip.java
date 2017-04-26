package com.trippin.androidtrippin.model;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by User on 20/05/2015.
 */
public class Trip implements Parcelable
{
    private String id;
    private String name;
    private String destination;
    private Date departureDate;
    private Date returnDate;
    private int numOfDays;
    private eTravelingWith travelingWith;
    private eTripType tripType;
    private double mainLat;
    private double mainLng;
    private String username;
    private String tripCoverPhotoStr;
    private List<TripPlace> tripPlaces;
    private String tripperName;
    private boolean share;
    private Float rating;

    public Trip() {}

    public Trip(Parcel in ) {
        readFromParcel( in );
    }

    public Trip(String name, String destination, Date departureDate, Date returnDate, eTravelingWith goingWith,
                eTripType tripType, double mainLat, double mainLng, String username)
    {
        this.name = name;
        this.destination = destination;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        calcNumOfDays();
        this.travelingWith = goingWith;
        this.tripType = tripType;
        this.mainLat = mainLat;
        this.mainLng = mainLng;
        this.username = username;
        this.tripPlaces = new ArrayList<>();
        this.share = true;
        this.rating = 0.0f;
    }

    private void calcNumOfDays()
    {
        this.numOfDays = (int)( (returnDate.getTime() - departureDate.getTime())
                / (1000 * 60 * 60 * 24) + 1);
    }

    //delete after handling Dates in Server
//    public Trip(String name, String destination, int numOfDays, eTravelingWith goingWith, eTripType type, double mainLat, double mainLng)
//    {
//        this.name = name;
//        this.destination = destination;
//        this.numOfDays = numOfDays;
//        this.travelingWith = goingWith;
//        this.tripType = type;
//        this.mainLat = mainLat;
//        this.mainLng = mainLng;
//        this.tripPlaces = new ArrayList<>();
//    }


    public String getUsername()
    {
        return username;
    }

    public void setID(String id)
    {
        this.id = id;
    }

    public Date getReturnDate()
    {
        return returnDate;
    }

    public void setReturnDate(Date returnDate)
    {
        this.returnDate = returnDate;
    }

    public Date getDepartureDate()
    {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate)
    {
        this.departureDate = departureDate;
    }

    public String getID()
    {
        return this.id;
    }

    public Float getRating()
    {
        return rating;
    }

    public void setRating(Float rating)
    {
        this.rating = rating;
    }

    public boolean addTripPlace(TripPlace tripPlace)
    {
        boolean exists = false;

        for (TripPlace t : tripPlaces)
        {
            if (t.getPlaceID().equals(tripPlace.getPlaceID()))
                exists = true;
        }

        if (!exists)
            tripPlaces.add(tripPlace);

        return (!exists);
    }

    public boolean isShare()
    {
        return share;
    }

    public void setShare(boolean share)
    {
        this.share = share;
    }

    public TripPlace getTripPlace(int index)
    {
        return tripPlaces.get(index);
    }

    public int getNumOfTripPlaces()
    {
        return tripPlaces.size();
    }

    public double getMainLng()
    {
        return mainLng;
    }

    public double getMainLat()
    {
        return mainLat;
    }

    public void removeTripPlace(TripPlace tripPlace)
    {
        tripPlaces.remove(tripPlace);
    }

    public JSONObject createJSONInNewTripForm() throws JSONException
    {
        JSONObject tripJSON = new JSONObject();

        String departureStr = DateUtils.dateToString(departureDate); // dd/mm/yyyy format
        String returnStr = DateUtils.dateToString(returnDate);

        tripJSON.put("name", this.name);
        tripJSON.put("departureDate", departureStr);
        tripJSON.put("returnDate", returnStr);
        tripJSON.put("goingWith", this.travelingWith.strValue());
        tripJSON.put("type", this.tripType.strValue());
        tripJSON.put("destination", this.destination);
        tripJSON.put("numOfDays", this.numOfDays);
        tripJSON.put("mainLat", String.valueOf(this.mainLat));
        tripJSON.put("mainLng", String.valueOf(this.mainLng));
        tripJSON.put("username", this.username);
        tripJSON.put("mainPhoto", AppConstants.EMPTY_STRING);
        tripJSON.put("share", true);
        tripJSON.put("rating", this.rating);


        return tripJSON;
    }

    public String getName()
    {
        return name;
    }

    public String getDestination()
    {
        return destination;
    }

    public int getNumOfDays()
    {
        return numOfDays;
    }

    public eTravelingWith getGoingWith()
    {
        return travelingWith;
    }

    public eTripType getType()
    {
        return tripType;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Trip createFromParcel(Parcel in) { return new Trip(in); }

        @Override
        public Object[] newArray(int size)
        {
            return new Object[size];
        }

        public Trip newTrip() {return new Trip();}
    };

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(destination);
        dest.writeSerializable(departureDate);
        dest.writeSerializable(returnDate);
        dest.writeInt(numOfDays);
        dest.writeString(travelingWith.strValue());
        dest.writeString(tripType.strValue());
        dest.writeDouble(mainLat);
        dest.writeDouble(mainLng);
        dest.writeString(username);
        dest.writeList(tripPlaces);
        dest.writeString(tripperName);
        dest.writeByte((byte) (share ? 1 : 0));
        dest.writeFloat(rating);
        dest.writeString(tripCoverPhotoStr);
    }

    private void readFromParcel(Parcel in)
    {
        id = in.readString();
        name = in.readString();
        destination = in.readString();
        departureDate = (Date)in.readSerializable();
        returnDate = (Date)in.readSerializable();
        numOfDays = in.readInt();
        travelingWith = eTravelingWith.fromStringToEnum(in.readString());
        tripType = eTripType.fromStringToEnum(in.readString());
        tripPlaces = new ArrayList<>();
        mainLat = in.readDouble();
        mainLng = in.readDouble();
        username = in.readString();
        in.readList(tripPlaces, Trip.class.getClassLoader());
        tripperName = in.readString();
        share = in.readByte() != 0;
        rating = in.readFloat();
        tripCoverPhotoStr = in.readString();
    }

    public void parsePlacesFromJson(JSONObject response)
    {
        tripPlaces = new ArrayList<>();
        JSONArray placesJSONArray;

        try
        {
            if (response.has("places"))
            {
                placesJSONArray = response.getJSONArray("places");

                for(int i = 0; i < placesJSONArray.length(); i++) // for each place in json array
                {
                    String name = placesJSONArray.getJSONObject(i).optString("name");
                    String placeID = placesJSONArray.getJSONObject(i).optString("placeID");
                    Double latitude = Double.parseDouble(placesJSONArray.getJSONObject(i).optString("latitude"));
                    Double longitude = Double.parseDouble(placesJSONArray.getJSONObject(i).optString("longitude"));
                    String serverID = placesJSONArray.getJSONObject(i).optString("_id");
                    String noteID = placesJSONArray.getJSONObject(i).optString("note");
                    Note note = new Note("","","",noteID);
                    TripPlace place = new TripPlace();
                    place.setName(name);
                    place.setPlaceID(placeID);
                    place.setLatitude(latitude);
                    place.setLongitude(longitude);
                    place.setServerID(serverID);
                    place.setNote(note);
                    tripPlaces.add(place);
                }
            }
        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
        }
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setTravelingWith(eTravelingWith travelingWith)
    {
        this.travelingWith = travelingWith;
    }

    public void setTripType(eTripType tripType)
    {
        this.tripType = tripType;
    }

    public void deleteTripPlace(int index)
    {
        tripPlaces.remove(index);
    }

    public void setTripperName(String name)
    {
        this.tripperName = name;
    }

    public String getTripperName()
    {
        return tripperName;
    }

    public String getTripCoverPhotoStr()
    {
        return tripCoverPhotoStr;
    }

    public Bitmap getTripCoverPhotoBitmap()
    {
        return AppUtils.stringToBitMap(tripCoverPhotoStr);
    }

    public void setTripCoverPhotoStr(String tripMainImage)
    {
        this.tripCoverPhotoStr = tripMainImage;
    }

    public Bundle createTripDetailsBundle()
    {
        Bundle bundle = new Bundle();

        bundle.putString("trip_id", this.id);
        bundle.putString("trip_name", this.name);
        bundle.putString("trip_dest", this.destination);
        bundle.putString("trip_departure", DateUtils.dateToString(this.departureDate));
        bundle.putString("trip_return", DateUtils.dateToString(this.returnDate));
        bundle.putString("trip_type", this.tripType.strValue());
        bundle.putString("trip_going_with", this.travelingWith.strValue());
        bundle.putBoolean("share", this.share);
        bundle.putFloat("rating", rating);
        bundle.putString("trip_photo_str", this.tripCoverPhotoStr);

        return bundle;
    }
}
