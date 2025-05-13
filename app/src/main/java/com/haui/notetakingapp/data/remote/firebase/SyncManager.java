package com.haui.notetakingapp.data.remote.firebase;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.haui.notetakingapp.data.local.dao.NoteDao;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.data.remote.model.FirestoreNote;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private static final Executor executor = Executors.newSingleThreadExecutor();

    public static void syncBothDirections(Context context, NoteDao noteDao, String userId) {
        // 1. Đồng bộ từ local lên Firestore
        syncNotesToFirestore(context, noteDao, userId);

        // 2. Đồng bộ từ Firestore về local
        syncNotesFromFirestore(context, noteDao, userId);
    }

    public static void syncNotesToFirestore(Context context, NoteDao noteDao, String userId) {
        executor.execute(() -> {
            try {
                List<Note> localNotes = noteDao.getAllNotesSync();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                for (Note note : localNotes) {
                    FirestoreNote fsNote = FirestoreNote.fromNote(note, userId);
                    db.collection("notes")
                            .document(note.getId())
                            .set(fsNote.toMap())
                            .addOnSuccessListener(aVoid ->
                                    Log.d(TAG, "Note synced: " + note.getId()))
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Sync failed for note: " + note.getId(), e));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing notes", e);
            }
        });
    }

    public static void syncNotesFromFirestore(Context context, NoteDao noteDao, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("notes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "📦 Số lượng ghi chú lấy từ Firestore: " + queryDocumentSnapshots.size());
                    executor.execute(() -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            FirestoreNote fsNote = doc.toObject(FirestoreNote.class);
                            if (fsNote != null) {
                                Note localNote = fsNote.toNote();
                                noteDao.insertNote(localNote); // Hoặc updateNote nếu bạn muốn cập nhật
                                Log.d(TAG, "Note pulled from Firestore: " + localNote.getId());
                            }
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to fetch notes from Firestore", e));
    }
}
