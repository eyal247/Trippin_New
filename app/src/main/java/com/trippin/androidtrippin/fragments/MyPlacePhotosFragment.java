package com.trippin.androidtrippin.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.trippin.androidtrippin.adapters.PhotosGridViewAdapter;
import com.trippin.androidtrippin.model.AppConstants;
import com.trippin.androidtrippin.model.AppUtils;
import com.trippin.androidtrippin.model.ImageItem;
import com.trippin.androidtrippin.model.OnFragmentInteractionListener;
import com.trippin.androidtrippin.model.OnSnackBarActionClickListener;
import com.trippin.androidtrippin.trippin.AppController;
import com.trippin.androidtrippin.trippin.MainActivity;
import com.trippin.androidtrippin.R;
import com.trippin.androidtrippin.trippin.TripPlaceActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyPlacePhotosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPlacePhotosFragment extends Fragment implements OnSnackBarActionClickListener
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private final OnSnackBarActionClickListener snackBarListener = this;

    private View mainView;
    private GridView gridView;
    private LinearLayout addPhotoButton;
    private FrameLayout mainLayout;
    private PhotosGridViewAdapter gridAdapter;
    private ArrayList<ImageItem> placeImagesList = new ArrayList<>();
    private String placeServerID;
    private TextView noPhotosTV;
    private boolean addGreyImages;
    private boolean snackBarActionIsOnScreen = false;
