package com.fed.notes.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.fed.notes.database.Entity.Note;

import java.util.List;
import java.util.UUID;

import io.reactivex.Single;

@Dao
public interface NoteDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes WHERE id = :id")
    Note getNote(UUID id);

    @Query("SELECT * FROM notes WHERE id = :id")
    Single<Note> getNoteRx(UUID id);

    @Query("SELECT * FROM notes WHERE id IN(:ids)")
    Single<List<Note>> getNotes(UUID[] ids);
}
