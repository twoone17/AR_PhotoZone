package com.google.ar.core.examples.java.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.ar.core.examples.java.app.board.BoardData;
import com.google.ar.core.examples.java.geospatial.GeospatialActivity;
import com.google.ar.core.examples.java.geospatial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    private final String TAG = "CameraActivity";
    private CameraSurfaceView surfaceView;
    private MediaScanner ms = MediaScanner.newInstance(CameraActivity.this);

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    Intent intent, intentBitmap;
    BoardData boardData;
    String getImgURL;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_camera);

        GeospatialActivity context;
        intent = getIntent();
        boardData = (BoardData) intent.getSerializableExtra("boardData");
        getImgURL = (String) intent.getSerializableExtra("imgURL");

        this.control();
        this.surfaceView = (CameraSurfaceView) this.findViewById(R.id.surfaceview);

        Button button = (Button) this.findViewById(R.id.capture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                capture();
            }
        });
    }

    public final void control() {

        ImageView load = (ImageView) this.findViewById(R.id.profile);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (boardData != null){
            if (user != null) {
                Glide.with(this).load(getImgURL).into(load);
            }
        }
    }

    public void capture() {
        surfaceView.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };

    private Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };

    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            //이미지의 너비와 높이 결정
            int w = camera.getParameters().getPictureSize().width;
            int h = camera.getParameters().getPictureSize().height;

            //byte array 를 bitmap 으로 변환
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

            //이미지를 디바이스 방향으로 회전
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

            //bitmap 을  byte array 로 변환
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] currentData = stream.toByteArray();

            //파일로 저장
            new saveImg().execute(currentData);

            intentBitmap = new Intent();
            intentBitmap.putExtra("bitmap",bitmap);
            ((GeospatialActivity)GeospatialActivity.mContext).savePhoto();

            camera.startPreview();
        }
    };

    private class saveImg extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            try {
                String path_name = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + "Pic-AR";
                File path = new File(path_name);
                if (!path.exists()) {
                    path.mkdirs();
                }

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outputFile = new File(path, fileName);

                outStream = new FileOutputStream(outputFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to "
                        + outputFile.getAbsolutePath());

                try {
                    // TODO : 미디어 스캔
                    ms.mediaScanning(outputFile.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("MediaScan", "ERROR" + e);
                } finally {

                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

    }



}