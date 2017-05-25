package com.trippin.androidtrippin.model;

import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by EyalEngel on 01/09/15.
 */
public class DateUtils
{
    public static Date getDate(int year, int month, int day)
    {
        Calendar cal = Calendar.getInstance();

        cal.set(year, month, day);

        return cal.getTime();
    }

    public static void setDatesEditTexts(int year, int month, int day, EditText departureET)
    {
        departureET.setText(new StringBuilder()
                // Month is 0 based, so you have to add 1
                .append(month+1).append("/")
                .append(day).append("/")
                .append(year).append(" "));
    }

    public static String dateToString(Date date)
    {
//        SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = new SimpleDateFormat("MM/dd/yyyy").format(date);

        return dateStr;
    }

    public static String dateToShortFormatString(Date date)
    {
        String shortDateFormat = new SimpleDateFormat("MMM yyyy").format(date);

        return shortDateFormat;
    }

    public static Date stringToDate(String dateStr)
    {
        Calendar cal = Calendar.getInstance();

        SimpleDateFormat myFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = null;
        try {
            if(!dateStr.equals(AppConstants.EMPTY_STRING))
                date = myFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(date);

        return date;
    }

    public static int dateToAge(String birthday) {
        int age = 18;

        Date dob = stringToDate(birthday);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dob);
        int birthdayYear = cal.get(Calendar.YEAR);

        Calendar now = Calendar.getInstance();
        int currYear = now.get(Calendar.YEAR);

        if(now.get(Calendar.MONTH) > cal.get(Calendar.MONTH))
            age = currYear - birthdayYear;
        else if (now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) > cal.get(Calendar.DAY_OF_MONTH))
            age = currYear - birthdayYear;
        else if (now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) < cal.get(Calendar.DAY_OF_MONTH))
            age = currYear - birthdayYear - 1;
        else if (now.get(Calendar.MONTH) < cal.get(Calendar.MONTH))
            age = currYear - birthdayYear - 1;

        return age;
    }
}
