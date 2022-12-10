/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.core.examples.java.geospatial;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.ar.core.examples.java.app.board.BoardData;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class GeospatialActivity extends AppCompatActivity
        implements SampleRender.Renderer, NoticeDialogListener {

    private static final String TAG = GeospatialActivity.class.getSimpleName();

    private static final String SHARED_PREFERENCES_SAVED_ANCHORS = "SHARED_PREFERENCES_SAVED_ANCHORS";
    private static final String ALLOW_GEOSPATIAL_ACCESS_KEY = "ALLOW_GEOSPATIAL_ACCESS";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000f;

    // The thresholds that are required for horizontal and heading accuracies before entering into the
    // LOCALIZED state. Once the accuracies are equal or less than these values, the app will
    // allow the user to place anchors.
    private static final double LOCALIZING_HORIZONTAL_ACCURACY_THRESHOLD_METERS = 10;
    private static final double LOCALIZING_HEADING_ACCURACY_THRESHOLD_DEGREES = 15;
    private DatabaseReference mDatabase;

    // Once in the LOCALIZED state, if either accuracies degrade beyond these amounts, the app will
    // revert back to the LOCALIZING state.
    private static final double LOCALIZED_HORIZONTAL_ACCURACY_HYSTERESIS_METERS = 10;
    private static final double LOCALIZED_HEADING_ACCURACY_HYSTERESIS_DEGREES = 10;

    private static final int LOCALIZING_TIMEOUT_SECONDS = 180;
    private static final int MAXIMUM_ANCHORS = 10;
    private boolean CONCURRENT_PREVENT_FLAG = false;
    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;

    private boolean installRequested;
    private Integer clearedAnchorsAmount = null;

    /**
     * Timer to keep track of how much time has passed since localizing has started.
     */
    private long localizingStartTimestamp;

    enum State {
        /**
         * The Geospatial API has not yet been initialized.
         */
        UNINITIALIZED,
        /**
         * The Geospatial API is not supported.
         */
        UNSUPPORTED,
        /**
         * The Geospatial API has encountered an unrecoverable error.
         */
        EARTH_STATE_ERROR,
        /**
         * The Session has started, but {@link Earth} isn't {@link TrackingState.TRACKING} yet.
         */
        PRETRACKING,
        /**
         * {@link Earth} is {@link TrackingState.TRACKING}, but the desired positioning confidence
         * hasn't been reached yet.
         */
        LOCALIZING,
        /**
         * The desired positioning confidence wasn't reached in time.
         */
        LOCALIZING_FAILED,
        /**
         * {@link Earth} is {@link TrackingState.TRACKING} and the desired positioning confidence has
         * been reached.
         */
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
    private TextView geospatialPoseTextView;
    private TextView statusTextView;
    private Button setAnchorButton;
    private Button clearAnchorsButton;

    //추가
    private String imgURL;
    private StorageTask uploadTask;
    StorageReference storageRef;


    private int timeOutCount = 5;
    private StoredGeolocation storedGeolocation_Photo;
    private Button setLocationButton;
    private TextView stroedLocationTextView;
    private Button cameraGeospatial;
    private double location;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    final FirebaseAuth auth = FirebaseAuth.getInstance();

    private File file;

    private BackgroundRenderer backgroundRenderer;
    private Framebuffer virtualSceneFramebuffer;
    private boolean hasSetTextureNames = false;

    // Virtual object (ARCore geospatial)
    private Mesh virtualObjectMesh;
    private Shader virtualObjectShader;
    private BoardData boradData;
    private List<Anchor> anchors = new ArrayList<>();
    private String anchorID;
    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16]; // view x model
    private final float[] modelViewProjectionMatrix = new float[16]; // projection x view x model

    private String documentID;
    private boolean anchorBoolean = false;
    boolean avoidLoopAnchor = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        //게시글에 있는 카메라를 클릭시 boardData 전달
        Intent intent = getIntent();
        BoardData boardData = (BoardData) intent.getSerializableExtra("boardData");
        documentID = boardData.getDocumentId();
        anchorID = boardData.getAnchorID();

        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceview);
        geospatialPoseTextView = findViewById(R.id.geospatial_pose_view);
        statusTextView = findViewById(R.id.status_text_view);
        setAnchorButton = findViewById(R.id.set_anchor_button);
        clearAnchorsButton = findViewById(R.id.clear_anchors_button);
        setLocationButton = findViewById(R.id.set_location);

