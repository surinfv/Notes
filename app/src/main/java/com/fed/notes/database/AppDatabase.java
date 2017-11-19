package com.fed.notes.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.os.Environment;

import com.fed.notes.App;

import java.io.File;

/**
 * Created by Fedor SURIN on 26.10.2017.
 */

@Database(entities = Note.class, version = 1)
@TypeConverters(Converter.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract NoteDAO getNoteDao();
}
