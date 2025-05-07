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
    private final LiveData<List<Note>> allNotes; // LiveData cho ghi chú CHƯA xóa
    private final LiveData<List<Note>> deletedNotes; // LiveData cho ghi chú ĐÃ xóa
    private final Application application;

    public NoteRepository(Application application) {
        this.application = application;
        NoteDatabase db = NoteDatabase.getInstance(application);
        noteDao = db.noteDao();
        // Lấy tất cả ghi chú CHƯA xóa cho màn hình Home
        allNotes = noteDao.getAllNotesLiveData();
        // Lấy tất cả ghi chú ĐÃ xóa cho màn hình thùng rác
        deletedNotes = noteDao.getDeletedNotesLiveData(); // Sử dụng query mới trong NoteDao
        executorService = Executors.newSingleThreadExecutor();
    }

    // Phương thức thêm ghi chú mới
    public void insert(Note note) {
        executorService.execute(() -> noteDao.insertNote(note));
    }

    // Phương thức cập nhật ghi chú
    public void update(Note note) {
        note.setUpdatedAt(System.currentTimeMillis());
        executorService.execute(() -> noteDao.updateNote(note));
    }

    // Phương thức "xóa mềm" ghi chú (chuyển vào thùng rác)
    public void moveToTrash(Note note) {
        executorService.execute(() -> {
            note.setDeleted(true); // Đánh dấu là đã xóa
            // Cập nhật thời gian xóa nếu bạn có thuộc tính đó, ở đây dùng lại updatedAt
            note.setUpdatedAt(System.currentTimeMillis());
            noteDao.updateNote(note); // Cập nhật trạng thái trong database
            // Lưu ý: Không xóa tệp đính kèm ở đây, chỉ xóa vĩnh viễn mới xóa tệp
        });
    }

    // Phương thức khôi phục ghi chú từ thùng rác
    public void restoreNote(Note note) {
        executorService.execute(() -> {
            note.setDeleted(false); // Bỏ đánh dấu đã xóa
            // Cập nhật thời gian khôi phục nếu bạn có thuộc tính đó, ở đây dùng lại updatedAt
            note.setUpdatedAt(System.currentTimeMillis());
            noteDao.updateNote(note); // Cập nhật trạng thái trong database
        });
    }

    // Phương thức xóa vĩnh viễn một ghi chú cụ thể khỏi database VÀ xóa tệp đính kèm
    public void permanentlyDeleteNote(Note note) {
        executorService.execute(() -> {
            // Xóa các tệp đính kèm trước
            FileManager.deleteFiles(note.getImagePaths());
            FileManager.deleteFiles(note.getAudioPaths());
            FileManager.deleteFiles(note.getDrawingPaths());

            // Xóa ghi chú khỏi database
            noteDao.deleteNote(note);
        });
    }

    // Phương thức xóa vĩnh viễn TẤT CẢ ghi chú trong thùng rác
    public void emptyTrash() {
        executorService.execute(() -> {
            List<Note> notesToDelete = noteDao.getDeletedNotesSync(); // Lấy danh sách ghi chú đã xóa một cách đồng bộ
            if (notesToDelete != null && !notesToDelete.isEmpty()) {
                for (Note note : notesToDelete) {
                    // Xóa các tệp đính kèm cho từng ghi chú
                    FileManager.deleteFiles(note.getImagePaths());
                    FileManager.deleteFiles(note.getAudioPaths());
                    FileManager.deleteFiles(note.getDrawingPaths());
                    // Xóa ghi chú khỏi database
                    noteDao.deleteNote(note);
                }
            }
        });
    }

    // Lấy tất cả ghi chú CHƯA xóa (cho màn hình Home)
    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    // Lấy tất cả ghi chú ĐÃ xóa (cho màn hình thùng rác)
    public LiveData<List<Note>> getDeletedNotes() {
        return deletedNotes;
    }

    // Phương thức tìm kiếm ghi chú (chỉ tìm kiếm ghi chú CHƯA xóa)
    public LiveData<List<Note>> searchNotes(String query) {
        return noteDao.searchNotes("%" + query + "%");
    }

    // Phương thức để lấy một ghi chú theo ID (có thể cần cho màn hình chỉnh sửa hoặc xem chi tiết)
    public LiveData<Note> getNoteById(String noteId) {
        return noteDao.getNoteById(noteId);
    }

    // Cần cập nhật phương thức delete ban đầu trong HomeViewModel và bất cứ đâu sử dụng nó
    // để gọi moveToTrash thay vì permanentlyDeleteNote
}