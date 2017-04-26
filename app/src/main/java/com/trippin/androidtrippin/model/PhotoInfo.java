package com.trippin.androidtrippin.model;

/**
 * Created by shaiyahli on 5/17/2015.
 */
public class PhotoInfo{

    private String photoRef;
    private String maxWidth;
    private String maxHeight;

        public PhotoInfo(){
            this.photoRef = "";
            this.maxHeight = "";
            this.maxWidth = "";
        }

    public void setMembers(String photoRef, String maxHeight, String maxWidth)
    {
        this.photoRef = photoRef;
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
    }

    public void setPhotoRef(String photoRef)
    {
        this.photoRef = photoRef;
    }

    public void setMaxWidth(String maxWidth)
    {
        this.maxWidth = maxWidth;
    }

    public void setMaxHeight(String maxHeight)
    {
        this.maxHeight = maxHeight;
    }

    public String getPhotoRef(){return photoRef;}

    public String getMaxWidth(){return maxWidth;}

    public String getMaxHeight(){return maxHeight;}
}
