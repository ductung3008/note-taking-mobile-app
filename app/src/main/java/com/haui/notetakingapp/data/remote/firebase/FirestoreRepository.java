package com.haui.notetakingapp.data.remote.firebase;

import com.haui.notetakingapp.data.local.entity.Note;

import java.util.List;

/**
 * Repository class for Firestore operations.
 * This is just a template for future implementation.
 */
public class FirestoreRepository {

    private static final String COLLECTION_NOTES = "notes";
    private static final String COLLECTION_IMAGES = "images";
    private static final String COLLECTION_AUDIO = "audio";
    private static final String COLLECTION_DRAWINGS = "drawings";

    /**
     * Syncs a note with Firestore
     *
     * @param note      The note to sync
     * @param onSuccess Callback for success
     * @param onFailure Callback for failure
     */
    public void syncNote(Note note, Runnable onSuccess, Runnable onFailure) {
        // TODO: Implement this when adding Firestore
        // This would convert the note to a Map/Document and upload to Firestore
        // For media files, you'd upload them to Firebase Storage and store the URLs
    }

    /**
     * Fetches all notes from Firestore for a user
     *
     * @param userId    User ID
     * @param onSuccess Callback with note list on success
     * @param onFailure Callback for failure
     */
    public void fetchNotes(String userId, OnNotesLoadedListener onSuccess, Runnable onFailure) {
        // TODO: Implement this when adding Firestore
        // This would query Firestore for all notes belonging to the user
    }

    /**
     * Deletes a note from Firestore
     *
     * @param noteId    Note ID to delete
     * @param onSuccess Callback for success
     * @param onFailure Callback for failure
     */
    public void deleteNote(String noteId, Runnable onSuccess, Runnable onFailure) {
        // TODO: Implement this when adding Firestore
        // This would delete the note document and any associated media files
    }

    /**
     * Listener interface for note loading
     */
    public interface OnNotesLoadedListener {
        void onNotesLoaded(List<Note> notes);
    }
}
