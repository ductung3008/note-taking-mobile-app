package com.haui.notetakingapp.data.local;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FileManager {
    private static final String TAG = "FileManager";

    private static final String IMAGE_DIR = "note_images";
    private static final String AUDIO_DIR = "note_audio";
    private static final String DRAWING_DIR = "note_drawings";

    private static final int IMAGE_QUALITY = 50;

    public static Uri createImageFileUri(Context context) {
        File directory = getDirectory(context, IMAGE_DIR);
        String fileName = "note_image_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(directory, fileName);
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", imageFile);
    }

    public static File createAudioFile(Context context) {
        File directory = getDirectory(context, AUDIO_DIR);
        String fileName = "note_audio_" + System.currentTimeMillis() + ".3gp";
        return new File(directory, fileName);
    }

    public static MediaRecorder prepareRecorder(File audioFile) throws IOException {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(audioFile.getAbsolutePath());
        recorder.prepare();
        return recorder;
    }

    public static Uri saveDrawing(Context context, Bitmap bitmap) {
        File directory = getDirectory(context, DRAWING_DIR);
        String fileName = "note_drawing_" + System.currentTimeMillis() + ".png";
        File file = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, fos);
            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } catch (IOException e) {
            Log.e(TAG, "Error saving drawing: " + e.getMessage());
            return null;
        }
    }

    public static String getPathFromUri(Context context, Uri uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return new File(context.getFilesDir(), Objects.requireNonNull(uri.getLastPathSegment())).getAbsolutePath();
    }

    public static boolean deleteFile(Context context, String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return false;
        }

        try {
            Uri uri = Uri.parse(uriString);
            int deleted = context.getContentResolver().delete(uri, null, null);
            return deleted > 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete file: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteFiles(Context context, List<String> uriStrings) {
        boolean allDeleted = true;
        for (String uriString : uriStrings) {
            Log.d(TAG, "deleteFiles: " + uriString);
            if (!deleteFile(context, uriString)) {
                allDeleted = false;
            }
        }
        return allDeleted;
    }

    private static File getDirectory(Context context, String dirName) {
        File directory = new File(context.getFilesDir(), dirName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }
}
