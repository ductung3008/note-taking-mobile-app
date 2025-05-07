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

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes")
    LiveData<List<Note>> getAllNotesLiveData();

    @Query("SELECT * FROM notes WHERE title LIKE :query OR content LIKE :query ORDER BY updatedAt DESC")
    LiveData<List<Note>> searchNotes(String query);

    //
    @Query("SELECT * FROM notes WHERE id = :noteId")
    Note getNoteById(String noteId);
}
