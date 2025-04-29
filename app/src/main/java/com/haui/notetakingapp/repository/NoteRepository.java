package com.haui.notetakingapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.haui.notetakingapp.data.local.NoteDatabase;
import com.haui.notetakingapp.data.local.dao.NoteDao;
import com.haui.notetakingapp.data.local.entity.Note;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private final NoteDao noteDao;
    private final ExecutorService executorService;

    public NoteRepository(Application application) {
        NoteDatabase db = NoteDatabase.getInstance(application);
        noteDao = db.noteDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Note note) {
        executorService.execute(() -> noteDao.insertNote(note));
    }

    public void update(Note note) {
        executorService.execute(() -> noteDao.updateNode(note));
    }

    public void delete(Note note) {
        executorService.execute(() -> noteDao.deleteNote(note));
    }

    public LiveData<List<Note>> getAllNotes() {
        MutableLiveData<List<Note>> notesLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Note> notes = noteDao.getAllNotes();
            notesLiveData.postValue(notes);
        });
        return notesLiveData;
    }
}
