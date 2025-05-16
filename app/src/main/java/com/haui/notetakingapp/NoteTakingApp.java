package com.haui.notetakingapp;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.haui.notetakingapp.data.local.NoteDatabase;
import com.haui.notetakingapp.data.local.dao.NoteDao;
import com.haui.notetakingapp.data.remote.cloudinary.CloudinaryManager;
import com.haui.notetakingapp.data.remote.firebase.SyncManager;

public class NoteTakingApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CloudinaryManager.getInstance().initialize(this);
        FirebaseAuth.getInstance().addAuthStateListener(auth -> {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                NoteDao noteDao = NoteDatabase.getInstance(getApplicationContext()).noteDao();
                SyncManager.getInstance().initialize(getApplicationContext(), noteDao);
            }
        });

    }
}
