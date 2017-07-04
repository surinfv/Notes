package com.fed.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.fed.notes.database.NoteBaseHelper;
import com.fed.notes.database.NoteCursorWrapper;
import com.fed.notes.database.NoteDbSchema.NoteTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by f on 10.05.2017.
 */

class NoteBook {
    private static NoteBook sNoteBook;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static NoteBook get(Context context) {
        if (sNoteBook == null) sNoteBook = new NoteBook(context);
        return sNoteBook;
    }

    private NoteBook(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new NoteBaseHelper(mContext).getWritableDatabase();

        //тестовые записи
//        for (int i = 0; i < 10; i++) {
//            Note note = new Note();
//            note.setTitle("Test Note # " + i);
//            note.setDescription("This is a long multiple string for describe the note, and repeat this string one more time - This is a long multiple string for describe the note, and repeat this string one more time " + i);
//            mNotes.add(0, note);
//        }
    }

    public void addNote(Note note) {
        ContentValues values = getContentValues(note);
        mDatabase.insert(NoteTable.TABLE_NAME, null, values);
    }

    public void deleteNote(Note note) {
        mDatabase.delete(NoteTable.TABLE_NAME,
                NoteTable.Columns.UUID + "= ?",
                new String[]{note.getId().toString()});
        getPhotoFile(note).delete();// попробовал удалить фото при удалении заметки

        //удаление из orderList
        UUID id = note.getId();
    }

    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<>();

        NoteCursorWrapper cursor = queryNotes(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                notes.add(cursor.getNote());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return notes;
    }

    public Note getNote(UUID id) {
        NoteCursorWrapper cursor = queryNotes(
                NoteTable.Columns.UUID + " = ?",
                new String[] { id.toString()}
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getNote();
        } finally {
            cursor.close();
        }
    }

    public void updateNote(Note note) {
        String uuidString = note.getId().toString();
        ContentValues values = getContentValues(note);
        mDatabase.update(NoteTable.TABLE_NAME, values, NoteTable.Columns.UUID + " = ?", new String[]{uuidString});
    }

    private static ContentValues getContentValues(Note note) {
        ContentValues values = new ContentValues();
        values.put(NoteTable.Columns.UUID, note.getId().toString());
        values.put(NoteTable.Columns.TITLE, note.getTitle());
        values.put(NoteTable.Columns.DESCRIPTION, note.getDescription());
        values.put(NoteTable.Columns.DATE, note.getDate().getTime());
        return values;
    }

    private NoteCursorWrapper queryNotes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                NoteTable.TABLE_NAME,
                null, // columns. null - all
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having by
                NoteTable.Columns.DATE + " DESC" // order by
        );
        return new NoteCursorWrapper(cursor);
    }

    public File getPhotoFile(Note note) {
        //проверяем доступность внешнего хранилища, если не доступно то пользуемся внутренним
        File externalFileDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            externalFileDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            externalFileDir = mContext.getFilesDir();
        }

        if (externalFileDir == null) return null;

        return new File(externalFileDir, note.getPhotoFilename());
    }

//    public int getPosition(Note note) {
//        for (int i = 0, p = mNotes.size(); i < p; i++) {
//            if (note.equals(mNotes.get(i))) return i;
//        }
//        return 0;//bad deal
//    }
}
