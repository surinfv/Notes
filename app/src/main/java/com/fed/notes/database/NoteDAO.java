package com.fed.notes.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;


import java.util.List;
import java.util.UUID;

/**
 * Created by Fedor SURIN on 31.10.2017.
 */

@Dao
public interface NoteDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :id")
    Note getNote(UUID id);

    @Query("SELECT * FROM notes WHERE id IN(:ids)")
    List<Note> getNotes(UUID[] ids);
}
