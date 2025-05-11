package com.haui.notetakingapp.data.local;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Utility class for managing files (images, audio, drawings) related to notes
 */
public class FileManager {
    private static final String TAG = "FileManager";

    private static final String IMAGE_DIR = "note_images";
    private static final String AUDIO_DIR = "note_audio";
    private static final String DRAWING_DIR = "note_drawings";

    /**
     * Save bitmap image to app's private storage
     *
     * @param context Application context
     * @param bitmap  Bitmap to save
     * @param noteId  ID of the note this image belongs to
     * @return Path to the saved image file, or null if saving failed
     */
    public static String saveImage(Context context, Bitmap bitmap, String noteId) {
        File directory = getDirectory(context, IMAGE_DIR);
        String fileName = noteId + "_" + UUID.randomUUID().toString() + ".jpg";
        File file = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);

            Glide.with(context).load(file).submit();

            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Save image from Uri to app's private storage using Glide
     *
     * @param context  Application context
     * @param uri      Uri of the image
     * @param noteId   ID of the note this image belongs to
     * @param callback Callback to receive the saved image path
     */
    public static void saveImageFromUri(final Context context, Uri uri, final String noteId, final ImageSaveCallback callback) {
        Glide.with(context).asBitmap().load(uri).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                String path = saveImage(context, bitmap, noteId);
                if (callback != null) {
                    callback.onImageSaved(path);
                }
            }
        });
    }

    /**
     * Load an image using Glide and get a Bitmap (for thumbnails or processing)
     *
     * @param context   Application context
     * @param imagePath Path to the image
     * @return Bitmap of the image, or null if loading failed
     */
    public static Bitmap loadImageAsBitmap(Context context, String imagePath) {
        try {
            return Glide.with(context).asBitmap().load(imagePath).submit().get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Copy audio file from Uri to app's private storage
     *
     * @param context Application context
     * @param uri     Uri of the audio file
     * @param noteId  ID of the note this audio belongs to
     * @return Path to the saved audio file, or null if saving failed
     */
    public static String saveAudio(Context context, Uri uri, String noteId) {
        File directory = getDirectory(context, AUDIO_DIR);
        String fileName = noteId + "_" + UUID.randomUUID().toString() + ".mp3";
        File destFile = new File(directory, fileName);

        try (InputStream in = context.getContentResolver().openInputStream(uri); OutputStream out = new FileOutputStream(destFile)) {

            if (in == null) {
                return null;
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            return destFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving audio: " + e.getMessage());
            return null;
        }
    }

    /**
     * Save drawing file to app's private storage
     *
     * @param context Application context
     * @param bitmap  Bitmap of the drawing
     * @return Uri of the saved drawing file, or null if saving failed
     */
    public static Uri saveDrawing(Context context, Bitmap bitmap) {
        File directory = getDirectory(context, DRAWING_DIR);
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".png";
        File file = new File(directory, fileName);
        Log.d(TAG, "saveDrawing: " + file.getAbsolutePath());

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } catch (IOException e) {
            Log.e(TAG, "Error saving drawing: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete a file from storage and remove it from Glide's cache if it's an image
     *
     * @param path Path to the file
     * @return true if deletion was successful, false otherwise
     */
    public static boolean deleteFile(String path) {
        if (path == null) {
            return false;
        }

        File file = new File(path);
        return file.exists() && file.delete();
    }

    /**
     * Get or create directory for storing files
     *
     * @param context Application context
     * @param dirName Name of the directory
     * @return File object representing the directory
     */
    private static File getDirectory(Context context, String dirName) {
        File directory = new File(context.getFilesDir(), dirName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    /**
     * Delete all files associated with a note
     *
     * @param paths List of file paths to delete
     */
    public static void deleteFiles(Iterable<String> paths) {
        if (paths == null) {
            return;
        }

        for (String path : paths) {
            deleteFile(path);
        }
    }

    /**
     * Clear all Glide caches
     *
     * @param context Application context
     */
    public static void clearGlideCache(final Context context) {
        new Thread(() -> {
            Glide.get(context).clearDiskCache();
        }).start();
        Glide.get(context).clearMemory();
    }

    /**
     * Callback interface for asynchronous image saving
     */
    public interface ImageSaveCallback {
        void onImageSaved(String imagePath);
    }
}
