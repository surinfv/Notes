package com.fed.notes.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.fed.notes.database.Entity.Note;

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
    
    /**
     * если вернуть вместо single - flowable, то в подписку будут прилетать изменения
     * если таблица поменялась
     */
    @Query("SELECT * FROM notes WHERE id = :id")
    Single<Note> getNoteRx(UUID id);

// --Commented out by Inspection START (03.11.2017 12:11):
//    @Query("SELECT * FROM notes")
//    List<Note> getAllNotes();
// --Commented out by Inspection STOP (03.11.2017 12:11)

// --Commented out by Inspection START (03.11.2017 12:11):
//    @Query("SELECT * FROM notes WHERE id IN(:ids)")
//    List<Note> getNotes(UUID[] ids);
// --Commented out by Inspection STOP (03.11.2017 12:11)
}
