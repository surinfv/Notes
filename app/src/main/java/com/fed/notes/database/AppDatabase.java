package com.fed.notes.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.Environment;

import com.fed.notes.Note;

import java.io.File;

/**
 * Created by Fedor SURIN on 26.10.2017.
 */

@Database(entities = Note.class, version = 1)
@TypeConverters(Converter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;
    private static Context context;

    public abstract NoteDAO getNoteDao();

    public static AppDatabase getAppDatabase(Context context) {
        AppDatabase.context = context.getApplicationContext();
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(AppDatabase.context, AppDatabase.class, "notes-database")
                            .allowMainThreadQueries() //TODO: remove this - synk db queries
                            .build();
        }
        return INSTANCE;
    }

    public File getPhotoFile(Note note) {
        File externalFileDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            externalFileDir = AppDatabase.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            externalFileDir = AppDatabase.context.getFilesDir();
        }

        if (externalFileDir == null) return null;

        return new File(externalFileDir, note.getPhotoFilename());
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
