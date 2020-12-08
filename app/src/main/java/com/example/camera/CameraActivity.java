package com.example.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.camera.camera.ICamera;
import com.example.camera.camera.MyCamera;
import com.example.camera.databinding.ActivityCameraBinding;
import com.example.camera.utils.UploadService;

import java.io.File;

/**
 * Author       : Arvindo Mondal
 * Created date : 07-12-2020
 */
public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";
    private ActivityCameraBinding binding;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);

        setupBroadcast();
//        setup();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setup();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyCamera.getInstance().releaseCamera();
    }

    @SuppressLint("NewApi")
    private void setup() {
        if(!checkPermissionWrite()){
            return;
        }
        if(!checkPermission()){
            return;
        }
        MyCamera.getInstance().setup(this, new ICamera() {
            @Override
            public void onCameraSetup(Camera camera) {
                mPreview = new CameraPreview(CameraActivity.this, camera);
                binding.cameraPreview.addView(mPreview);

                binding.buttonCapture.setOnClickListener(v -> {
                    MyCamera.getInstance().takeImage();
                    binding.buttonCapture.setEnabled(false);
                    binding.progress.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void cameraNotSupport() {
                message("Camera not support");
            }

            @Override
            public void onCameraFile(File file) {
                if(file.exists()){
                    Log.e("CameraActivity", "File found");
                    UploadService.startUpload(CameraActivity.this, file);
                }
            }
        });
    }

    private void message(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    //---------

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermission() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MyCamera.MY_CAMERA_PERMISSION_CODE);
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermissionWrite() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    12222);
            return false;
        }
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MyCamera.MY_CAMERA_PERMISSION_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                setup();
            }
            else{
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    //-----------------------

    private void setupBroadcast() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(UploadService.FileUpdates));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            boolean status = intent.getBooleanExtra("Status", false);

            binding.progress.setVisibility(View.GONE);
            if(status){
                message("File upload successfully");
            }
            else{
                message("File upload error,\n try again");
            }
            finish();
        }
    };

}