//        //임시로 textview 안보이게 함 (for UI)
//        geospatialPoseTextView.setVisibility(View.INVISIBLE);
//        statusTextView.setVisibility(View.INVISIBLE);

        setAnchorButton.setOnClickListener(view -> handleSetAnchorButton());
        clearAnchorsButton.setOnClickListener(view -> handleClearAnchorsButton());

        displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

        // Set up renderer.
        render = new SampleRender(surfaceView, this, getAssets());

        installRequested = false;
        clearedAnchorsAmount = null;

        System.out.println("boardData = " + boardData);

//        auth.signInWithEmailAndPassword("oldstyle4@naver.com", "2580as2580@");


    }

    @Override
    protected void onDestroy() {
        if (session != null) {
            // Explicitly close ARCore Session to release native resources.
            // Review the API reference for important considerations before calling close() in apps with
            // more complicated lifecycle requirements:
            // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
            session.close();
            session = null;
        }

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

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
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

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            configureSession();
            // To record a live camera session for later playback, call
            // `session.startRecording(recordingConfig)` at anytime. To playback a previously recorded AR
            // session instead of using the live camera feed, call
            // `session.setPlaybackDatasetUri(Uri)` before calling `session.resume()`. To
            // learn more about recording and playback, see:
            // https://developers.google.com/ar/develop/java/recording-and-playback
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
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            // Use toast instead of snackbar here since the activity will exit.
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
        // Check if this result pertains to the location permission.
        if (LocationPermissionHelper.hasFineLocationPermissionsResponseInResult(permissions)
                && !LocationPermissionHelper.hasFineLocationPermission(this)) {
            // Use toast instead of snackbar here since the activity will exit.
            Toast.makeText(
                            this,
                            "Precise location permission is needed to run this application",
                            Toast.LENGTH_LONG)
                    .show();
            if (!LocationPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
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

            // Virtual object to render (ARCore geospatial)
            Texture virtualObjectTexture =
                    Texture.createFromAsset(
                            render,
                            "models/heart_object_baked_just_color.png",
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            Texture.ColorFormat.LINEAR);

            virtualObjectMesh = Mesh.createFromAsset(render, "models/heart_object.obj");
            virtualObjectShader =
                    Shader.createFromAssets(
                                    render,
                                    "shaders/ar_unlit_object.vert",
                                    "shaders/ar_unlit_object.frag",
                                    /*defines=*/ null)
                            .setTexture("u_Texture", virtualObjectTexture);

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

    //루프함수
    @Override
    public void onDrawFrame(SampleRender render) {
        if (session == null) {
            return;
        }

        // Texture names should only be set once on a GL thread unless they change. This is done during
        // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
        // initialized during the execution of onSurfaceCreated.
        if (!hasSetTextureNames) {
            session.setCameraTextureNames(
                    new int[]{backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }

        // -- Update per-frame state

        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        // Obtain the current frame from ARSession. When the configuration is set to
        // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
        // camera framerate.
        Frame frame;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            Log.e(TAG, "Camera not available during onDrawFrame", e);
            messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            return;
        }

        Camera camera = frame.getCamera();

        // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
        // used to draw the background camera image.
        backgroundRenderer.updateDisplayGeometry(frame);

        // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
        trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

        Earth earth = session.getEarth();
        if (earth != null) {
            updateGeospatialState(earth);
        }
        setLocationButton = findViewById(R.id.set_location);
/*        stroedLocationTextView = findViewById(R.id.stored_location);*/
        cameraGeospatial = findViewById(R.id.camera_geospatial);

        GeospatialPose geospatialPose = earth.getCameraGeospatialPose();


        String getUid = "2BXzuCaFIYXf7Dp06sHMCrTNSH43";

        GeospatialPose geospatialPose2 = earth.getCameraGeospatialPose();

        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                storedGeolocation = new StoredGeolocation(geospatialPose2.getLatitude(),
//                        geospatialPose2.getLongitude(),
//                        geospatialPose2.getHorizontalAccuracy(),
//                        geospatialPose2.getAltitude(),
//                        geospatialPose2.getVerticalAccuracy(),
//                        geospatialPose2.getHeading(),
//                        geospatialPose2.getHeadingAccuracy());

//                stroedLocationTextView.setText("저장되었습니다 ! ");

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                //boardData의 앵커 아이디를 받아와서 해당 앵커만 anchors에 저장한다
                DocumentReference docRef = db.collection("anchor").document(anchorID);

                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, Object> data = document.getData();

//                        Anchor anchor =
//                                earth.createAnchor(
//                                        (Double) data.get("latitude"),
//                                        (Double) data.get("longitude"),
//                                        (Double) data.get("altitude"),
//                                        0.0f,
//                                        (float) Math.sin(20 / 2),
//                                        0.0f,
//                                        (float) Math.cos(20 / 2));
//
//
//                        Anchor anchor2 =
//                                earth.createAnchor(
//                                        (Double) data.get("latitude"),
//                                        (Double) data.get("longitude"),
//                                        (Double) data.get("altitude") + 1,
//                                        0.0f,
//                                        (float) Math.sin(20 / 2),
//                                        0.0f,
//                                        (float) Math.cos(20 / 2));
//
//
//
//                        Anchor anchor3 =
//                                earth.createAnchor(
//                                        (Double) data.get("latitude"),
//                                        (Double) data.get("longitude"),
//                                        (Double) data.get("altitude") -1,
//                                        0.0f,
//                                        (float) Math.sin(20 / 2),
//                                        0.0f,
//                                        (float) Math.cos(20 / 2));

                        for(int i = 0 ; i< 50 ; i++)
                        {
                            Anchor anchor =
                                    earth.createAnchor(
                                            (Double) data.get("latitude"),
                                            (Double) data.get("longitude"),
                                            (Double) data.get("altitude") -1 +i*0.5,
                                            0.0f,
                                            (float) Math.sin(20 / 2),
                                            0.0f,
                                            (float) Math.cos(20 / 2));

                            anchors.add(anchor);
                        }

//                        anchors.add(anchor);
//                        anchors.add(anchor2);
//                        anchors.add(anchor3);
                    anchorBoolean = true;

                        Log.e(TAG, "onDrawFrame: anchor 0" + anchors);
                        Log.e(TAG, "onDrawFrame: anchor to string0 " + anchors.toString());
                        Log.e(TAG, "onDrawFrame: anchorBoolean 1 "+anchorBoolean );
                        Log.d(TAG, "onDrawFrame:  (Double) data.get(\"latitude\")," +  (Double) data.get("latitude"));
                        Log.d(TAG, "onDrawFrame:  (Double) data.get(\"latitude\")," +  (Double) data.get("longitude"));
                        Log.d(TAG, "onDrawFrame:  (Double) data.get(\"latitude\")," +  (Double) data.get("altitude"));
                    }


                });

                anchorBoolean = true;
                handleSetAnchorButton();

                //기존 저장한 앵커를 파이어베이스에서 불러온다

//                db.collectionGroup("anchor").get().
//                        addOnCompleteListener(task -> {
//                            if(task.isSuccessful()) {
//
//                                for(QueryDocumentSnapshot document : task.getResult()) {
//                                    AnchorFirebase anchorFirebase = document.toObject(AnchorFirebase.class);
//                                    Anchor anchor =
//                                            earth.createAnchor(
//                                                    anchorFirebase.getLatitude(),
//                                                    anchorFirebase.getLongitude(),
//                                                    anchorFirebase.getAltitude(),
//                                                    0.0f,
//                                                    (float) Math.sin(anchorFirebase.getAngleRadians()/ 2),
//                                                    0.0f,
//                                                    (float) Math.cos(anchorFirebase.getAngleRadians() / 2));
//                                    anchors.add(anchor);
//                                }
//
//                            }
//
//
//                        });



            }
        });

        startCameraGeospatial();

        // Show a message based on whether tracking has failed, if planes are detected, and if the user
        // has placed any objects.
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

        // -- Draw background
        /**`
         * 보여지는 것
         */

        if (frame.getTimestamp() != 0) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            backgroundRenderer.drawBackground(render);
        }


        // -- Draw virtual objects

        // Get projection matrix.
        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);

        // Get camera matrix and draw.
        camera.getViewMatrix(viewMatrix, 0);

        // Visualize anchors created by touch.
        render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f);

        Iterator<Anchor> iterator = anchors.iterator();
        if (anchorBoolean) {
            for (Anchor anchor : anchors) {
                // Get the current pose of an Anchor in world space. The Anchor pose is updated
                // during calls to session.update() as ARCore refines its estimate of the world.
                anchor.getPose().toMatrix(modelMatrix, 0);

                // Calculate model/view/projection matrices
                Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
                Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

                // Update shader properties and draw
                virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);

                render.draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer);
