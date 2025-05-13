package com.haui.notetakingapp.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.haui.notetakingapp.data.local.entity.CheckListItem;
import com.haui.notetakingapp.repository.NoteRepository;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseNoteViewModel extends AndroidViewModel {
    protected final NoteRepository noteRepository;

    protected final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    protected final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<Boolean> saveSuccess = _saveSuccess;
    public LiveData<String> errorMessage = _errorMessage;

    protected String title = "";
    protected String content = "";
    protected List<String> imagePaths = new ArrayList<>();
    protected List<String> audioPaths = new ArrayList<>();
    protected List<String> drawingPaths = new ArrayList<>();
    protected MutableLiveData<List<CheckListItem>> checklistItems = new MutableLiveData<>(new ArrayList<>());

    public BaseNoteViewModel(@NonNull Application application) {
        super(application);
        noteRepository = new NoteRepository(application);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
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

    public void addDrawingPath(Uri drawingPath) {
        if (drawingPath != null) {
            this.drawingPaths.add(String.valueOf(drawingPath));
        }
    }

    public void removeImagePath(Uri imageUri) {
        if (imageUri != null) {
            imagePaths.remove(String.valueOf(imageUri));
        }
    }

    public void addAudioPath(String audioPath) {
        if (audioPath != null) {
            audioPaths.add(audioPath);
        }
    }

    public void addAudioPaths(List<Uri> audioPaths) {
        for (Uri audioPath : audioPaths) {
            if (audioPath != null) {
                this.audioPaths.add(String.valueOf(audioPath));
            }
        }
    }

    public void removeAudioPath(String audioPath) {
        if (audioPath != null) {
            audioPaths.remove(audioPath);
        }
    }

    public void clearAudioPaths() {
        this.audioPaths.clear();
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

    protected List<String> getSelectedImagePaths() {
        List<String> selectedImagePaths = new ArrayList<>();
        for (String imagePath : imagePaths) {
            if (imagePath != null) {
                selectedImagePaths.add(imagePath);
            }
        }
        return selectedImagePaths;
    }

    protected List<String> getRecordedAudioPaths() {
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

    protected List<String> getDrewPaths() {
        List<String> drewPaths = new ArrayList<>();
        for (String drawImagePath : drawingPaths) {
            if (drawImagePath != null) {
                drewPaths.add(drawImagePath);
            }
        }
        return drewPaths;
    }

    protected boolean validateInput() {
        if (title.trim().isEmpty() || content.trim().isEmpty()) {
            _errorMessage.setValue("Vui lòng nhập đầy đủ tiêu đề và nội dung");
            return false;
        }
        return true;
    }

    public abstract void saveNote();

    public List<String> getDrawingPaths() {
        return drawingPaths;
    }

    public void setDrawingPaths(List<String> drawingPaths) {
        this.drawingPaths = drawingPaths;
    }
}