//    private ImageButton refreshPlacePhotosIB;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyPlacePhotosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyPlacePhotosFragment newInstance(String param1, String param2)
    {
        MyPlacePhotosFragment fragment = new MyPlacePhotosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MyPlacePhotosFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mainView = inflater.inflate(R.layout.fragment_my_place_photos, container, false);
        addGreyImages = true;
        getUIComponents();
        disableAddPhotoButtonIfNeeded();
        setUIComponentsListeners();
        initGridView();
        getPlaceServerID();
        getUserPhotosFromServer();

        return mainView;
    }

    private void disableAddPhotoButtonIfNeeded()
    {
        if (getActivity().getIntent().hasExtra("caller")) {
            if (getActivity().getIntent().getExtras().getString("caller").equals("OtherUserTripActivity")) {
                addPhotoButton.setVisibility(View.GONE);
                noPhotosTV.setText("This user has no photos here.");
            }
            else
            {
                noPhotosTV.setText("You have no photos here\nStart uploading some!");
            }
        }
    }

    private void getUserPhotosFromServer()
    {
        JSONObject tripJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.GET_USER_PLACE_PHOTOS;
        //String url = "http://192.168.207.97:3000/getPlaceUploadedImagesIDs";

        try
        {
            tripJSON.put("server_id", placeServerID);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, tripJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    snackBarActionIsOnScreen = false;
                    setAddPhotoButtonListener();
                    handlePlaceUploadedImagesResponse(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    snackBarActionIsOnScreen = true;
                    AppUtils.showSnackBarMsgWithAction(getActivity(), mainLayout, snackBarListener);
                    System.out.println("error response on getUserPhotosFromServer()");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlePlaceUploadedImagesResponse(JSONObject response)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    uploadImagesToGrid(response);
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    Toast.makeText(getActivity(), "Server Error. Couldn't get place images", Toast.LENGTH_LONG).show();
                    break;
                default:
                    System.out.println("error on handlePlaceUploadedImagesResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void uploadImagesToGrid(JSONObject response)
    {
        if (response.has("images_ids"))
        {
            try
            {
                JSONArray imagesJSONArray = response.getJSONArray("images_ids");
                if (imagesJSONArray.length() == 0)
                {
                    noPhotosTV.setVisibility(View.VISIBLE);
                }
                else
                {
                    if(addGreyImages) {
                        addGreyImagesToGrid(imagesJSONArray.length());
                        addGreyImages = false;
                    }
                    noPhotosTV.setVisibility(View.GONE);
                    for (int i = 0; i < imagesJSONArray.length(); i++) {
                        String imageID = (String) imagesJSONArray.get(i);

                        StringBuilder key = new StringBuilder(imageID);
                        Bitmap bitmap = AppController.getInstance().getLruBitmapCache().getBitmap(key.toString());

                        if (bitmap == null) //image not in cache
                            requestImageFromServerAndDisplay(imageID, i);
                        else //image already in cache
                            loadImageToGrid(bitmap, imageID, i);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    private void addGreyImagesToGrid(int numOfImages)
    {
        Bitmap greyImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grey_image);
        for(int i = 0; i < numOfImages; i++){
            placeImagesList.add(new ImageItem(greyImageBitmap, "", ""));
            gridAdapter.notifyDataSetChanged();        }
    }

    private void requestImageFromServerAndDisplay(final String imageID, final int indexInGrid)
    {
        JSONObject imageJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.GET_USER_PLACE_IMAGE;
        //String url = "http://192.168.207.97:3000/getUserPlaceImage";

        try
        {
            imageJSON.put("image_id", imageID);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, imageJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    snackBarActionIsOnScreen = false;
                    handleGetImageResponse(response, imageID, indexInGrid);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    snackBarActionIsOnScreen = true;
                    AppUtils.showSnackBarMsgWithAction(getActivity(), mainLayout, snackBarListener);
                    System.out.println("error response on requestImageFromServerAndDisplay()");
                }
            });

            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleGetImageResponse(JSONObject response, String imageID, int indexInGrid)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    String imageStr = response.getString("image");
                    Bitmap bitmap = AppUtils.stringToBitMap(imageStr);

                    loadImageToGrid(bitmap, imageID, indexInGrid);
                    StringBuilder key = new StringBuilder(imageID);
                    AppController.getInstance().getLruBitmapCache().putBitmap(key.toString(), bitmap);
                    break;
                case AppConstants.RESPONSE_FAILURE:
                    Toast.makeText(getActivity(), "Server Error. Couldn't get image", Toast.LENGTH_LONG).show();
                    break;
                default:
                    System.out.println("error on handleGetImageResponse() (result)");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getPlaceServerID()
    {
        placeServerID = ((TripPlaceActivity)getActivity()).getPlaceServerID();
    }

    private void setUIComponentsListeners()
    {
        if (!AppUtils.isNetworkAvailable(MyPlacePhotosFragment.this.getActivity()))
            addPhotoButton.setOnClickListener(null);
        else
            if(!snackBarActionIsOnScreen)
                setAddPhotoButtonListener();

//        refreshPlacePhotosIB.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                if (AppUtils.isNetworkAvailable(MyPlacePhotosFragment.this.getActivity())) {
//                    refreshPlacePhotosIB.setVisibility(View.GONE);
//                    getUserPhotosFromServer();
//                } else
//                    AppUtils.showNoInternetConnectionToast(MyPlacePhotosFragment.this.getActivity());
//            }
//        });
    }

    private void setAddPhotoButtonListener()
    {
        addPhotoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (AppUtils.isNetworkAvailable(MyPlacePhotosFragment.this.getActivity()))
                    if (!snackBarActionIsOnScreen)
                        openUploadProfilePicDialog();
                    else if (!snackBarActionIsOnScreen)
                        AppUtils.showSnackBarMsg(AppConstants.NO_INTERNET_CONNECTION, getActivity(), mainLayout);
            }
        });
    }

    private void openUploadProfilePicDialog()
    {
        String[] imageOptions = {"Camera Roll", "Take a Photo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.changeProfilePicOptions)
                .setItems(imageOptions, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        executeSelectedOption(which);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void executeSelectedOption(int which)
    {
        switch (which){
            case AppConstants.CAMERA_ROLL_REQUEST_CODE-1:
                handleAddFromCameraRoll();
                break;
            case AppConstants.CAMERA_REQUEST_CODE-1:
                handleOpenCamera();
                break;
            default:
                System.out.println("in executeSelectedOption default myProfileFragment");
        }
    }

    private void handleOpenCamera()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 2);
    }

    private void handleAddFromCameraRoll()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), AppConstants.CAMERA_ROLL_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Uri imageUri;
        Bitmap imageBitmap = null;
        System.out.println("onActivityResult in myPlacePhotosFragment");

        if (requestCode == AppConstants.CAMERA_ROLL_REQUEST_CODE)
        {
            if (data != null)
            {
                System.out.println("Request code is 1 - from camera roll");
                imageUri = data.getData();
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (imageBitmap != null)
                {
                    Bitmap newSizedBitmap = AppUtils.resizeBitmap(imageBitmap, AppConstants.IMAGE_MAX_SIZE);
                    sendImageToServer(newSizedBitmap);
                }
            }
        }
        else if (requestCode == AppConstants.CAMERA_REQUEST_CODE)
        {
            if (data != null)
            {
                imageBitmap = (Bitmap) data.getExtras().get("data");
                Bitmap newSizedBitmap = AppUtils.resizeBitmap(imageBitmap, AppConstants.IMAGE_MAX_SIZE);
                sendImageToServer(newSizedBitmap);
            }
        }
    }

    private void sendImageToServer(final Bitmap newSizedBitmap)
    {
        showLoadingPanel();
        String imageStr = AppUtils.bitMapToString(newSizedBitmap);

        JSONObject imageJSON = new JSONObject();
        String url = AppConstants.SERVER_URL + AppConstants.SAVE_PLACE_IMAGE;
        //String url = "http://192.168.192.202:3000/savePlaceImage";

        try
        {
            imageJSON.put("parentTripPlaceID", placeServerID);
            imageJSON.put("image", imageStr);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, imageJSON, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    handleSaveImageResponse(response, newSizedBitmap);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    hideLoadingPanel();
                    Toast.makeText(getActivity(), "Server Error. Couldn't upload image", Toast.LENGTH_LONG).show();
                    System.out.println("error response on sendImageToServer()");
                }
            });

            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS*10,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MainActivity.addRequestToQueue(jsObjRequest);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showLoadingPanel()
    {
        mainView.findViewById(R.id.loading_panel_my_place_photos).setVisibility(View.VISIBLE);
        mainView.findViewById(R.id.my_place_photos_content_RL).setAlpha(0.4f);
    }

    private void hideLoadingPanel(){
        mainView.findViewById(R.id.loading_panel_my_place_photos).setVisibility(View.GONE);
        mainView.findViewById(R.id.my_place_photos_content_RL).setAlpha(1.0f);
    }

    private void handleSaveImageResponse(JSONObject response, Bitmap imageBitmap)
    {
        try
        {
            switch (response.getString("result"))
            {
                case AppConstants.RESPONSE_SUCCESS:
                    addImageToGrid(imageBitmap, response.getString("place_image_id"));
                    StringBuilder key = new StringBuilder(response.getString("place_image_id"));
                    AppController.getInstance().getLruBitmapCache().putBitmap(key.toString(), imageBitmap);

                    break;
                case AppConstants.RESPONSE_FAILURE:
                    Toast.makeText(getActivity(), "Server Error. Couldn't upload image", Toast.LENGTH_LONG).show();
                    break;
                default:
                    System.out.println("error on handleSaveImageResponse() (result)");
            }
            hideLoadingPanel();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadImageToGrid(Bitmap imageBitmap, String imageID, int indexInGrid)
    {
        placeImagesList.set(indexInGrid, new ImageItem(imageBitmap, "", imageID));
        gridAdapter.notifyDataSetChanged();
        noPhotosTV.setVisibility(View.GONE);
    }


    private void addImageToGrid(Bitmap imageBitmap, String imageID)
    {
        placeImagesList.add(new ImageItem(imageBitmap, "", imageID));
        gridAdapter.notifyDataSetChanged();
        noPhotosTV.setVisibility(View.GONE);
    }


    public Bitmap getResizedBitmap(Bitmap image, int maxSize)
    {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1)
        {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else
        {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void getUIComponents()
    {
        addPhotoButton = (LinearLayout) mainView.findViewById(R.id.upload_photo_LL);
        noPhotosTV = (TextView) mainView.findViewById(R.id.no_place_photos_TV);
        noPhotosTV.setVisibility(View.GONE);
        //refreshPlacePhotosIB = (ImageButton)mainView.findViewById(R.id.refresh_place_photos_IB);
        mainLayout = (FrameLayout)mainView.findViewById(R.id.my_place_photos_frame_layout);
    }

    private void initGridView()
    {
        gridView = (GridView) mainView.findViewById(R.id.gridView);
        gridAdapter = new PhotosGridViewAdapter(getActivity(), R.layout.grid_item_layout, placeImagesList); // getData()
        gridView.setAdapter(gridAdapter);
        setGridViewListener();
    }

    public void updateGridViewAfterDelete(String imageID)
    {
        for (int i = 0; i < placeImagesList.size(); i++)
        {
            if (placeImagesList.get(i).getServerID().equals(imageID)) {
                placeImagesList.remove(i);
                gridAdapter.notifyDataSetChanged();
                break;
            }
        }
        if(placeImagesList.size() == 0)
            noPhotosTV.setVisibility(View.VISIBLE);
    }

    private void setGridViewListener()
    {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ImageItem imageItemClicked = (ImageItem) parent.getItemAtPosition(position);
//                zoomImageFromThumb(view, imageItemClicked.getImage());

                int tempPosition = getCorrectPosition(position);
                if (tempPosition != AppConstants.NOT_FOUND)
                    position = tempPosition;
                ImageView iv = (ImageView)((LinearLayout)gridView.getChildAt(position)).getChildAt(0);
//                ImageView iv = new ImageView(getActivity());
//                iv.setImageBitmap(placeImagesList.get(position).getImage());
                AppController.getInstance().setCurrPlaceImagesStrings(getImagesStringsList());
                AppController.getInstance().setCurrPlaceImagesServerIds(getImagesIds());
                mListener.onFragmentInteraction("switchToImageActivity", iv, imageItemClicked.getServerID(), position);
            }
        });

//        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
//        {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
//            {
//                RotateAnimation anim = new RotateAnimation(0f, 350f, 15f, 15f);
//                anim.setInterpolator(new LinearInterpolator());
//                anim.setRepeatCount(Animation.INFINITE);
//                anim.setDuration(700);
//
//// Start animating the image
//                ImageView imageItemClicked = (ImageView)parent.getItemAtPosition(position);
//                imageItemClicked.startAnimation(anim);
//
//// Later.. stop the animation
//                imageItemClicked.setAnimation(null);
//                return false;
//            }
//        });
    }

    private ArrayList<String> getImagesIds()
    {
        ArrayList<String> imagesServerIds = new ArrayList<>();

        for(int i = 0 ; i < placeImagesList.size() ; i++)
        {
            imagesServerIds.add(i, placeImagesList.get(i).getServerID());
        }

        return imagesServerIds;
    }

    private ArrayList<String> getImagesStringsList()
    {
        ArrayList<String> imagesStrings = new ArrayList<>();

        for(int i = 0 ; i < placeImagesList.size() ; i++)
        {
            imagesStrings.add(i, AppUtils.bitMapToString(placeImagesList.get(i).getImage()));
        }

        return imagesStrings;
    }

    private int getCorrectPosition(int position)
    {
        int firstPosition = gridView.getFirstVisiblePosition();
        int lastPosition = gridView.getLastVisiblePosition();

        if ((position < firstPosition) || (position > lastPosition))
            return -1;

        return position - firstPosition;
    }

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
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSnackBarActionClick()
    {
        getUserPhotosFromServer();
    }
}
