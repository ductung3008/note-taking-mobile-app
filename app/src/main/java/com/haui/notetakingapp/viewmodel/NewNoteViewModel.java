package com.haui.notetakingapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import com.haui.notetakingapp.data.local.entity.Note;

public class NewNoteViewModel extends BaseNoteViewModel {

    public NewNoteViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void saveNote() {
        if (!validateInput()) {
            return;
        }

        Note note = new Note(title, content);
        note.setImagePaths(getSelectedImagePaths());
        note.setAudioPaths(getRecordedAudioPaths());
        note.setDrawingPaths(getDrewPaths());
        note.setCreatedAt(System.currentTimeMillis());
        note.setUpdatedAt(System.currentTimeMillis());
        note.setChecklistItems(checklistItems.getValue());

        noteRepository.insert(note);
        _saveSuccess.setValue(true);
    }
}
