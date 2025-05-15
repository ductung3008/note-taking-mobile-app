package com.haui.notetakingapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity(tableName = "notes")
public class Note implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String content;
    private long createdAt;
    private long updatedAt;

    private List<String> imagePaths;
    private List<String> audioPaths;
    private List<String> drawingPaths;
    private List<CheckListItem> checklistItems;
    private boolean isPinned;
    private boolean isDeleted;

    public Note(String title, String content) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isPinned = false;
        this.isDeleted = false;
        this.imagePaths = null;
        this.audioPaths = null;
        this.drawingPaths = null;
        this.checklistItems = null;
    }

    @Ignore
    public Note() {
        this.id = UUID.randomUUID().toString();
        this.title = "";
        this.content = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isPinned = false;
        this.isDeleted = false;
        this.imagePaths = null;
        this.audioPaths = null;
        this.drawingPaths = null;
        this.checklistItems = null;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
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

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public List<String> getAudioPaths() {
        return audioPaths;
    }

    public void setAudioPaths(List<String> audioPaths) {
        this.audioPaths = audioPaths;
    }

    public List<String> getDrawingPaths() {
        return drawingPaths;
    }

    public void setDrawingPaths(List<String> drawingPaths) {
        this.drawingPaths = drawingPaths;
    }

    public List<CheckListItem> getChecklistItems() {
        return checklistItems;
    }

    public void setChecklistItems(List<CheckListItem> checklistItems) {
        this.checklistItems = checklistItems;
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
}
