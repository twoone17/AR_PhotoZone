package com.google.ar.core.examples.java.geospatial;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Earth;
import com.google.ar.core.Frame;
import com.google.ar.core.GeospatialPose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper;
import com.google.ar.core.examples.java.common.helpers.LocationPermissionHelper;
import com.google.ar.core.examples.java.common.helpers.SnackbarHelper;
import com.google.ar.core.examples.java.common.helpers.TrackingStateHelper;
import com.google.ar.core.examples.java.common.samplerender.Framebuffer;
import com.google.ar.core.examples.java.common.samplerender.Mesh;
import com.google.ar.core.examples.java.common.samplerender.SampleRender;
import com.google.ar.core.examples.java.common.samplerender.Shader;
import com.google.ar.core.examples.java.common.samplerender.Texture;
import com.google.ar.core.examples.java.common.samplerender.arcore.BackgroundRenderer;
import com.google.ar.core.examples.java.geospatial.PrivacyNoticeDialogFragment.NoticeDialogListener;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.FineLocationPermissionNotGrantedException;
import com.google.ar.core.exceptions.GooglePlayServicesLocationLibraryNotLinkedException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.core.exceptions.UnsupportedConfigurationException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ArLikes extends AppCompatActivity
        implements SampleRender.Renderer, NoticeDialogListener {

    private static final String TAG = GeospatialActivity.class.getSimpleName();

    private static final String SHARED_PREFERENCES_SAVED_ANCHORS = "SHARED_PREFERENCES_SAVED_ANCHORS";
    private static final String ALLOW_GEOSPATIAL_ACCESS_KEY = "ALLOW_GEOSPATIAL_ACCESS";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000f;
    private static final double LOCALIZING_HORIZONTAL_ACCURACY_THRESHOLD_METERS = 10;
    private static final double LOCALIZING_HEADING_ACCURACY_THRESHOLD_DEGREES = 15;

    private static final double LOCALIZED_HORIZONTAL_ACCURACY_HYSTERESIS_METERS = 10;
    private static final double LOCALIZED_HEADING_ACCURACY_HYSTERESIS_DEGREES = 10;

    private static final int LOCALIZING_TIMEOUT_SECONDS = 180;
    private static final int MAXIMUM_ANCHORS = 1000;

    private boolean CONCURRENT_PREVENT_FLAG = false;

    private GLSurfaceView surfaceView;

    private boolean installRequested;
    private Integer clearedAnchorsAmount = null;
    private long localizingStartTimestamp;

    private FirebaseFirestore db;

    enum State {
        UNINITIALIZED,
        UNSUPPORTED,
        EARTH_STATE_ERROR,
        PRETRACKING,
        LOCALIZING,
        LOCALIZING_FAILED,
        LOCALIZED
    }

    private State state = State.UNINITIALIZED;
    private StoredGeolocation storedGeolocation;
    private Session session;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private DisplayRotationHelper displayRotationHelper;
    private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);
    private SampleRender render;
    private SharedPreferences sharedPreferences;

    private String lastStatusText;
    private TextView statusTextView;
    private Button setAnchorButton;
    private Button setLikesButton;
    private Button clearAnchorsButton;


    final FirebaseAuth auth = FirebaseAuth.getInstance();


    private BackgroundRenderer backgroundRenderer;
    private Framebuffer virtualSceneFramebuffer;
    private boolean hasSetTextureNames = false;

    private Mesh likesObjectMesh;
    private Shader likesObjectShader;

    private final List<Anchor> anchors = new ArrayList<>();

    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16]; // view x model
    private final float[] modelViewProjectionMatrix = new float[16]; // projection x view x model


    private String photoZoneName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        db = FirebaseFirestore.getInstance();

        setContentView(R.layout.activity_geospatial_camera_view_likes);
        surfaceView = findViewById(R.id.surfaceview);
        statusTextView = findViewById(R.id.status_text_view);
        setAnchorButton = findViewById(R.id.set_anchor_button);
        setLikesButton = findViewById(R.id.set_likes_button);
        clearAnchorsButton = findViewById(R.id.clear_anchors_button);
        setAnchorButton.setOnClickListener(view -> handleSetAnchorButton());
        setLikesButton.setOnClickListener(view -> handleSetLikesButton());
        clearAnchorsButton.setOnClickListener(view -> handleClearAnchorsButton());

        displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

        render = new SampleRender(surfaceView, this, getAssets());

        installRequested = false;
        clearedAnchorsAmount = null;

        Intent intent = getIntent();
        photoZoneName = intent.getStringExtra("photoZoneName");
    }

    @Override
    protected void onDestroy() {
        if (session != null) {
            session.close();
            session = null;
        }

        // TODO 여기서 안내 종료 관련 기능 추가하기
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sharedPreferences.getBoolean(ALLOW_GEOSPATIAL_ACCESS_KEY, /*defValue=*/ false)) {
            createSession();
        } else {
            showPrivacyNoticeDialog();
        }

        surfaceView.onResume();
        displayRotationHelper.onResume();
    }

    private void showPrivacyNoticeDialog() {
        DialogFragment dialog = PrivacyNoticeDialogFragment.createDialog();
        dialog.show(getSupportFragmentManager(), PrivacyNoticeDialogFragment.class.getName());
    }

    private void createSession() {
        Exception exception = null;
        String message = null;
        if (session == null) {

            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }
                if (!LocationPermissionHelper.hasFineLocationPermission(this)) {
                    LocationPermissionHelper.requestFineLocationPermission(this);
                    return;
                }

                // Create the session.
                session = new Session(/* context= */ this);
            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(this, message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

        try {
            configureSession();
            session.resume();
        } catch (CameraNotAvailableException e) {
            message = "Camera not available. Try restarting the app.";
            exception = e;
        } catch (GooglePlayServicesLocationLibraryNotLinkedException e) {
            message = "Google Play Services location library not linked or obfuscated with Proguard.";
            exception = e;
        } catch (FineLocationPermissionNotGrantedException e) {
            message = "The Android permission ACCESS_FINE_LOCATION was not granted.";
            exception = e;
        } catch (UnsupportedConfigurationException e) {
            message = "This device does not support GeospatialMode.ENABLED.";
            exception = e;
        } catch (SecurityException e) {
            message = "Camera failure or the internet permission has not been granted.";
            exception = e;
        }

        if (message != null) {
            session = null;
            messageSnackbarHelper.showError(this, message);
            Log.e(TAG, "Exception configuring and resuming the session", exception);
            return;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
        if (LocationPermissionHelper.hasFineLocationPermissionsResponseInResult(permissions)
                && !LocationPermissionHelper.hasFineLocationPermission(this)) {
            Toast.makeText(
                    this,
                    "Precise location permission is needed to run this application",
                    Toast.LENGTH_LONG)
                    .show();
            if (!LocationPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                LocationPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    @Override
    public void onSurfaceCreated(SampleRender render) {
        // Prepare the rendering objects. This involves reading shaders and 3D model files, so may throw
        // an IOException.
        try {
            backgroundRenderer = new BackgroundRenderer(render);
            virtualSceneFramebuffer = new Framebuffer(render, /*width=*/ 1, /*height=*/ 1);
            // 좋아요 객체 관련 설정
            Texture likesObjectTexture =
                    Texture.createFromAsset(
                            render,
                            "models/heart_object_baked_just_color.png",
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            Texture.ColorFormat.SRGB);

            likesObjectMesh = Mesh.createFromAsset(render, "models/heart_object_obj.obj");
            likesObjectShader =
                    Shader.createFromAssets(
                            render,
                            "shaders/ar_unlit_object.vert",
                            "shaders/ar_unlit_object.frag",
                            /*defines=*/ null)
                            .setTexture("u_Texture", likesObjectTexture);

            backgroundRenderer.setUseDepthVisualization(render, false);
            backgroundRenderer.setUseOcclusion(render, false);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read a required asset file", e);
            messageSnackbarHelper.showError(this, "Failed to read a required asset file: " + e);
        }
    }

    @Override
    public void onSurfaceChanged(SampleRender render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        virtualSceneFramebuffer.resize(width, height);
    }

    @Override
    public void onDrawFrame(SampleRender render) {
        if (session == null) {
            return;
        }
        if (!hasSetTextureNames) {
            session.setCameraTextureNames(
                    new int[]{backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }
        displayRotationHelper.updateSessionIfNeeded(session);
        Frame frame;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            Log.e(TAG, "Camera not available during onDrawFrame", e);
            messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            return;
        }

        Camera camera = frame.getCamera();

        backgroundRenderer.updateDisplayGeometry(frame);
        trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

        Earth earth = session.getEarth();
        if (earth != null) {
            updateGeospatialState(earth);
        }


        String message = null;
        switch (state) {
            case UNINITIALIZED:
                break;
            case UNSUPPORTED:
                message = getResources().getString(R.string.status_unsupported);
                break;
            case PRETRACKING:
                message = getResources().getString(R.string.status_pretracking);
                break;
            case EARTH_STATE_ERROR:
                message = getResources().getString(R.string.status_earth_state_error);
                break;
            case LOCALIZING:
                message = getResources().getString(R.string.status_localize_hint);
                break;
            case LOCALIZING_FAILED:
                message = getResources().getString(R.string.status_localize_timeout);
                break;
            case LOCALIZED:
                if (anchors.size() > 0) {
                    message =
                            getResources()
                                    .getQuantityString(R.plurals.status_anchors_set, anchors.size(), anchors.size());

                } else if (clearedAnchorsAmount != null) {
                    message =
                            getResources()
                                    .getQuantityString(
                                            R.plurals.status_anchors_cleared, clearedAnchorsAmount, clearedAnchorsAmount);
                } else {
                    message = getResources().getString(R.string.status_localize_complete);
                }
                break;
        }
        if (message == null) {
            lastStatusText = null;
            runOnUiThread(() -> statusTextView.setVisibility(View.INVISIBLE));
        } else if (lastStatusText != message) {
            lastStatusText = message;
            runOnUiThread(
                    () -> {
                        statusTextView.setVisibility(View.VISIBLE);
                        statusTextView.setText(lastStatusText);
                    });
        }

        if (frame.getTimestamp() != 0) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            backgroundRenderer.drawBackground(render);
        }


        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);

        camera.getViewMatrix(viewMatrix, 0);

        render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f);

        if (CONCURRENT_PREVENT_FLAG) {
            int counter = 0;
            for (Iterator<Anchor> iterator = anchors.iterator(); iterator.hasNext(); ) {
                iterator.next().getPose().toMatrix(modelMatrix, 0);
                    Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                    Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
                    likesObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
                    render.draw(likesObjectMesh, likesObjectShader, virtualSceneFramebuffer);
            }
        }
        backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR);
    }


    private void configureSession() {
        if (!session.isGeospatialModeSupported(Config.GeospatialMode.ENABLED)) {
            state = State.UNSUPPORTED;
            return;
        }

        Config config = session.getConfig();
        config.setGeospatialMode(Config.GeospatialMode.ENABLED);
        session.configure(config);
        state = State.PRETRACKING;
        localizingStartTimestamp = System.currentTimeMillis();
    }

    private void updateGeospatialState(Earth earth) {
        if (state == State.PRETRACKING) {
            updatePretrackingState(earth);
        } else if (state == State.LOCALIZING) {
            updateLocalizingState(earth);
        } else if (state == State.LOCALIZED) {
            updateLocalizedState(earth);
        }
    }

    private void updatePretrackingState(Earth earth) {
        if (earth.getTrackingState() == TrackingState.TRACKING) {
            state = State.LOCALIZING;
            return;
        }

        if (earth.getEarthState() != Earth.EarthState.ENABLED) {
            state = State.EARTH_STATE_ERROR;
            return;
        }
    }

    private void updateLocalizingState(Earth earth) {
        GeospatialPose geospatialPose = earth.getCameraGeospatialPose();
        if (geospatialPose.getHorizontalAccuracy() <= LOCALIZING_HORIZONTAL_ACCURACY_THRESHOLD_METERS
                && geospatialPose.getHeadingAccuracy() <= LOCALIZING_HEADING_ACCURACY_THRESHOLD_DEGREES) {
            state = State.LOCALIZED;
            if (anchors.isEmpty()) {
                createAnchorFromSharedPreferences(earth);
            }
            runOnUiThread(
                    () -> {
                        setAnchorButton.setVisibility(View.VISIBLE);
                    });
            return;
        }

        if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - localizingStartTimestamp)
                > LOCALIZING_TIMEOUT_SECONDS) {
            state = State.LOCALIZING_FAILED;
            return;
        }

    }

    private void updateLocalizedState(Earth earth) {
        GeospatialPose geospatialPose = earth.getCameraGeospatialPose();
        if (geospatialPose.getHorizontalAccuracy()
                > LOCALIZING_HORIZONTAL_ACCURACY_THRESHOLD_METERS
                + LOCALIZED_HORIZONTAL_ACCURACY_HYSTERESIS_METERS
                || geospatialPose.getHeadingAccuracy()
                > LOCALIZING_HEADING_ACCURACY_THRESHOLD_DEGREES
                + LOCALIZED_HEADING_ACCURACY_HYSTERESIS_DEGREES) {
            state = State.LOCALIZING;
            localizingStartTimestamp = System.currentTimeMillis();
            runOnUiThread(
                    () -> {
                        setAnchorButton.setVisibility(View.INVISIBLE);
                        clearAnchorsButton.setVisibility(View.INVISIBLE);
                    });
            return;
        }

    }


    private void handleSetAnchorButton() {
        Earth earth = session.getEarth();
        if (earth == null || earth.getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        CollectionReference photoZoneLikesRef = db.collection("photoZone").document(photoZoneName)
                .collection("userLikes");

        photoZoneLikesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            Double latitude;
            Double longitude;
            Double altitude;
            Double headingDegrees;
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (QueryDocumentSnapshot results : task.getResult()) {
                        latitude = (Double) results.get("latitude");
                        longitude = (Double) results.get("longitude");
//                        altitude = (Double) results.get("altitude");
                        headingDegrees = (Double) results.get("headingDegrees");
                        createAnchor(earth, latitude, longitude, 55, headingDegrees);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e );
            }
        });

    }

    private void handleSetLikesButton() {

        // 좋아요 객체는 단 하나만 추가됨
        // 화면에서는 여러개가 뜨지만 다시 접속할 경우 마지막으로 생성한 단 하나의 좋아요만 표시됨

        Earth earth = session.getEarth();
        if (earth == null || earth.getTrackingState() != TrackingState.TRACKING) {
            return;
        }
        GeospatialPose geospatialPose = earth.getCameraGeospatialPose();
        double latitude = geospatialPose.getLatitude();
        double longitude = geospatialPose.getLongitude();
        double altitude = geospatialPose.getAltitude();
        double headingDegrees = geospatialPose.getHeading();

        DocumentReference photoZoneLikesRef = db.collection("photoZone").document(photoZoneName)
                .collection("userLikes").document(auth.getCurrentUser().getUid());

        DocumentReference userPhotoZoneLikesRef = db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("photoZoneLikes").document(auth.getCurrentUser().getUid());


        HashMap<String, Double> DBInput = new HashMap<>();
        DBInput.put("latitude", latitude);
        DBInput.put("longitude", longitude);
        DBInput.put("altitude", altitude);
        DBInput.put("headingDegrees", headingDegrees);
        photoZoneLikesRef.set(DBInput).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    createAnchor(earth, latitude, longitude, altitude, headingDegrees);
                    storeAnchorParameters(latitude, longitude, altitude, headingDegrees);
                    runOnUiThread(() -> clearAnchorsButton.setVisibility(View.VISIBLE));
                    if (clearedAnchorsAmount != null) {
                        clearedAnchorsAmount = null;
                    }
                }
                HashMap<String, String> map = new HashMap<>();
                map.put("photoZoneName", photoZoneName);
                // TODO 리스너 처리 미정
                userPhotoZoneLikesRef.set(map);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e);
            }
        });

    }


    private void handleClearAnchorsButton() {
        clearedAnchorsAmount = anchors.size();
        anchors.clear();
        clearAnchorsFromSharedPreferences();
        clearAnchorsButton.setVisibility(View.INVISIBLE);
    }

    private void createAnchor(
            Earth earth, double latitude, double longitude, double altitude, double headingDegrees) {
        double angleRadians = Math.toRadians(180.0f - headingDegrees);

        Anchor anchor =
                earth.createAnchor(
                        latitude,
                        longitude,
                        altitude,
                        45.0f,
//                        (float) Math.sin(angleRadians),
                        -120.0f,
                        45.0f,
                        (float) Math.cos(angleRadians / 2));
        anchors.add(anchor);


        if (anchors.size() > MAXIMUM_ANCHORS) {
            anchors.remove(0);
        }
    }

    private void storeAnchorParameters(
            double latitude, double longitude, double altitude, double headingDegrees) {
        Set<String> anchorParameterSet =
                sharedPreferences.getStringSet(SHARED_PREFERENCES_SAVED_ANCHORS, new HashSet<>());
        HashSet<String> newAnchorParameterSet = new HashSet<>(anchorParameterSet);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        newAnchorParameterSet.add(
                String.format("%.6f,%.6f,%.6f,%.6f", latitude, longitude, altitude, headingDegrees));
        editor.putStringSet(SHARED_PREFERENCES_SAVED_ANCHORS, newAnchorParameterSet);
        editor.commit();
    }


    private void clearAnchorsFromSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(SHARED_PREFERENCES_SAVED_ANCHORS, null);
        editor.commit();
    }

    private void createAnchorFromSharedPreferences(Earth earth) {
        Set<String> anchorParameterSet =
                sharedPreferences.getStringSet(SHARED_PREFERENCES_SAVED_ANCHORS, null);
        if (anchorParameterSet == null) {
            return;
        }

        for (String anchorParameters : anchorParameterSet) {
            String[] parameters = anchorParameters.split(",");
            if (parameters.length != 4) {
                Log.d(
                        TAG, "Invalid number of anchor parameters. Expected four, found " + parameters.length);
                return;
            }
            double latitude = Double.parseDouble(parameters[0]);
            double longitude = Double.parseDouble(parameters[1]);
            double altitude = Double.parseDouble(parameters[2]);
            double heading = Double.parseDouble(parameters[3]);
            createAnchor(earth, latitude, longitude, altitude, heading);
        }

        runOnUiThread(() -> clearAnchorsButton.setVisibility(View.VISIBLE));
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (!sharedPreferences.edit().putBoolean(ALLOW_GEOSPATIAL_ACCESS_KEY, true).commit()) {
            throw new AssertionError("Could not save the user preference to SharedPreferences!");
        }
        createSession();
    }
}
