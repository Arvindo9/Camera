package com.example.camera.utils;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Author       : Arvindo Mondal
 * Created date : 08-12-2020
 * Email        : arvindo@aiprog.ai
 * Company      : AIPROG
 * Designation  : Programmer
 * About        : I am a human can only think, I can't be a person like machine which have lots of memory and knowledge.
 * Quote        : No one can measure limit of stupidity but stupid things bring revolutions
 * Strength     : Never give up
 * Motto        : To be known as great Mathematician
 * Skills       : Algorithms and logic
 * Website      : www.aiprog.ai
 */
public class UploadManger {

    public interface Manager{
        void success(UploadTask.TaskSnapshot taskSnapshot);
        void failure(Exception exception);
    }

    public static void uploadFile(final File file, Manager callback){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        String fileName = file.getName();
        Log.e("Upload", fileName);
        // Create a reference to "mountains.jpg"
        StorageReference mountainsRef = storageRef.child(fileName);

        // Create a reference to 'images/mountains.jpg'
//        StorageReference mountainImagesRef = storageRef.child("images/mountains.jpg");
        StorageReference mountainImagesRef = storageRef.child("images/" + fileName);

        // While the file names are the same, the references point to different files
        mountainsRef.getName().equals(mountainImagesRef.getName());    // true
        mountainsRef.getPath().equals(mountainImagesRef.getPath());    // false

        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }


        UploadTask uploadTask = mountainsRef.putStream(stream);
        uploadTask.addOnFailureListener(exception -> {
            Log.e("addOnFailureListener", fileName);
            exception.printStackTrace();
            callback.failure(exception);
            // Handle unsuccessful uploads
        }).addOnSuccessListener(taskSnapshot -> {
            Log.e("onSuccess", taskSnapshot.getMetadata() + "");
            callback.success(taskSnapshot);
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
            // ...
        });
    }


}
