package com.trippin.androidtrippin.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by EyalEngel on 04/06/15.
 */
public class Note implements Parcelable
{
    private String noteTitle;
    private String noteDate;
    private String noteText;
    private String noteID;

    public Note(String noteTitle, String noteDate, String noteText, String noteID)
    {
        this.noteTitle = noteTitle;
        this.noteDate = noteDate;
        this.noteText = noteText;
        this.noteID = noteID;
    }

    public Note()
    {

    }

    public Note(Parcel in){
        readFromParcel(in);
    }

    public String getNoteID()
    {
        return noteID;
    }

    public void setNoteID(String noteID)
    {
        this.noteID = noteID;
    }

    public String getNoteText()
    {
        return noteText;
    }

    public void setNoteText(String noteText)
    {
        this.noteText = noteText;
    }

    public String getNoteTitle()
    {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle)
    {
        this.noteTitle = noteTitle;
    }

    public String getNoteDate()
    {
        return noteDate;
    }

    public void setnoteDate(String noteDate)
    {
        this.noteDate = noteDate;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(noteTitle);
        dest.writeString(noteDate);
        dest.writeString(noteText);
        dest.writeString(noteID);
    }

    private void readFromParcel(Parcel in){
        noteTitle = in.readString();
        noteDate = in.readString();
        noteText = in.readString();
        noteID = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Note createFromParcel(Parcel in) {return new Note(in); }

        @Override
        public Object[] newArray(int size)
        {
            return new Object[size];
        }

        public Note newNote() {return new Note();}
    };
}
