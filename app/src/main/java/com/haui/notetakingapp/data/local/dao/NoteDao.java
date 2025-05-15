package com.haui.notetakingapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.haui.notetakingapp.data.local.entity.Note;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    void insertNote(Note note);

    @Update
    void updateNote(Note note);

    @Delete
    void deleteNote(Note note);

    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    LiveData<List<Note>> getAllNotesLiveData();

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    LiveData<List<Note>> getDeletedNotesLiveData();

    @Query("SELECT * FROM notes WHERE title LIKE :query OR content LIKE :query ORDER BY updatedAt DESC")
    LiveData<List<Note>> searchNotes(String query);

    @Query("SELECT * FROM notes WHERE id = :noteId")
    LiveData<Note> getNoteById(String noteId);

    @Query("SELECT * FROM notes WHERE id = :noteId")
    Note getNoteByIdSync(String noteId);

    @Query("SELECT * FROM notes WHERE isDeleted = 1")
    List<Note> getDeletedNotesSync();

    @Query("SELECT * FROM notes")
    List<Note> getAllNotesSync();

    @Query("DELETE FROM notes")
    void deleteAllNotes();
}
