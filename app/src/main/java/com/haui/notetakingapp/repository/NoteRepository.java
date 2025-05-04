package com.haui.notetakingapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.haui.notetakingapp.data.local.FileManager;
import com.haui.notetakingapp.data.local.NoteDatabase;
import com.haui.notetakingapp.data.local.dao.NoteDao;
import com.haui.notetakingapp.data.local.entity.Note;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private final NoteDao noteDao;
    private final ExecutorService executorService;
    private final LiveData<List<Note>> allNotes;
    private final Application application;

    public NoteRepository(Application application) {
        this.application = application;
        NoteDatabase db = NoteDatabase.getInstance(application);
        noteDao = db.noteDao();
        allNotes = noteDao.getAllNotesLiveData();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Note note) {
        executorService.execute(() -> noteDao.insertNote(note));
    }

    public void update(Note note) {
        note.setUpdatedAt(System.currentTimeMillis());
        executorService.execute(() -> noteDao.updateNote(note));
    }

    public void delete(Note note) {
        executorService.execute(() -> {
            FileManager.deleteFiles(note.getImagePaths());
            FileManager.deleteFiles(note.getAudioPaths());
            FileManager.deleteFiles(note.getDrawingPaths());

            noteDao.deleteNote(note);
        });
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> searchNotes(String query) {
        return noteDao.searchNotes("%" + query + "%");
    }
}
