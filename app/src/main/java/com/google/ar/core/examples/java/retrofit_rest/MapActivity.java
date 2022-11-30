package com.google.ar.core.examples.java.retrofit_rest;

import android.app.Application;
import android.content.res.AssetManager;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "테스트용";
    String API_Key;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
//        RoadTracker rt = new RoadTracker(googleMap);
        LatLng start = new LatLng(37.449522, 127.126941);
        LatLng end = new LatLng(37.4119623, 127.1284907);
        API_Key = getResources().getString(R.string.tMapAPIKey);
//        ArrayList<LatLng> jsonData = rt.getJsonData(start, end);
        try {
            new RoadTracker().execute(String.valueOf(start.longitude), String.valueOf(start.latitude),
                    String.valueOf(end.longitude), String.valueOf(end.latitude),
                    URLEncoder.encode("출발지", "UTF-8"), URLEncoder.encode("도착지", "UTF-8"),
                    API_Key);
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

    private ArrayList<Double> longitudes = new ArrayList<>();
    private ArrayList<Double> latitudes = new ArrayList<>();

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String tempUID = "2BXzuCaFIYXf7Dp06sHMCrTNSH43";

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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://apis.openapi.sk.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<RouteDTO> result = retrofitService.getPosts(1, "result", positions[6], Double.parseDouble(positions[0]),
                Double.parseDouble(positions[1]), Double.parseDouble(positions[2]), Double.parseDouble(positions[3]), positions[4], positions[5]);
        try {
            RouteDTO body = result.execute().body();
//            for(int i = 0; i<body.getFeatures().size(); i++) {
//                Log.e(TAG, "doInBackground: " + body.getFeatures().get(i).getGeometry().getCoordinates().toString());
//                String base = body.getFeatures().get(i).getGeometry().getCoordinates().toString();
//                if(base.charAt(1) == '[') {
//                    Log.e(TAG, "doInBackground: " + i );
//                }
//            }
            // 파싱 클래스에서 오류가 발생해 Object로 받아온 좌표에 대해서만 직접 파싱
            for(int v = 0; v < body.getFeatures().size(); v++) {
                String base = body.getFeatures().get(v).getGeometry().getCoordinates().toString();
                if(base.charAt(1) == '[') {
                    base = base.substring(1);
                    base = base.substring(0, base.length() - 1);
                    String[] _split = base.split("\\[");
                    for (int i = 1; i < _split.length; i++) {
                        String[] _splitDepth = _split[i].split(",");
                        for (int k = 0; k < _splitDepth.length; k++) {
                            if (!_splitDepth[k].equals(" ")) {
                                if (k == 0) {
                                    Double tempLnd = Double.parseDouble(_splitDepth[k]);
                                    longitudes.add(tempLnd);
                                } else {
                                    Double tempLat = Double.parseDouble(_splitDepth[k].substring(0, _splitDepth[k].length() - 1));
                                    latitudes.add(tempLat);
                                }
                            }
                        }
                    }
                } else if(base.charAt(0) == '[') {
                    String[] _split = base.split("\\[");
                    for(int j = 0; j < _split.length; j++) {
                        String[] _splitDepth = _split[j].split(",");
                        for(int l = 0; l < _splitDepth.length; l++) {
                            if(!_splitDepth[l].equals(" ") && _splitDepth[l].length() > 0) {
                                if (l == 0) {
                                    Double tempLnd = Double.parseDouble(_splitDepth[l]);
                                    longitudes.add(tempLnd);
                                } else {
                                    Double tempLat = Double.parseDouble(_splitDepth[l].substring(0, _splitDepth[l].length() - 1));
                                    latitudes.add(tempLat);
                                }
                            }
                        }
                    }
                }
            }
            Log.e(TAG, "doInBackground: " + longitudes.size() + " " + latitudes.size());
            Map insertData = new HashMap<String, String>();
//            insertData.put("passes", coords);
            db.collection("users").document(tempUID).collection("nav").document(tempUID).set(insertData);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        retrofitService.getPosts(1, "result", API_Key, Double.parseDouble(positions[0]),
//                Double.parseDouble(positions[1]), Double.parseDouble(positions[2]), Double.parseDouble(positions[3]), positions[4], positions[5])
//                .enqueue(new Callback<RouteDTO>() {
//                    @Override
//                    public void onResponse(Call<RouteDTO> call, Response<RouteDTO> response) {
//                        if(response.isSuccessful()) {
//                            RouteDTO data = response.body();
//                            Log.e(TAG, "onResponse: " + data.getFeatures().get(0).getGeometry());
//                        }
//                    }
//                    @Override
//                    public void onFailure(Call<RouteDTO> call, Throwable t) {
//                        Log.e(TAG, "onFailure: " + t);
//                    }
//                });
        return mapPoints;
    }
}


