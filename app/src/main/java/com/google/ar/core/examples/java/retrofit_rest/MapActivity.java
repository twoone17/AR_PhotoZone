package com.google.ar.core.examples.java.retrofit_rest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.ar.core.examples.java.geospatial.ArNav;
import com.google.ar.core.examples.java.geospatial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private CancellationToken token;

    private static final String TAG = "????????????";

    private String photoZoneName;

    String API_Key;

    private double start_lat = 37.413003;
    private double start_lng = 127.125923;
    // to ????????? 4??? ??????
//    private double end_lat = 37.4119623;
//    private double end_lng = 127.1284907;
    // to ?????????
    private double end_lat = 37.4556093;
    private double end_lng = 127.1271491;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        start_lat = getIntent().getDoubleExtra("startLatitude", 0);
        start_lng = getIntent().getDoubleExtra("startLongitude", 0);
        end_lat = getIntent().getDoubleExtra("endLatitude", 0);
        end_lng = getIntent().getDoubleExtra("endLongitude", 0);
        photoZoneName = getIntent().getStringExtra("photoZoneName");

        Log.e(TAG, "onCreate: " + start_lat + " " + start_lng + " " + end_lat + " " + end_lng);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        tokenInit();
//        checkDangerousPermissions();

        // ??????????????? ???????????? ?????? ???????????? ??????????????? ??????.
        setCurrentLocation();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.e(TAG, "onMapReady: " + "?????? ?????????" );
    }

    private void tokenInit() {
        token = new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        };
    }

    private void setCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, token)
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    start_lat = location.getLatitude();
                    start_lng = location.getLongitude();
                    Log.e(TAG, "?????? ?????? ?????? ??????" + start_lat + " " + start_lng);

                    API_Key = getResources().getString(R.string.tMapAPIKey);
                    try {
                        new RoadTracker(MapActivity.this).execute(String.valueOf(start_lng), String.valueOf(start_lat),
                                String.valueOf(end_lng), String.valueOf(end_lat),
                                URLEncoder.encode("?????????", "UTF-8"), URLEncoder.encode("?????????", "UTF-8"),
                                API_Key, photoZoneName);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


}

class RoadTracker extends AsyncTask<String, Void, ArrayList<LatLng>> {

    private static final String TAG = "RoadTracker";

    private final MapActivity intentFlow;

    private ArrayList<LatLng> mapPoints;

