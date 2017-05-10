package com.fed.notes;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by f on 10.05.2017.
 */

public class NoteBook {
    private static NoteBook sNoteBook;
    private List<Note> mNotes;

    public static NoteBook get(Context context){
        if (sNoteBook == null) return new NoteBook(context);
        return sNoteBook;
    }

    private NoteBook(Context context){
        mNotes = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Note note = new Note();
            note.setTitle("Title # " + i);
            note.setDescription("This is a long multiple string for describe the note, and repeat this string one more time - This is a long multiple string for describe the note, and repeat this string one more time " + i);
            mNotes.add(note);
        }
    }

    public List<Note> getNotes(){
        return mNotes;
    }

    public Note getNote(UUID id) {
        for (Note note : mNotes){
            if (note.getId().equals(id)) return note;
        }
        return null;
    }
}
