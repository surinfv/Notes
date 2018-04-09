package com.fed.notes.view.list

import com.fed.notes.database.Entity.Note
import java.util.*

interface ListInterface {
    fun setNotes(notes: ArrayList<Note>)

    fun openNoteEditorFragment(note: Note)

    fun addNoteToOrder(noteId: UUID)

    fun saveListOrder()
}