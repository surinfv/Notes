package com.fed.notes.database;

import android.os.Environment;

import com.fed.notes.App;

import java.io.File;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Fedor SURIN on 12.11.2017.
 */

public class DbHelper {
    private NoteDAO noteDAO;

    public DbHelper(AppDatabase appDatabase) {
        noteDAO = appDatabase.getNoteDao();
    }

    public Note getNote(UUID uuid) {
        return noteDAO.getNote(uuid);
    }

    public Single<Note> getNoteRx(final UUID uuid) {
        return Single.fromCallable(() -> noteDAO.getNote(uuid));
    }

    public void insert(Note note) {
        noteDAO.insert(note);
    }

    public Completable insertRx(Note note) {
        return Completable.fromAction(() -> noteDAO.insert(note));
    }

    public void delete(Note note) {
        noteDAO.delete(note);
    }

    public Completable deleteRx(Note note) {
        return Completable.fromAction(() -> {
            File photoFile = new File(note.getPhotoFilename());
            photoFile.delete();
            noteDAO.delete(note);
        });
    }

    public File getPhotoFile(Note note) {
        File externalFileDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            externalFileDir = App.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            externalFileDir = App.getInstance().getFilesDir();
        }

        if (externalFileDir == null) return null;

        return new File(externalFileDir, note.getPhotoFilename());
    }
}