//                avoidLoopAnchor = false;
            }

            // Compose the virtual scene with the background.
            backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR);
        }
    }
    //camera가 나옴
    private void startCameraGeospatial() {

        cameraGeospatial = findViewById(R.id.camera_geospatial);
        Earth earth = session.getEarth();
        if (earth == null || earth.getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        GeospatialPose geospatialPose = earth.getCameraGeospatialPose();
        double latitude = geospatialPose.getLatitude();
        double longitude = geospatialPose.getLongitude();
        double altitude = geospatialPose.getAltitude();
        double headingDegrees = geospatialPose.getHeading();

        storedGeolocation_Photo = new StoredGeolocation(latitude, longitude, altitude, headingDegrees);

        cameraGeospatial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e(TAG, "onClick: storedGeolocation_Photo" + storedGeolocation_Photo.toString());

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 101);
                //TODO: 현재 카메라까지만 구현, 투명도 높은 사진을 storage에서 가져와서 띄워야함
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Match the request 'pic id with requestCode

        if (requestCode == 101) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "onActivityResult: 권한접근");
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                    Log.e(TAG, "onActivityResult: shouldShowRequestPermissionRationale");
                }

                requestPermissions(
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        1000);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant that should be quite unique
                Log.e(TAG, "onActivityResult: return");
                return;
            }
            Log.e(TAG, "onActivityResult: 권한성공");
            // BitMap is data structure of image file which store the image in memory
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // Set the image in imageview for display
            System.out.println("photo = " + photo);

            Uri imgURL = getImageUri(this, photo);
