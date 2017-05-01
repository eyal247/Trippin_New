package com.trippin.androidtrippin.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.Note;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.trippin.MainActivity;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.trippin.TripPlaceActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.trippin.androidtrippin.model.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoteFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String NOTE_TITLE = "noteTitle";
    private static final String NOTE_DATE = "noteDate";
    private static final String NOTE_TEXT = "noteText";
    private static final String NOTE_ID = "noteID";

    // TODO: Rename and change types of parameters

    private OnFragmentInteractionListener mListener;

    private View mainView;
    private RelativeLayout noteMainLayout;
    private LineEditText noteET;
    private TextView noteTitleTV;
    private TextView noteDateTV;
    private String currDate = "";
    private String noteTitle;
    private String noteDate;
    private String noteText;
    private String noteID;
    private String placeServerID;
    private boolean firstFragmentLaunch = true;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param noteTitle Parameter 1.
     * @param noteDate Parameter 2.
     * @param noteText Parameter 3.
     * @param noteID Parameter 4.
     * @return A new instance of fragment NoteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NoteFragment newInstance(String noteTitle, String noteDate, String noteText, String noteID)
    {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        args.putString(NOTE_TITLE, noteTitle);
        args.putString(NOTE_DATE, noteDate);
        args.putString(NOTE_TEXT, noteText);
        args.putString(NOTE_ID, noteID);
        fragment.setArguments(args);
        return fragment;
    }

    public NoteFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noteTitle = getArguments().getString(NOTE_TITLE);
            noteDate = getArguments().getString(NOTE_DATE);
            noteText = getArguments().getString(NOTE_TEXT);
            noteID = getArguments().getString(NOTE_ID);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_note, container, false);
        getPlaceServerID();
        getUIComponents();
        disableNoteIfNeeded();
        setUIComponents();
        setUIComponentsListeners();
        getNoteFromServer();

        
        return mainView;
    }

    private void disableNoteIfNeeded()
    {
        if (getActivity().getIntent().hasExtra("caller")) {
            if (getActivity().getIntent().getExtras().getString("caller").equals("OtherUserTripActivity")){
                noteET.setFocusable(false);
                noteET.setEnabled(false);
            }
        }
    }

    private void getNoteFromServer()
    {
        JSONObject noteJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.GET_PLACE_NOTE;
        //String url = "http://192.168.205.237:3000/getPlaceNote";

        try
        {
            noteJSON.put("noteID", noteID);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, noteJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    handleGetNoteResponse(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("error response on getNoteFromServer()");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleGetNoteResponse(JSONObject response)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    parseResponseToNote(response);
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    AppUtils.showSnackBarMsg("Server Error. Couldn't get Note", getActivity(), noteMainLayout);
                    break;
                default:
                    System.out.println("error on handleCreateTripResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseResponseToNote(JSONObject response)
    {
        String returnedDate = null;

        if(response.has("place_note")) {
            try {

                JSONObject noteObject = response.getJSONObject("place_note");
                returnedDate = noteObject.optString("noteDate");
                if(!returnedDate.equals("New Note")) {
                    this.noteDate = noteObject.optString("noteDate");
                    this.noteTitle = noteObject.optString("noteTitle");
                    this.noteText = noteObject.optString("noteText");
                    setUIComponents();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUIComponents()
    {
        if(noteDate.equals(AppConstants.EMPTY_STRING))
            setDateText();
        else
            noteDateTV.setText("Last Modified: " + noteDate);

        if(!noteText.equals(AppConstants.EMPTY_STRING))
            noteET.setText(noteText);

        noteTitleTV.setText(noteTitle);
    }

    private void setDateText()
    {
        if(currDate.equals(AppConstants.EMPTY_STRING)) {
            long msTime = System.currentTimeMillis();
            Date curDateTime = new Date(msTime);

            SimpleDateFormat formatter = new SimpleDateFormat("dd'/'MM'/'y");
            currDate = formatter.format(curDateTime);

            noteDateTV.setText("Last Modified: " + currDate);
            noteDate = currDate;
        }
    }

    private void updateDate(){
        long msTime = System.currentTimeMillis();
        Date curDateTime = new Date(msTime);

        SimpleDateFormat formatter = new SimpleDateFormat("dd'/'MM'/'y");
        currDate = formatter.format(curDateTime);

        noteDateTV.setText("Last Modified: " + currDate);
        noteDate = currDate;
    }

    private void getUIComponents()
    {
        noteTitleTV = (TextView)mainView.findViewById(R.id.note_title);
        noteET = (LineEditText)mainView.findViewById(R.id.note_body);
        noteDateTV = (TextView)mainView.findViewById(R.id.notelist_date);
        noteMainLayout = (RelativeLayout)mainView.findViewById(R.id.note_main_layout);
    }

    private void setUIComponentsListeners()
    {

        noteET.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                System.out.println("Note text changed");
                if(!firstFragmentLaunch)
                    updateDate();
                firstFragmentLaunch = false;
                //noteText = s.toString();
                //noteText = noteET.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable s)
            {
//                if(s.length() > 0){
//                    noteText = s.toString();
//                    //sendNoteToServer();
//                    System.out.println(noteText);
//                }

                //noteText = noteET.getText().toString();
            }
        });
    }

    private void getPlaceServerID()
    {
        placeServerID = ((TripPlaceActivity)getActivity()).getPlaceServerID();
    }

    // TODO: Rename method, update argument and hook method into UI event

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        noteText = noteET.getText().toString();
        Note note = new Note(noteTitle, noteDate, noteText, noteID);
        notifyActivityOfObject(note);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    public void sendInformation(Object obj)
    {
        Log.e("TAG: ", "Object received by Fragment");
    }

    private void notifyActivityOfObject(Object obj)
    {
        mListener.onFragmentInteraction("saveNote", obj);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */


    public static class LineEditText extends EditText
    {
        private Rect mRect;
        private Paint mPaint;

        // we need this constructor for LayoutInflater
        public LineEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setColor(getResources().getColor(R.color.colorPrimary));
        }

        @Override
        protected void onDraw(Canvas canvas) {

            int height = getHeight();
            int line_height = getLineHeight();

            int count = height / line_height;

            if (getLineCount() > count)
                count = getLineCount();

            Rect r = mRect;
            Paint paint = mPaint;
            int baseline = getLineBounds(0, r);

            for (int i = 0; i < count; i++) {

                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
                baseline += getLineHeight();

                super.onDraw(canvas);
            }

        }
    }

}
