package com.haui.notetakingapp.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.haui.notetakingapp.data.local.entity.CheckListItem;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.repository.NoteRepository;

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
    private List<String> imagePaths = new ArrayList<>();
    private List<String> audioPaths = new ArrayList<>();
    private List<String> drawingPaths = new ArrayList<>();
    private MutableLiveData<List<CheckListItem>> checklistItems = new MutableLiveData<>(new ArrayList<>());


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
    public void setChecklistItems(List<CheckListItem> items) {
        checklistItems.setValue(items);
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

    public void addAudioPath(List<Uri> audioPaths) {
        for (Uri audioPath : audioPaths) {
            if (audioPath != null) {
                this.audioPaths.add(String.valueOf(audioPath));
            }
        }
    }

    public LiveData<List<CheckListItem>> getChecklistItems() {
        return checklistItems;
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

    public List<String> getDrawingPaths() {
        return drawingPaths;
    }

    public void setDrawingPaths(List<String> drawingPaths) {
        this.drawingPaths = drawingPaths;
    }

    public void saveNote() {
        if (title.trim().isEmpty() || content.trim().isEmpty()) {
            _errorMessage.setValue("Vui lòng nhập đầy đủ tiêu đề và nội dung");
            return;
        }

        Note note = new Note(title, content);
        note.setImagePaths(getSelectedImagePaths());
        note.setAudioPaths(getRecordedAudioPaths());
        note.setCreatedAt(System.currentTimeMillis());
        note.setUpdatedAt(System.currentTimeMillis());
        note.setChecklistItems(checklistItems.getValue());
        note.setDrawingPaths(getDrawImagePaths());

        noteRepository.insert(note);
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
