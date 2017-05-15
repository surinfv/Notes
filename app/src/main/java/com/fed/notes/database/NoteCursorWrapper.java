package com.fed.notes.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.fed.notes.Note;
import com.fed.notes.database.NoteDbSchema.NoteTable;

import java.util.UUID;

/**
 * Created by f on 15.05.2017.
 */

public class NoteCursorWrapper extends CursorWrapper {

    public NoteCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Note getNote() {
        String uuidString = getString(getColumnIndex(NoteTable.Columns.UUID));
        String title = getString(getColumnIndex(NoteTable.Columns.TITLE));
        String description = getString(getColumnIndex(NoteTable.Columns.DESCRIPTION));
        long date = getLong(getColumnIndex(NoteTable.Columns.DATE));

        Note note = new Note(UUID.fromString(uuidString));
        note.setTitle(title);
        note.setDescription(description);
        note.setDate(date);

        return note;
    }
}
