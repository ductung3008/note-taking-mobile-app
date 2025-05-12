package com.haui.notetakingapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.haui.notetakingapp.data.local.entity.Note;

import java.util.ArrayList;

public class EditNoteViewModel extends BaseNoteViewModel {
    private Note currentNote;

    public EditNoteViewModel(@NonNull Application application) {
        super(application);
    }

    public Note getCurrentNote() {
        return currentNote;
    }

    public void setCurrentNote(Note note) {
        this.currentNote = note;
        this.title = note.getTitle();
        this.content = note.getContent();

        if (note.getImagePaths() != null) {
            this.imagePaths = new ArrayList<>(note.getImagePaths());
        }

        if (note.getAudioPaths() != null) {
            this.audioPaths = new ArrayList<>(note.getAudioPaths());
        }

        if (note.getDrawingPaths() != null) {
            this.drawingPaths = new ArrayList<>(note.getDrawingPaths());
        }

        if (note.getChecklistItems() != null) {
            setChecklistItems(note.getChecklistItems());
        }
    }

    public void updateNote() {
        saveNote();
    }

    @Override
    public void saveNote() {
        if (!validateInput()) {
            return;
        }

        if (currentNote == null) {
            _errorMessage.setValue("No note to update");
            return;
        }

        currentNote.setTitle(title);
        currentNote.setContent(content);
        currentNote.setImagePaths(getSelectedImagePaths());
        currentNote.setAudioPaths(getRecordedAudioPaths());
        currentNote.setDrawingPaths(getDrewPaths());
        currentNote.setChecklistItems(checklistItems.getValue());
        currentNote.setUpdatedAt(System.currentTimeMillis());

        noteRepository.update(currentNote);
        _saveSuccess.setValue(true);
    }
}