    private ArrayList<Double> longitudes = new ArrayList<>();
    private ArrayList<Double> latitudes = new ArrayList<>();


    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public RoadTracker(MapActivity intentFlow) {
        this.intentFlow = intentFlow;
    }

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
                                    if(!longitudes.contains(tempLnd)) {
//                                        Log.e(TAG, "" + body.getFeatures().get(v).getProperties().getDistance());
                                        longitudes.add(tempLnd);
                                    }
                                } else {
                                    Double tempLat = Double.parseDouble(_splitDepth[k].substring(0, _splitDepth[k].length() - 1));
                                    if(!latitudes.contains(tempLat)) {
//                                        Log.e(TAG, "" + body.getFeatures().get(v).getProperties().getDistance());
                                        latitudes.add(tempLat);
                                    }
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
                                    if(!longitudes.contains(tempLnd)) {
                                        longitudes.add(tempLnd);
                                    }
                                } else {
                                    Double tempLat = Double.parseDouble(_splitDepth[l].substring(0, _splitDepth[l].length() - 1));
                                    if(!latitudes.contains(tempLat)) {
                                        latitudes.add(tempLat);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            coords_extend(latitudes, longitudes, positions[7]);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapPoints;
    }

    private void coords_extend(ArrayList<Double> latitudes, ArrayList<Double> longitudes, String photoZoneName) {

        Log.e(TAG, "size: " + latitudes.size() );

        ArrayList<Double> extended_coords_lat = new ArrayList<>();
        ArrayList<Double> extended_coords_lng = new ArrayList<>();

        // deep copy
        for(int k=0; k< latitudes.size(); k++) {
            extended_coords_lat.add(latitudes.get(k));
            extended_coords_lng.add(longitudes.get(k));
        }

        for(int i=1; i<latitudes.size(); i++) {
            Location locationA = new Location("first point");
            locationA.setLatitude(latitudes.get(i-1));
            locationA.setLongitude(longitudes.get(i-1));

            Location locationB = new Location("second point");
            locationB.setLatitude(latitudes.get(i));
            locationB.setLongitude(longitudes.get(i));

            float distance = locationA.distanceTo(locationB);
            // ????????? ????????????.
            // 10m, 50m, 100m
            // 10m??? ?????? 3?????? ????????? ??????????????????.
            // 50m??? ?????? 5??? ????????? ?????????,
            // 100m????????? ????????? ??????????????? ?????? ??????????????? ?????????.

            if(distance > 5 && distance < 10) {
                double _middle_lat = (locationA.getLatitude() + locationB.getLatitude())/2;
                double _middle_lng = (locationA.getLongitude() + locationB.getLongitude())/2;
                extended_coords_lat.add(_middle_lat);
                extended_coords_lng.add(_middle_lng);
            } else
                if(distance > 10 && distance < 50) {
                double _middle_lat = (locationA.getLatitude() + locationB.getLatitude())/2;
                double _middle_lng = (locationA.getLongitude() + locationB.getLongitude())/2;
                extended_coords_lat.add(_middle_lat);
                extended_coords_lng.add(_middle_lng);

                // 2??? ??????
                double _first_middle_lat = (locationA.getLatitude() + _middle_lat) / 2;
                double _first_middle_lng = (locationA.getLongitude() + _middle_lng) / 2;
                double _last_middle_lat = (locationB.getLatitude() + _middle_lat) / 2;
                double _last_middle_lng = (locationB.getLongitude() + _middle_lng) / 2;;
                extended_coords_lat.add(_first_middle_lat);
                extended_coords_lat.add(_last_middle_lat);
                extended_coords_lng.add(_first_middle_lng);
                extended_coords_lng.add(_last_middle_lng);
            } else if(distance > 100) {

                Log.e(TAG, "????????? : " + i + " ?????? : " + latitudes.get(i) + " " + longitudes.get(i));
                Log.e(TAG, "?????? : " + distance );
                ////////////////////////////////////////////////////////////////////////////
                // ??????????????? ??????????????? ????????? ?????? ????????? ?????? ????????? ?????? ??????
                // 1??? ??????
                double _middle_lat = (locationA.getLatitude() + locationB.getLatitude())/2;
                double _middle_lng = (locationA.getLongitude() + locationB.getLongitude())/2;
                extended_coords_lat.add(_middle_lat);
                extended_coords_lng.add(_middle_lng);

                // 2??? ??????
                double _first_middle_lat = (locationA.getLatitude() + _middle_lat) / 2;
                double _first_middle_lng = (locationA.getLongitude() + _middle_lng) / 2;
                double _last_middle_lat = (locationB.getLatitude() + _middle_lat) / 2;
                double _last_middle_lng = (locationB.getLongitude() + _middle_lng) / 2;;
                extended_coords_lat.add(_first_middle_lat);
                extended_coords_lat.add(_last_middle_lat);
                extended_coords_lng.add(_first_middle_lng);
                extended_coords_lng.add(_last_middle_lng);

                // 3??? ??????
                double _first_first_middle_lat = (locationA.getLatitude() + _first_middle_lat) / 2;
                double _first_first_middle_lng = (locationA.getLongitude() + _first_middle_lng) / 2;
                double _first_last_middle_lat = (locationA.getLatitude() + _last_middle_lat) / 2;
                double _first_last_middle_lng = (locationA.getLongitude() + _last_middle_lng) / 2;
                double _last_first_middle_lat = (locationB.getLatitude() + _first_middle_lat) / 2;
                double _last_first_middle_lng = (locationB.getLatitude() + _first_middle_lng) / 2;
                double _last_last_middle_lat = (locationB.getLatitude() + _last_middle_lat) / 2;
                double _last_last_middle_lng = (locationB.getLatitude() + _last_middle_lng) / 2;
                extended_coords_lat.add(_first_first_middle_lat);
                extended_coords_lat.add(_first_last_middle_lat);
                extended_coords_lat.add(_last_first_middle_lat);
                extended_coords_lat.add(_last_last_middle_lat);
                extended_coords_lng.add(_first_first_middle_lng);
                extended_coords_lng.add(_first_last_middle_lng);
                extended_coords_lng.add(_last_first_middle_lng);
                extended_coords_lng.add(_last_last_middle_lng);

            }
        }
        Map insertData = new HashMap<String, List<Double>>();
        insertData.put("latitudes", extended_coords_lat);
        insertData.put("longitudes", extended_coords_lng);
        db.collection("users").document(auth.getCurrentUser().getUid()).collection("nav").document(auth.getCurrentUser().getUid()).set(insertData)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Intent intent = new Intent(intentFlow.getApplicationContext(), ArNav.class);
                    intent.putExtra("photoZoneName", photoZoneName);
                    intentFlow.startActivity(intent);
                }
            }
        });
    }
}


