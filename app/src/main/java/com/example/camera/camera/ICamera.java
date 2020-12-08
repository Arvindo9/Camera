package com.example.camera.camera;


import android.hardware.Camera;

import java.io.File;

/**
 * Author       : Arvindo Mondal
 * Created date : 07-12-2020
 */
public interface ICamera {
    void onCameraSetup(Camera camera);

    void cameraNotSupport();

    void onCameraFile(File file);
}
