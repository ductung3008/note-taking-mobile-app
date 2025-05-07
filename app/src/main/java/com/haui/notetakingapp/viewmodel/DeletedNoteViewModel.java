package com.haui.notetakingapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.haui.notetakingapp.data.local.entity.Note;
import com.haui.notetakingapp.repository.NoteRepository;

import java.util.List;

public class DeletedNoteViewModel extends AndroidViewModel {

    private final NoteRepository noteRepository;
    private final LiveData<List<Note>> deletedNotes;

    public DeletedNoteViewModel(@NonNull Application application) {
        super(application);
        noteRepository = new NoteRepository(application);
        deletedNotes = noteRepository.getDeletedNotes(); // Lấy LiveData của ghi chú đã xóa từ Repository
    }

    // Phương thức để lấy danh sách ghi chú đã xóa
    public LiveData<List<Note>> getDeletedNotes() {
        return deletedNotes;
    }

    // Phương thức để khôi phục một ghi chú
    public void restoreNote(Note note) {
        noteRepository.restoreNote(note);
    }

    // Phương thức để xóa vĩnh viễn một ghi chú
    public void permanentlyDeleteNote(Note note) {
        noteRepository.permanentlyDeleteNote(note);
    }

    // Phương thức để xóa vĩnh viễn tất cả ghi chú trong thùng rác (tùy chọn)
    public void emptyTrash() {
        noteRepository.emptyTrash();
    }
}