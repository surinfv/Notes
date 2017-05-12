package com.fed.notes;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by f on 10.05.2017.
 */

 class NoteBook {
    private static NoteBook sNoteBook;
    private List<Note> mNotes;

    public static NoteBook get(Context context){
        if (sNoteBook == null) sNoteBook = new NoteBook(context);
        return sNoteBook;
    }

    private NoteBook(Context context){
        mNotes = new ArrayList<>();

        //тестовые записи
        for (int i = 0; i < 10; i++) {
            Note note = new Note();
            note.setTitle("Test Note # " + i);
            note.setDescription("This is a long multiple string for describe the note, and repeat this string one more time - This is a long multiple string for describe the note, and repeat this string one more time " + i);
            mNotes.add(0, note);
        }
    }

    public void addNote(Note note) {
        mNotes.add(0, note);
    }

    public void deleteNote(Note note) {
        mNotes.remove(note);
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

    public int getPosition(Note note) {
        for (int i = 0, p = mNotes.size(); i < p; i++) {
            if (note.equals(mNotes.get(i))) return i;
        }
        return 0;//bad deal
    }
}
