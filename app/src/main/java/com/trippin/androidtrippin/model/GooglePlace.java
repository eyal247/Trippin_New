package com.trippin.androidtrippin.model;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class GooglePlace
{
    private String name;
    private String rating;
    private String weekdayText;
    private String mainType;
    private PhotoInfo mainPhotoInfo;
    private ArrayList<PhotoInfo> placePhotosInfo;
    private ArrayList<Bitmap> photosList;
    private String placeID;
    private String address;
    private String internationalPhoneNum;
    private String priceLevel;
    private String website;
    private Double latitude;
    private Double longitude;
    private String vicinity;
    private String description;

    public GooglePlace()
    {
        this.name = "";
        this.rating = "";
        this.weekdayText = "";
        this.placeID = "";
        this.mainType = "";
        this.mainPhotoInfo = new PhotoInfo();
        this.placePhotosInfo = new ArrayList<>();
        this.photosList = new ArrayList<>();
        this.address = "";
        this.internationalPhoneNum = "";
        this.priceLevel = "";
        this.website = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public Bitmap getPhoto(int i)
    {
        Bitmap photo = null;

        if (i < photosList.size())
            photo = photosList.get(i);

        return photo;
    }

    public void addPhoto(Bitmap photoBitmap){
        photosList.add(photoBitmap);
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    public Double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setPlaceID(String placeID)
    {
        this.placeID = placeID;
    }

    public void setMainPhotoInfo(String photoReference, String maxHeight, String maxWidth)
    {
        this.mainPhotoInfo.setMembers(photoReference, maxHeight, maxWidth);
    }

    public PhotoInfo getMainPhotoInfo()
    {
        return mainPhotoInfo;
    }

    public void addPlacePhotoInfoToList(PhotoInfo photoInfo){
        placePhotosInfo.add(photoInfo);
    }

    public PhotoInfo getPhotoInfoFromList(int position)
    {
        if (position >= 0 && position  < placePhotosInfo.size())
            return placePhotosInfo.get(position);
        else
            return null;
    }


    public String getName()
    {
        return name;
    }

    public String getPlaceID()
    {
        return placeID;
    }

    public void setRating(String rating)
    {
        this.rating = rating;
    }

    public String getRating()
    {
        return rating;
    }

    public String getWeekdayText()
    {
        return weekdayText;
    }

    public ArrayList<PhotoInfo> getPlacePhotosInfo()
    {
        return placePhotosInfo;
    }

    public String getAddress()
    {
        return address;
    }

    public String getInternationalPhoneNum()
    {
        return internationalPhoneNum;
    }

    public String getPriceLevel()
    {
        return priceLevel;
    }

    public void setWeekdayText(String weekdayText)
    {
        this.weekdayText = weekdayText;
    }

    public void setPlacePhotosInfo(ArrayList<PhotoInfo> placePhotosInfo)
    {
        this.placePhotosInfo = placePhotosInfo;
    }

    public void setMainPhotoInfo(PhotoInfo mainPhotoInfo)
    {
        this.mainPhotoInfo = mainPhotoInfo;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public void setInternationalPhoneNum(String internationalPhoneNum)
    {
        this.internationalPhoneNum = internationalPhoneNum;
    }

    public void setPriceLevel(String priceLevel)
    {
        this.priceLevel = priceLevel;
    }

    public void setWebsite(String website)
    {
        this.website = website;
    }

    public String getWebsite()
    {
        return website;
    }

    public void setMainType(String mainType)
    {
        this.mainType = fixTypeStringFormat(mainType);
    }

    private String fixTypeStringFormat(String mainType)
    {
        mainType = mainType.replaceAll("_", " ");
        mainType = mainType.substring(0, 1).toUpperCase() + mainType.substring(1);

        return mainType;
    }

    public String getMainType()
    {
        return mainType;
    }

    public String getVicinity()
    {
        return vicinity;
    }

    public void setVicinity(String vicinity)
    {
        this.vicinity = vicinity;
    }

    //    public void loadAllImages()
//    {
////        for (int i = 0; i < placePhotosInfo.size() && i < AppConstants.GOOGLE_PLACE_MAX_IMAGES; i++)
////        {
////           new GetPhotoAsync(i).execute();
////        }
//
//        new AsyncTask()
//        {
//            Bitmap photoBitmap = null;
//
//            @Override
//            protected Object doInBackground(Object[] params)
//            {
//                for (int i = 0; i < placePhotosInfo.size() && i < AppConstants.GOOGLE_PLACE_MAX_IMAGES; i++)
//                {
//                    PhotoInfo currPhotoInfo = placePhotosInfo.get(i);
//                    photoBitmap = GooglePlacesUtils.makePhotoCall(currPhotoInfo.getPhotoRef(), currPhotoInfo.getMaxHeight());
//                    if (photoBitmap != null)
//                        photosList.add(photoBitmap);
//                }
//
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Object o)
//            {
//                super.onPostExecute(o);
//
//            }
//        }.execute();
//    }

//    private class GetPhotoAsync extends AsyncTask{
//        private int index;
//        Bitmap photoBitmap;
//
//
//        public GetPhotoAsync(int index)
//        {
//            super();
//            this.index = index;
//            this.photoBitmap = null;
//        }
//
//        @Override
//        protected Object doInBackground(Object[] params)
//        {
//            PhotoInfo currPhotoInfo = placePhotosInfo.get(index);
//            photoBitmap = GooglePlacesUtils.makePhotoCall(currPhotoInfo.getPhotoRef(), currPhotoInfo.getMaxHeight());
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Object o)
//        {
//            super.onPostExecute(o);
//            if (photoBitmap != null)
//                photosList.add(photoBitmap);
//        }
//    }


}