//            imgURL = uri.toString();
            Log.e(TAG, "onActivityResult: imgURL" + imgURL);
            Log.e(TAG, "onActivityResult: storedGeolocation_Photo" + storedGeolocation_Photo);
            //TODO: 현재 bitmap 상태로 저장, firebase에 boardData, 위치정보와 함께 담아야함
            Log.e(TAG, "onActivityResult:  firebaseAuth.getCurrentUser()" + auth.getCurrentUser());
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            //storage에 파일 저장하는 코드
            /**
             * 파이어베이스에 저장할 데이터
             * 위치 : users - uid - posts - postID - {document}
             *
             * -anchorFirebase : latitude, longitude, altitude, angleRadians
             * -imgURL : 촬영한 사진 이미지
             * -userID
             * -활용한 게시글
             */
            if (imgURL != null) {
//                String GetUid = firebaseUser.getUid();
                //TODO: 로그인구현 이후 수정
                String getUid = auth.getCurrentUser().getUid();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("Gallery/" + getUid); //storgae의 저장경로
                final StorageReference ref = storageRef.child(System.currentTimeMillis() + ".jpg"); //이미지의 파일이름
                uploadTask = ref.putFile(imgURL); //storage에 file을 업로드, uri를 통해서
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) { //task가 성공하면
                            Uri downloadUri = task.getResult(); //위의 return값을 받아 downloadUri에 저장
                            String DownloadUrl = downloadUri.toString();
                            LocalDateTime now = LocalDateTime.now();
                            String postdocument_bydate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH));

                            UploadFirebaseData uploadFirebaseData = new UploadFirebaseData(getUid, DownloadUrl, storedGeolocation_Photo.getLatitude(), storedGeolocation_Photo.getLongitude(), storedGeolocation_Photo.getAltitude(), storedGeolocation_Photo.getHeading(), postdocument_bydate);

                            db.collection("anchor").document(postdocument_bydate).set(uploadFirebaseData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.e("temp", "onSuccess: DB Insertion success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("temp", "onFailure: DB Insertion failed");
                                        }
                                    });

                            db.collection("users").document(getUid).collection("posts").document(postdocument_bydate).set(uploadFirebaseData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.e("temp", "onSuccess: DB Insertion success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("temp", "onFailure: DB Insertion failed");
                                        }
                                    });
                            finish();

                        } else {
                            Toast.makeText(GeospatialActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }


        }
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * Configures the session with feature settings.
     */
    private void configureSession() {
        // Earth mode may not be supported on this device due to insufficient sensor quality.
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

    /**
     * Change behavior depending on the current {@link State} of the application.
     */
    private void updateGeospatialState(Earth earth) {
        if (state == State.PRETRACKING) {
            updatePretrackingState(earth);
        } else if (state == State.LOCALIZING) {
            updateLocalizingState(earth);
        } else if (state == State.LOCALIZED) {
            updateLocalizedState(earth);
        }
    }

    /**
     * Handles the updating for {@link State.PRETRACKING}. In this state, wait for {@link Earth} to
     * have {@link TrackingState.TRACKING}. If it hasn't been enabled by now, then we've encountered
     * an unrecoverable {@link State.EARTH_STATE_ERROR}.
     */
    private void updatePretrackingState(Earth earth) {
        if (earth.getTrackingState() == TrackingState.TRACKING) {
            state = State.LOCALIZING;
            return;
        }

        if (earth.getEarthState() != Earth.EarthState.ENABLED) {
            state = State.EARTH_STATE_ERROR;
            return;
        }

        runOnUiThread(() -> geospatialPoseTextView.setText(R.string.geospatial_pose_not_tracking));
    }

    /**
     * Handles the updating for {@link State.LOCALIZING}. In this state, wait for the horizontal and
     * heading threshold to improve until it reaches your threshold.
     *
     * <p>If it takes too long for the threshold to be reached, this could mean that GPS data isn't
     * accurate enough, or that the user is in an area that can't be localized with StreetView.
     */
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

        updateGeospatialPoseText(geospatialPose);
    }
