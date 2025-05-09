package com.haui.notetakingapp.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.haui.notetakingapp.data.local.entity.CheckListItem;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.repository.NoteRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NewNoteViewModel extends AndroidViewModel {
    private final NoteRepository noteRepository;

    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private MutableLiveData<List<CheckListItem>> checklistItems = new MutableLiveData<>(new ArrayList<>());
    public LiveData<Boolean> saveSuccess = _saveSuccess;
    public LiveData<String> errorMessage = _errorMessage;

    private String title = "";
    private String content = "";
    private List<Uri> imageUris = new ArrayList<>();
    private String audioFilePath = null;
//    private List<String> checklistItems = new ArrayList<>();
    private List<Uri> drawImages = new ArrayList<>();


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
        if (imageUri != null) {
            imageUris.add(imageUri);
        }
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }
    public LiveData<List<CheckListItem>> getChecklistItems() {
        return checklistItems;
    }
    public void setChecklistItems(List<CheckListItem> items) {
        checklistItems.setValue(items);
    }
    public void addChecklistItem(CheckListItem item) {
        List<CheckListItem> items = checklistItems.getValue();
        if (items != null) {
            items.add(item);
            checklistItems.setValue(items);
        }
    }

    public void removeChecklistItem(CheckListItem item) {
        List<CheckListItem> items = checklistItems.getValue();
        if (items != null) {
            items.remove(item);
            checklistItems.setValue(items);
        }
    }

    public List<Uri> getDrawImages() {
        return drawImages;
    }

    public void setDrawImages(List<Uri> drawImages) {
        this.drawImages = drawImages;
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
        note.setChecklistItems(checklistItems.getValue());
        note.setDrawingPaths(getDrawImagePaths());
        noteRepository.insert(note);
        _saveSuccess.setValue(true);
    }

    private List<String> getSelectedImagePaths() {
        List<String> imagePaths = new ArrayList<>();

        for (Uri uri : imageUris) {
            imagePaths.add(uri.toString());
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


    private List<String> getDrawImagePaths() {
        List<String> drawImagePaths = new ArrayList<>();
        for (Uri uri : drawImages) {
            drawImagePaths.add(uri.toString());  // Chuyển đổi Uri thành String
        }
        return drawImagePaths;
    }
}
