package com.haui.notetakingapp.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.repository.NoteRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NewNoteViewModel extends AndroidViewModel {
    private final NoteRepository noteRepository;

    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<Boolean> saveSuccess = _saveSuccess;
    public LiveData<String> errorMessage = _errorMessage;

    private String title = "";
    private String content = "";
    private Uri imageUri = null;
    private String audioFilePath = null;

    public NewNoteViewModel(@NonNull Application application) {
        super(application);
        noteRepository = new NoteRepository(application);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public void saveNote() {
        if (title.trim().isEmpty() || content.trim().isEmpty()) {
            _errorMessage.setValue("Vui lòng nhập đầy đủ tiêu đề và nội dung");
            return;
        }

        Note note = new Note(title, content);
        note.setImagePaths(getSelectedImagePaths());
        note.setAudioPaths(getRecordedAudioPaths());
        note.setUpdatedAt(System.currentTimeMillis());

        noteRepository.insert(note);
        _saveSuccess.setValue(true);
    }

    private List<String> getSelectedImagePaths() {
        List<String> imagePaths = new ArrayList<>();

        if (imageUri != null) {
            String imagePath = imageUri.toString();
            imagePaths.add(imagePath);
        }

        return imagePaths;
    }

    private List<String> getRecordedAudioPaths() {
        List<String> audioPaths = new ArrayList<>();

        if (audioFilePath != null && new File(audioFilePath).exists()) {
            audioPaths.add(audioFilePath);
        }

        return audioPaths;
    }
}
