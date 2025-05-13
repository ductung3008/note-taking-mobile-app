package com.haui.notetakingapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.repository.NoteRepository;
import com.haui.notetakingapp.repository.SettingRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final NoteRepository noteRepository;
    private final SettingRepository settingRepository;
    private final MutableLiveData<String> _layoutSetting = new MutableLiveData<>();
    public final LiveData<String> layoutSetting = _layoutSetting;
    private final MutableLiveData<String> _sortBySetting = new MutableLiveData<>();
    public final LiveData<String> sortBySetting = _sortBySetting;
    private final MutableLiveData<String> _themeSetting = new MutableLiveData<>();
    public final LiveData<String> themeSetting = _themeSetting;
    private final LiveData<List<Note>> allNotes;
    private final LiveData<List<Note>> sortedNotes;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        noteRepository = new NoteRepository(application);
        settingRepository = new SettingRepository(application);

        _layoutSetting.setValue(settingRepository.getLayout());
        _sortBySetting.setValue(settingRepository.getSortBy());
        _themeSetting.setValue(settingRepository.getTheme());

        allNotes = noteRepository.getAllNotes();

        sortedNotes = Transformations.switchMap(
                _sortBySetting,
                sortBy -> Transformations.map(
                        allNotes,
                        notes -> sortNotes(notes, sortBy)
                )
        );
    }

    private List<Note> sortNotes(List<Note> notes, String sortBy) {
        if (notes == null) return new ArrayList<>();

        List<Note> sortedList = new ArrayList<>(notes);

        List<Note> pinnedNotes = new ArrayList<>();
        List<Note> unpinnedNotes = new ArrayList<>();

        for (Note note : sortedList) {
            if (note.isPinned()) {
                pinnedNotes.add(note);
            } else {
                unpinnedNotes.add(note);
            }
        }

        Comparator<Note> comparator;

        switch (sortBy) {
            case "Theo tiêu đề":
                comparator = (n1, n2) -> n1.getTitle().compareToIgnoreCase(n2.getTitle());
                break;
            case "Theo ngày tạo":
                comparator = (n1, n2) -> Math.toIntExact(n2.getCreatedAt() - n1.getCreatedAt());
                break;
            case "Theo ngày chỉnh sửa":
            default:
                comparator = (n1, n2) -> Math.toIntExact(n2.getUpdatedAt() - n1.getUpdatedAt());
                break;
        }

        pinnedNotes.sort(comparator);
        unpinnedNotes.sort(comparator);

        List<Note> result = new ArrayList<>(pinnedNotes);
        result.addAll(unpinnedNotes);

        return result;
    }

    public LiveData<List<Note>> getAllNotes() {
        return sortedNotes;
    }

    public void insert(Note note) {
        noteRepository.insert(note);
    }

    public void update(Note note) {
        noteRepository.update(note);
    }

    public void softDelete(Note note) {
        noteRepository.moveToTrash(note);
    }

    public void delete(Note note) {
        noteRepository.permanentlyDeleteNote(note);
    }

    public LiveData<String> getLayoutSetting() {
        return layoutSetting;
    }

    public LiveData<String> getSortBySetting() {
        return sortBySetting;
    }

    public LiveData<String> getThemeSetting() {
        return themeSetting;
    }

    public void refreshSettings() {
        _layoutSetting.setValue(settingRepository.getLayout());
        _sortBySetting.setValue(settingRepository.getSortBy());
        _themeSetting.setValue(settingRepository.getTheme());
    }
}
