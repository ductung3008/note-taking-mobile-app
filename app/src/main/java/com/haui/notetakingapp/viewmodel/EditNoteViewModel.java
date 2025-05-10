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
    private List<String> imagePaths = new ArrayList<>();
    private List<String> audioPaths = null;
    private List<String> drawingPaths = new ArrayList<>();

    public EditNoteViewModel(@NonNull Application application) {
        super(application);
        noteRepository = new NoteRepository(application);
    }

    public Note getCurrentNote() {
        title = currentNote.getTitle();
        content = currentNote.getContent();
        imagePaths = new ArrayList<>(currentNote.getImagePaths());
        audioPaths = new ArrayList<>(currentNote.getAudioPaths());
        drawingPaths = new ArrayList<>(currentNote.getDrawingPaths());
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
    }

    public void addImagePath(Uri imageUri) {
        if (imageUri != null) {
            imagePaths.add(String.valueOf(imageUri));
        }
    }

    public void addImagePaths(List<Uri> imageUris) {
        for (Uri imageUri : imageUris) {
            if (imageUri != null) {
                imagePaths.add(String.valueOf(imageUri));
            }
        }
    }

    public void addAudioPath(String audioPath) {
        if (audioPath != null) {
            audioPaths.add(audioPath);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void updateNote() {
        if (title.trim().isEmpty() || content.trim().isEmpty()) {
            _errorMessage.setValue("Vui lòng nhập đầy đủ tiêu đề và nội dung");
            return;
        }

        currentNote.setTitle(title);
        currentNote.setContent(content);
        currentNote.setImagePaths(getSelectedImagePaths());
        currentNote.setAudioPaths(getRecordedAudioPaths());
        currentNote.setUpdatedAt(System.currentTimeMillis());

        noteRepository.update(currentNote);
        _saveSuccess.setValue(true);
    }

    private List<String> getSelectedImagePaths() {
        List<String> selectedImagePaths = new ArrayList<>();
        for (String imagePath : imagePaths) {
            if (imagePath != null) {
                selectedImagePaths.add(imagePath);
            }
        }
        return selectedImagePaths;
    }

    private List<String> getRecordedAudioPaths() {
        List<String> recordedAudioPaths = new ArrayList<>();
        if (audioPaths != null) {
            for (String audioPath : audioPaths) {
                if (audioPath != null) {
                    recordedAudioPaths.add(audioPath);
                }
            }
        }
        return recordedAudioPaths;
    }

    private List<String> getDrawImagePaths() {
        List<String> drawImagePaths = new ArrayList<>();
        for (String drawImagePath : drawingPaths) {
            if (drawImagePath != null) {
                drawImagePaths.add(drawImagePath);
            }
        }
        return drawImagePaths;
    }
}
