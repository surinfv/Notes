package com.fed.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.fed.notes.database.NoteBaseHelper;
import com.fed.notes.database.NoteDbSchema;
import com.fed.notes.database.NoteDbSchema.NoteTable;

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
    }

    public List<Note> getNotes() {
    }

    public Note getNote(UUID id) {
        return null;
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
        values.put(NoteTable.Columns.DATE, note.getDate().toString());
        return values;
    }

//    public int getPosition(Note note) {
//        for (int i = 0, p = mNotes.size(); i < p; i++) {
//            if (note.equals(mNotes.get(i))) return i;
//        }
//        return 0;//bad deal
//    }
}
