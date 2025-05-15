package com.haui.notetakingapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.haui.notetakingapp.data.local.NoteDatabase;
import com.haui.notetakingapp.data.local.dao.NoteDao;
import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.data.remote.firebase.SyncManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private final NoteDao noteDao;
    private final ExecutorService executorService;
    private final LiveData<List<Note>> allNotes;
    private final LiveData<List<Note>> deletedNotes;
    private final Application application;
    private final SyncManager syncManager;

    public NoteRepository(Application application) {
        this.application = application;
        NoteDatabase db = NoteDatabase.getInstance(application);
        noteDao = db.noteDao();
        allNotes = noteDao.getAllNotesLiveData();
        deletedNotes = noteDao.getDeletedNotesLiveData();
        executorService = Executors.newSingleThreadExecutor();
        syncManager = SyncManager.getInstance();
    }

    // Phương thức thêm ghi chú mới
    public void insert(Note note) {
        executorService.execute(() -> {
            noteDao.insertNote(note);

            if (isUserLoggedIn()) {
                syncManager.syncSingleNote(note);
            }
        });
    }

    public LiveData<Note> getNoteById(String noteId) {
        return noteDao.getNoteById(noteId);
    }

    // Phương thức cập nhật ghi chú
    public void update(Note note) {
        note.setUpdatedAt(System.currentTimeMillis());
        executorService.execute(() -> {
            noteDao.updateNote(note);

            if (isUserLoggedIn()) {
                syncManager.syncSingleNote(note);
            }
        });
    }

    // Phương thức "xóa mềm" ghi chú (chuyển vào thùng rác)
    public void moveToTrash(Note note) {
        executorService.execute(() -> {
            note.setDeleted(true);
            note.setUpdatedAt(System.currentTimeMillis());
            noteDao.updateNote(note);

            if (isUserLoggedIn()) {
                syncManager.syncSingleNote(note);
            }
        });
    }

    public void restoreNote(Note note) {
        executorService.execute(() -> {
            note.setDeleted(false);
            note.setUpdatedAt(System.currentTimeMillis());
            noteDao.updateNote(note);

            if (isUserLoggedIn()) {
                syncManager.syncSingleNote(note);
            }
        });
    }

    // Phương thức xóa vĩnh viễn một ghi chú cụ thể khỏi database VÀ xóa tệp đính kèm
    public void permanentlyDeleteNote(Note note) {
        executorService.execute(() -> noteDao.deleteNote(note));
    }

    // Phương thức xóa vĩnh viễn TẤT CẢ ghi chú trong thùng rác
    public void emptyTrash() {
        executorService.execute(() -> {
            List<Note> notesToDelete = noteDao.getDeletedNotesSync();
            for (Note note : notesToDelete) {
                noteDao.deleteNote(note);
            }
        });
    }

    // Lấy tất cả ghi chú CHƯA xóa (cho màn hình Home)
    public LiveData<List<Note>> getAllNotes() {
//        return allNotes;
        return noteDao.getAllNotesLiveData();
    }

    // Lấy tất cả ghi chú ĐÃ xóa (cho màn hình thùng rác)
    public LiveData<List<Note>> getDeletedNotes() {
        return deletedNotes;
    }

    // Helper method to check if user is logged in
    private boolean isUserLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null;
    }

    // Delete all notes from local database when user logs out
    public void clearAllLocalNotes() {
        executorService.execute(noteDao::deleteAllNotes);
    }
}
