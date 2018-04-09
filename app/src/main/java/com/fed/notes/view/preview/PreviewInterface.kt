package com.fed.notes.view.preview

import com.fed.notes.database.Entity.Note
import java.io.File
import java.util.*


interface PreviewInterface {
    fun showNote(note: Note, photoFile: File?)

    fun sendEmail(photoFile: File?, note: Note)

    fun showEditNoteFragment(note: Note)

    fun showDeleteNoteDialog()

    fun removeNoteFromOrder(noteId: UUID)

    fun closeFragment()

    fun onBackPressed()

}