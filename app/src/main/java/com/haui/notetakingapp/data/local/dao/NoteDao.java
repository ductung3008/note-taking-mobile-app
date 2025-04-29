package com.haui.notetakingapp.data.local.dao;

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
    public void insertNote(Note note);

    @Update
    public void updateNode(Note note);

    @Delete
    public void deleteNote(Note note);

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    public List<Note> getAllNotes();
}
