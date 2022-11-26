package com.google.ar.core.examples.java.retrofit_rest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.ar.core.examples.java.geospatial.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "테스트용";
    String API_Key = "l7xxa97487515286485e98d6fafe222d88c7";



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

//        // T Map View
//        tMapView = new TMapView(this);
//
//        // API Key
//        tMapView.setSKTMapApiKey(API_Key);
//
//        // Initial Setting
//        tMapView.setZoomLevel(17);
//        tMapView.setIconVisibility(true);
//        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
//        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
//
//        // T Map View Using Linear Layout
//        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
//        linearLayoutTmap.addView(tMapView);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
//        RoadTracker rt = new RoadTracker(googleMap);
        LatLng start = new LatLng(37.449522, 127.126941);
        LatLng end = new LatLng(37.4119623, 127.1284907);
//        ArrayList<LatLng> jsonData = rt.getJsonData(start, end);
        try {
            new RoadTracker().execute(String.valueOf(start.longitude), String.valueOf(start.latitude),
                    String.valueOf(end.longitude), String.valueOf(end.latitude),
                    URLEncoder.encode("출발지", "UTF-8"), URLEncoder.encode("도착지", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

class RoadTracker extends AsyncTask<String, Void, ArrayList<LatLng>> {

    private static final String TAG = "RoadTracker";

//    private GeoApiContext mContext;

    private ArrayList<LatLng> mCapturedLocations = new ArrayList<LatLng>();        //지나간 좌표 들을 저장하는 List

    private static final int PAGINATION_OVERLAP = 5;

    private static final int PAGE_SIZE_LIMIT = 100;

    private ArrayList<LatLng> mapPoints;

//
//    int totalDistance;
//
//
//    public RoadTracker(GoogleMap map) {
//        mMap = map;
//    }

//    public ArrayList<LatLng> getJsonData(final LatLng startPoint, final LatLng endPoint) {
//
//        String API_Key = "l7xxa97487515286485e98d6fafe222d88c7";
//
////
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://apis.openapi.sk.com")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
//        Call<Object> result = retrofitService.getPosts(API_Key, startPoint.longitude, startPoint.latitude,
//                endPoint.longitude, endPoint.latitude);
//        try {
//            System.out.println(result.execute().body());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return mapPoints;
////        return mapPoints;
//
//    }

    @Override
    protected ArrayList<LatLng> doInBackground(String... positions) {
        String API_Key = "l7xxa97487515286485e98d6fafe222d88c7";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://apis.openapi.sk.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<Object> result = retrofitService.getPosts(1, "result", API_Key, Double.parseDouble(positions[0]),
                Double.parseDouble(positions[1]), Double.parseDouble(positions[2]), Double.parseDouble(positions[3]), positions[4], positions[5]);
        try {
            Log.e(TAG, "" + result.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapPoints;
    }
}


