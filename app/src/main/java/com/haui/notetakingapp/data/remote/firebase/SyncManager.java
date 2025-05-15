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
import com.haui.notetakingapp.data.local.dao.NoteDao;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.data.remote.model.FirestoreNote;

import java.util.List;
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
        // Private constructor for singleton
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
                    FirestoreNote fsNote = FirestoreNote.fromNote(note, userId);
                    db.collection("notes")
                            .document(note.getId())
                            .set(fsNote.toMap(), SetOptions.merge())
                            .addOnSuccessListener(aVoid ->
                                    Log.d(TAG, "Note synced to Firestore: " + note.getId() + ", isDeleted=" + note.getIsDeleted()))
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to sync note: " + note.getId(), e));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing notes to Firestore", e);
            }
        });
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

                                    if (existingNote != null) {
                                        if (existingNote.getIsDeleted()) {
                                            noteDao.updateNote(localNote);
                                        }
                                    } else {
                                        noteDao.insertNote(localNote);
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

        this.appContext = context.getApplicationContext();
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
                    FirestoreNote fsNote = FirestoreNote.fromNote(note, currentUser.getUid());
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("notes")
                            .document(note.getId())
                            .set(fsNote.toMap())
                            .addOnSuccessListener(aVoid ->
                                    Log.d(TAG, "Single note synced to Firestore: " + note.getId() + ", isDeleted=" + note.getIsDeleted()))
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
