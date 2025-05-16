package com.haui.notetakingapp.data.remote.firebase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.haui.notetakingapp.data.local.FileManager;
import com.haui.notetakingapp.data.local.dao.NoteDao;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.data.remote.cloudinary.CloudinaryManager;
import com.haui.notetakingapp.data.remote.model.FirestoreNote;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SyncManager implements LifecycleObserver {
    private static final String TAG = "SyncManager";
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final long SYNC_INTERVAL_MILLISECONDS = 15 * 60 * 1000; // 15 minutes
    private static final String PREFS_NAME = "NoteSyncPrefs";
    private static final String LAST_SYNC_TIME = "lastSyncTime";

    private static SyncManager instance;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable syncRunnable;
    private boolean isInitialized = false;
    private Context appContext;
    private NoteDao noteDao;

    private SyncManager() {
    }

    public static synchronized SyncManager getInstance() {
        if (instance == null) {
            instance = new SyncManager();
        }
        return instance;
    }

    public static void syncBothDirections(NoteDao noteDao, String userId) {
        // 1. Đồng bộ từ local lên Firestore
        syncNotesToFirestore(noteDao, userId);

        // 2. Đồng bộ từ Firestore về local
        syncNotesFromFirestore(noteDao, userId);
    }

    public static void syncNotesToFirestore(NoteDao noteDao, String userId) {
        executor.execute(() -> {
            try {
                List<Note> localNotes = noteDao.getAllNotesSync();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                for (Note note : localNotes) {
                    Map<String, String> uriToUrlMap = new HashMap<>();

                    FirestoreNote fsNote = uploadAttachmentsAndCreateFirestoreNote(note, userId, uriToUrlMap);

                    db.collection("notes")
                            .document(note.getId())
                            .set(fsNote.toMap(), SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Note synced to Firestore: " + note.getId());

                                // Update local note with Cloudinary URLs
                                updateLocalNoteWithCloudinaryUrls(noteDao, note, uriToUrlMap);
                            })
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to sync note: " + note.getId(), e));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing notes to Firestore", e);
            }
        });
    }

    private static FirestoreNote uploadAttachmentsAndCreateFirestoreNote(Note note, String userId, Map<String, String> uriToUrlMap) {
        List<String> imageUrls = uploadAttachments(note.getImagePaths(), "images/" + userId, uriToUrlMap);
        List<String> audioUrls = uploadAttachments(note.getAudioPaths(), "audio/" + userId, uriToUrlMap);
        List<String> drawingUrls = uploadAttachments(note.getDrawingPaths(), "drawings/" + userId, uriToUrlMap);

        return FirestoreNote.fromNoteWithAttachments(note, userId, imageUrls, audioUrls, drawingUrls);
    }

    private static List<String> uploadAttachments(List<String> filePaths, String folder, Map<String, String> uriToUrlMap) {
        if (filePaths == null || filePaths.isEmpty()) {
            return null;
        }

        List<String> uploadedUrls = new ArrayList<>();
        CloudinaryManager cloudinaryManager = CloudinaryManager.getInstance();

        for (String path : filePaths) {
            if (isUrl(path)) {
                uploadedUrls.add(path);
                continue;
            }

            String cloudinaryUrl = cloudinaryManager.uploadFile(path, folder);
            if (cloudinaryUrl != null) {
                uploadedUrls.add(cloudinaryUrl);
                uriToUrlMap.put(path, cloudinaryUrl);
                Log.d(TAG, "File uploaded to Cloudinary: " + path + " -> " + cloudinaryUrl);
            } else {
                Log.e(TAG, "Failed to upload file to Cloudinary: " + path);
            }
        }

        return uploadedUrls.isEmpty() ? null : uploadedUrls;
    }

    private static void updateLocalNoteWithCloudinaryUrls(NoteDao noteDao, Note note, Map<String, String> uriToUrlMap) {
        if (uriToUrlMap.isEmpty()) {
            return;
        }

        executor.execute(() -> {
            try {
                Note currentNote = noteDao.getNoteByIdSync(note.getId());
                if (currentNote == null) {
                    Log.w(TAG, "Cannot update local note with Cloudinary URLs: Note not found in database");
                    return;
                }

                boolean changed = false;

                if (currentNote.getImagePaths() != null) {
                    List<String> updatedImagePaths = updatePathsWithCloudinaryUrls(
                            currentNote.getImagePaths(), uriToUrlMap);
                    if (updatedImagePaths != null) {
                        FileManager.deleteFiles(instance.appContext, currentNote.getImagePaths());
                        currentNote.setImagePaths(updatedImagePaths);
                        changed = true;
                    }
                }

                if (currentNote.getAudioPaths() != null) {
                    List<String> updatedAudioPaths = updatePathsWithCloudinaryUrls(
                            currentNote.getAudioPaths(), uriToUrlMap);
                    if (updatedAudioPaths != null) {
                        FileManager.deleteFiles(instance.appContext, currentNote.getAudioPaths());
                        currentNote.setAudioPaths(updatedAudioPaths);
                        changed = true;
                    }
                }

                if (currentNote.getDrawingPaths() != null) {
                    List<String> updatedDrawingPaths = updatePathsWithCloudinaryUrls(
                            currentNote.getDrawingPaths(), uriToUrlMap);
                    if (updatedDrawingPaths != null) {
                        FileManager.deleteFiles(instance.appContext, currentNote.getDrawingPaths());
                        currentNote.setDrawingPaths(updatedDrawingPaths);
                        changed = true;
                    }
                }

                if (changed) {
                    Log.d(TAG, "Updating local note with Cloudinary URLs: " + note.getId());
                    noteDao.updateNote(currentNote);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating local note with Cloudinary URLs", e);
            }
        });
    }

    private static List<String> updatePathsWithCloudinaryUrls(List<String> paths, Map<String, String> uriToUrlMap) {
        if (paths == null || paths.isEmpty()) {
            return null;
        }

        boolean changed = false;
        List<String> updatedPaths = new ArrayList<>(paths);

        for (int i = 0; i < updatedPaths.size(); i++) {
            String path = updatedPaths.get(i);
            if (uriToUrlMap.containsKey(path)) {
                updatedPaths.set(i, uriToUrlMap.get(path));
                changed = true;
            }
        }

        return changed ? updatedPaths : null;
    }

    private static boolean isUrl(String string) {
        try {
            new URL(string);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static void syncNotesFromFirestore(NoteDao noteDao, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("notes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    executor.execute(() -> {
                        try {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                FirestoreNote fsNote = doc.toObject(FirestoreNote.class);
                                if (fsNote != null) {
                                    Note localNote = fsNote.toNote();
                                    Note existingNote = noteDao.getNoteByIdSync(localNote.getId());

                                    if (existingNote == null) {
                                        noteDao.insertNote(localNote);
                                    } else if (localNote.getUpdatedAt() > existingNote.getUpdatedAt()) {
                                        noteDao.updateNote(localNote);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing Firestore notes", e);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch notes from Firestore", e);
                });
    }

    public void initialize(Context context, NoteDao dao) {
        if (isInitialized) return;

        this.appContext = context;
        this.noteDao = dao;

        // Register lifecycle observer to sync when app goes to background
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        // Schedule periodic syncing
        schedulePeriodicalSync();

        isInitialized = true;

        // Initial sync if needed
        checkAndSync();
    }

    public void reset() {
        if (syncRunnable != null) {
            handler.removeCallbacks(syncRunnable);
            syncRunnable = null;
        }

        if (isInitialized) {
            try {
                ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.appContext = null;
        this.noteDao = null;

        isInitialized = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        syncIfLoggedIn();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        checkAndSync();
    }

    private void schedulePeriodicalSync() {
        syncRunnable = () -> {
            syncIfLoggedIn();
            // Schedule next run
            handler.postDelayed(syncRunnable, SYNC_INTERVAL_MILLISECONDS);
        };

        // Start the periodic sync
        handler.postDelayed(syncRunnable, SYNC_INTERVAL_MILLISECONDS);
    }

    private void checkAndSync() {
        if (appContext == null) return;

        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastSyncTime = prefs.getLong(LAST_SYNC_TIME, 0);
        long currentTime = System.currentTimeMillis();

        // If it's been more than the sync interval since last sync, sync now
        if (currentTime - lastSyncTime > SYNC_INTERVAL_MILLISECONDS) {
            syncIfLoggedIn();
        }
    }

    private void syncIfLoggedIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && appContext != null && noteDao != null) {
            syncBothDirections(noteDao, currentUser.getUid());

            // Update last sync time
            if (appContext != null) {
                SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putLong(LAST_SYNC_TIME, System.currentTimeMillis()).apply();
            }
        }
    }

    // Sync a single note immediately (use when a note is created/updated)
    public void syncSingleNote(Note note) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && note != null) {
            executor.execute(() -> {
                try {
                    Map<String, String> uriToUrlMap = new HashMap<>();

                    FirestoreNote fsNote = uploadAttachmentsAndCreateFirestoreNote(note, currentUser.getUid(), uriToUrlMap);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("notes")
                            .document(note.getId())
                            .set(fsNote.toMap())
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Single note synced to Firestore: " + note.getId() + ", isDeleted=" + note.getIsDeleted());
                                updateLocalNoteWithCloudinaryUrls(noteDao, note, uriToUrlMap);
                            })
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to sync single note: " + note.getId(), e));
                } catch (Exception e) {
                    Log.e(TAG, "Error in syncSingleNote", e);
                }
            });
        }
    }

    // Delete a note from Firestore (use when a note is permanently deleted)
    public void deleteSyncedNote(String noteId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && noteId != null) {
            executor.execute(() -> {
                try {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("notes")
                            .document(noteId)
                            .delete()
                            .addOnSuccessListener(aVoid ->
                                    Log.d(TAG, "Note deleted from Firestore: " + noteId))
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to delete note from Firestore: " + noteId, e));
                } catch (Exception e) {
                    Log.e(TAG, "Error deleting synced note", e);
                }
            });
        }
    }

    // For BroadcastReceiver to handle periodic sync
    public static class SyncBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SyncManager.getInstance().syncIfLoggedIn();
        }
    }
}