//

    /**
     * Handles the updating for {@link State.LOCALIZED}. In this state, check the accuracy for
     * degradation and return to {@link State.LOCALIZING} if the position accuracies have dropped too
     * low.
     */
    private void updateLocalizedState(Earth earth) {
        GeospatialPose geospatialPose = earth.getCameraGeospatialPose();
        // Check if either accuracy has degraded to the point we should enter back into the LOCALIZING
        // state.
        if (geospatialPose.getHorizontalAccuracy()
                > LOCALIZING_HORIZONTAL_ACCURACY_THRESHOLD_METERS
                + LOCALIZED_HORIZONTAL_ACCURACY_HYSTERESIS_METERS
                || geospatialPose.getHeadingAccuracy()
                > LOCALIZING_HEADING_ACCURACY_THRESHOLD_DEGREES
                + LOCALIZED_HEADING_ACCURACY_HYSTERESIS_DEGREES) {
            // Accuracies have degenerated, return to the localizing state.
            state = State.LOCALIZING;
            localizingStartTimestamp = System.currentTimeMillis();
            runOnUiThread(
                    () -> {
                        setAnchorButton.setVisibility(View.INVISIBLE);
                        clearAnchorsButton.setVisibility(View.INVISIBLE);
                    });
            return;
        }

        updateGeospatialPoseText(geospatialPose);
    }

    private void updateGeospatialPoseText(GeospatialPose geospatialPose) {
        String poseText =
                getResources()
                        .getString(
                                R.string.geospatial_pose,
                                geospatialPose.getLatitude(),
                                geospatialPose.getLongitude(),
                                geospatialPose.getHorizontalAccuracy(),
                                geospatialPose.getAltitude(),
                                geospatialPose.getVerticalAccuracy(),
                                geospatialPose.getHeading(),
                                geospatialPose.getHeadingAccuracy());
        runOnUiThread(
                () -> {
                    geospatialPoseTextView.setText(poseText);
                });
    }

    /**
     * Handles the button that creates an anchor.
     *
     * <p>Ensure Earth is in the proper state, then create the anchor. Persist the parameters used to
     * create the anchors so that the anchors will be loaded next time the app is launched.
     */
    private void handleSetAnchorButton() {
        Earth earth = session.getEarth();
        if (earth == null || earth.getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        GeospatialPose geospatialPose = earth.getCameraGeospatialPose();
        double latitude = geospatialPose.getLatitude();
        double longitude = geospatialPose.getLongitude();
        double altitude = geospatialPose.getAltitude();
        double headingDegrees = geospatialPose.getHeading();
        createAnchor(earth, latitude, longitude, altitude, headingDegrees);
        //TODO: firebase에 저장하는 코드는 이곳에서 parameter로 지리정보를 받아야함
        storeAnchorParameters(latitude, longitude, altitude, headingDegrees);
        runOnUiThread(() -> clearAnchorsButton.setVisibility(View.VISIBLE));
        if (clearedAnchorsAmount != null) {
            clearedAnchorsAmount = null;
        }
        //firebase에 올리기
    }

    private void handleClearAnchorsButton() {
        clearedAnchorsAmount = anchors.size();
        anchors.clear();
        clearAnchorsFromSharedPreferences();
        clearAnchorsButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Create an anchor at a specific geodetic location using a heading.
     */
    private void createAnchor(
            Earth earth, double latitude, double longitude, double altitude, double headingDegrees) {
        // Convert a heading to a EUS quaternion:
        double angleRadians = Math.toRadians(180.0f - headingDegrees);
        LocalDateTime now = LocalDateTime.now();
        String AnchorDate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH));
        //현재 위치의 앵커를 파이어베이스에 저장한다
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        AnchorFirebase anchorFirebase = new AnchorFirebase(latitude, longitude, altitude, angleRadians);
//        db.collection("anchor").document(AnchorDate).set(anchorFirebase);

//        Anchor anchor =
//                earth.createAnchor(
//                        latitude,
//                        longitude,
//                        altitude,
//                        0.0f,
//                        (float) Math.sin(angleRadians / 2),
//                        0.0f,
//                        (float) Math.cos(angleRadians / 2));
//        anchors.add(anchor);

        anchorBoolean = true;


        if (anchors.size() > MAXIMUM_ANCHORS) {
            anchors.remove(0);
        }
    }

    /**
     * Helper function to store the parameters used in anchor creation in {@link SharedPreferences}.
     */
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

    /**
     * Creates all anchors that were stored in the {@link SharedPreferences}.
     */
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
