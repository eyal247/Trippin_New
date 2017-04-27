package com.trippin.androidtrippin.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.DateUtils;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by EyalEngel on 24/07/15.
 */
public class DatesPickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
{
    private OnFragmentInteractionListener mListener;
    private String dateType;
    private int datePickerStyle;
    private Calendar calendar;
    private Calendar calendarHelper;
    private Date otherTypeDate;
    private Date currDateInET;

    public DatesPickerFragment()
    {

    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        this.dateType = args.getString("date_type_clicked");
        this.otherTypeDate = DateUtils.stringToDate(args.getString("other_type_date"));
        datePickerStyle = R.style.MyDatePickerDialogTheme;
        this.currDateInET = DateUtils.stringToDate(args.getString("curr_date"));
    }

//    public DatesPickerFragment(String dateType, Date currDateInET, Date otherTypeDate)
//    {
//        this.dateType = dateType;
//        this.otherTypeDate = otherTypeDate;
//        datePickerStyle = R.style.MyDatePickerDialogTheme;
//        this.currDateInET = currDateInET;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        calendar = Calendar.getInstance();
        calendarHelper = Calendar.getInstance();
        Date today = calendar.getTime();


        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        //today = calendar.getTime();

        DatePickerDialog dpd = new DatePickerDialog(getActivity(), datePickerStyle, this, year, month, day);

        if(dateType.equals("departureET")) //Departure Date was clicked
        {
            dpd.setTitle("Choose Departure Date");
//            Date returnDate = ((NewTripForm)getActivity()).getReturnDateForDateFragment();
            if(otherTypeDate != null)
            {
                if(!otherTypeDate.equals(AppConstants.EMPTY_STRING)) {
                    if (currDateInET == null) {
                        if (otherTypeDate.after(today)) {
                            calendar.setTime(today);
                            calendarHelper.setTime(otherTypeDate);
                            dpd.getDatePicker().setMaxDate(calendarHelper.getTimeInMillis());
                        } else {
                            calendar.setTime(otherTypeDate);
                            dpd.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                        }
                    } else {
                        calendar.setTime(currDateInET);
                        calendarHelper.setTime(otherTypeDate);
                        dpd.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                        dpd.getDatePicker().setMaxDate(calendarHelper.getTimeInMillis());
                    }
                }
            }
        }
        else //Return Date was clicked
        {
            dpd.setTitle("Choose Return Date");
//            Date departureDate = ((NewTripForm)getActivity()).getDepartureDateForDateFragment();

            if(otherTypeDate != null)
            {
                if(currDateInET==null) {
                    calendar.setTime(otherTypeDate);
                    dpd.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    dpd.getDatePicker().setMinDate(calendar.getTimeInMillis());
                }
                else {
                    calendar.setTime(currDateInET);
                    calendarHelper.setTime(otherTypeDate);
                    dpd.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    dpd.getDatePicker().setMinDate(calendarHelper.getTimeInMillis());
                }
            }
        }

        return dpd;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
    {
        Bundle bundle = new Bundle();

        bundle.putInt("year", year);
        bundle.putInt("month", monthOfYear);
        bundle.putInt("day", dayOfMonth);

        mListener.onFragmentInteraction("datesFromDatesPicker", bundle);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }
}
