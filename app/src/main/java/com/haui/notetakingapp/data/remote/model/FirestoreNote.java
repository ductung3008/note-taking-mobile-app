package com.haui.notetakingapp.data.remote.model;

import com.haui.notetakingapp.data.local.entity.Note;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class representing a note in Firestore.
 * This will be used when syncing with Firebase Firestore in the future.
 */
public class FirestoreNote {
    private String id;
    private String title;
    private String content;
    private long createdAt;
    private long updatedAt;
    private List<String> imageUrls;
    private List<String> audioUrls;
    private List<String> drawingUrls;
    private boolean isPinned;
    private boolean isDeleted;
    private String userId;

    // Empty constructor required for Firestore
    public FirestoreNote() {
    }

    /**
     * Convert a local Note to a FirestoreNote
     *
     * @param note   Local Note entity
     * @param userId User ID who owns the note
     * @return FirestoreNote ready for Firestore
     */
    public static FirestoreNote fromNote(Note note, String userId) {
        FirestoreNote firestoreNote = new FirestoreNote();
        firestoreNote.id = note.getId();
        firestoreNote.title = note.getTitle();
        firestoreNote.content = note.getContent();
        firestoreNote.createdAt = note.getCreatedAt();
        firestoreNote.updatedAt = note.getUpdatedAt();
        firestoreNote.isPinned = note.isPinned();
        firestoreNote.isDeleted = note.getIsDeleted();
        firestoreNote.userId = userId;

        // Note: Image, audio and drawing URLs would need to be
        // set after uploading files to Firebase Storage

        return firestoreNote;
    }

    /**
     * Convert this FirestoreNote to a Map for Firestore storage
     *
     * @return Map representation of this note
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("content", content);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("isPinned", isPinned);
        map.put("isDeleted", isDeleted);
        map.put("userId", userId);

        if (imageUrls != null) {
            map.put("imageUrls", imageUrls);
        }

        if (audioUrls != null) {
            map.put("audioUrls", audioUrls);
        }

        if (drawingUrls != null) {
            map.put("drawingUrls", drawingUrls);
        }

        return map;
    }

    /**
     * Convert this FirestoreNote to a local Note entity
     *
     * @return Note entity for local storage
     */
    public Note toNote() {
        Note note = new Note();
        note.setId(id);
        note.setTitle(title);
        note.setContent(content);
        note.setCreatedAt(createdAt);
        note.setUpdatedAt(updatedAt);
        note.setPinned(isPinned);
        note.setDeleted(this.isDeleted);

        // Note: When converting from Firestore to local Note,
        // you would need to download files from Firebase Storage URLs
        // and save them locally, then set the local paths

        return note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getAudioUrls() {
        return audioUrls;
    }

    public void setAudioUrls(List<String> audioUrls) {
        this.audioUrls = audioUrls;
    }

    public List<String> getDrawingUrls() {
        return drawingUrls;
    }

    public void setDrawingUrls(List<String> drawingUrls) {
        this.drawingUrls = drawingUrls;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
