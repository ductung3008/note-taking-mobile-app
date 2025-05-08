package com.haui.notetakingapp.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.repository.NoteRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditNoteViewModel extends AndroidViewModel {
    private final NoteRepository noteRepository;

    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<Boolean> saveSuccess = _saveSuccess;
    public LiveData<String> errorMessage = _errorMessage;

    private Note currentNote;
    private String title = "";
    private String content = "";
    private Uri imageUri = null;
    private String audioFilePath = null;

    public EditNoteViewModel(@NonNull Application application) {
        super(application);
        noteRepository = new NoteRepository(application);
    }

    public void setCurrentNote(Note note) {
        this.currentNote = note;
        this.title = note.getTitle();
        this.content = note.getContent();

        // Keep existing image paths if they exist
        if (note.getImagePaths() != null && !note.getImagePaths().isEmpty()) {
            String imagePath = note.getImagePaths().get(0);
            if (imagePath.startsWith("content://")) {
                this.imageUri = Uri.parse(imagePath);
            }
        }

        // Keep existing audio paths if they exist
        if (note.getAudioPaths() != null && !note.getAudioPaths().isEmpty()) {
            this.audioFilePath = note.getAudioPaths().get(0);
        }
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void updateNote() {
        Toast.makeText(getApplication(), "Title: " + title + "\nContent: " + content, Toast.LENGTH_SHORT).show();
        if (title.trim().isEmpty() || content.trim().isEmpty()) {
            _errorMessage.setValue("Vui lòng nhập đầy đủ tiêu đề và nội dung");
            return;
        }

        if (currentNote == null) {
            _errorMessage.setValue("Không tìm thấy ghi chú cần chỉnh sửa");
            return;
        }

        // Update the current note's properties
        currentNote.setTitle(title);
        currentNote.setContent(content);
        currentNote.setImagePaths(getSelectedImagePaths());
        currentNote.setAudioPaths(getRecordedAudioPaths());
        currentNote.setUpdatedAt(System.currentTimeMillis());

        noteRepository.update(currentNote);
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