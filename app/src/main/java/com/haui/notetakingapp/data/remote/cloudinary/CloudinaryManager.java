package com.haui.notetakingapp.data.remote.cloudinary;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.haui.notetakingapp.BuildConfig;
import com.haui.notetakingapp.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";
    private static CloudinaryManager instance;
    private boolean isInitialized = false;
    private Context appContext;

    private CloudinaryManager() {
        // Private constructor for singleton pattern
    }

    public static synchronized CloudinaryManager getInstance() {
        if (instance == null) {
            instance = new CloudinaryManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (isInitialized) return;

        try {
            this.appContext = context.getApplicationContext();
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
            config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
            config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);
            MediaManager.init(context, config);
            isInitialized = true;
            Log.d(TAG, "Cloudinary initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Cloudinary: " + e.getMessage());
        }
    }

    public String uploadFile(String uriOrPath, String folder) {
        if (!isInitialized) {
            Log.e(TAG, "Cloudinary not initialized. Call initialize() first.");
            return null;
        }

        if (isHttpUrl(uriOrPath)) {
            Log.d(TAG, "File is already a URL, skipping upload: " + uriOrPath);
            return uriOrPath;
        }

        Uri uri;
        try {
            uri = Uri.parse(uriOrPath);
            
            if (uri.getScheme() == null) {
                File file = new File(uriOrPath);
                if (!file.exists()) {
                    Log.e(TAG, "File not found: " + uriOrPath);
                    return null;
                }
                uri = Uri.fromFile(file);
            }
        } catch (Exception e) {
            Log.e(TAG, "Invalid URI or file path: " + uriOrPath, e);
            return null;
        }

        final String[] resultUrl = {null};
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] uploadCompleted = {false};

        String requestId = MediaManager.get().upload(uri)
                .option("folder", folder)
                .option("resource_type", "auto")
                .callback(new UploadCallback() {
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        resultUrl[0] = (String) resultData.get("secure_url");
                        Log.d(TAG, "Upload success: " + resultUrl[0]);
                        uploadCompleted[0] = true;
                        latch.countDown();
                    }

                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started for: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (double) bytes / totalBytes;
                        Log.d(TAG, "Upload progress: " + (int) (progress * 100) + "%");
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Upload error: " + error.getDescription());
                        latch.countDown();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d(TAG, "Upload rescheduled");
                    }
                })
                .dispatch();

        try {
            if (!latch.await(60, TimeUnit.SECONDS)) {
                Log.e(TAG, "Upload timeout");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Upload interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        return uploadCompleted[0] ? resultUrl[0] : null;
    }

    private boolean isHttpUrl(String string) {
        return string != null && (string.startsWith("http://") || string.startsWith("https://"));
    }
}
