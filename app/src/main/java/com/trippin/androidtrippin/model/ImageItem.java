package com.trippin.androidtrippin.model;


import android.graphics.Bitmap;

public class ImageItem
{
    private Bitmap image;
    private String title;
    private String serverID;

    public ImageItem(Bitmap image, String title, String serverID)
    {
        super();
        this.image = image;
        this.title = title;
        this.serverID = serverID;
    }

    public String getServerID()
    {
        return serverID;
    }

    public void setServerID(String serverID)
    {
        this.serverID = serverID;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
