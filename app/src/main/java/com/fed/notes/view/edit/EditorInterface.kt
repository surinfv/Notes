package com.fed.notes.view.edit

import android.content.Intent
import android.net.Uri
import com.fed.notes.database.Entity.Note
import java.io.File
import java.util.*

interface EditorInterface {
    fun showTextFromNote(note: Note)

    fun setTextListeners()

    fun initFab()

    fun showPhotoDialog(isPhotoFileExist : Boolean)

    fun doPhotoIntent(uriPhotoFile: Uri?)

    fun choosePhotoIntent()

    fun deletePhotoDialog()

    fun updatePhotoView(photoFile : File?)

    fun savePictureToFile(data: Intent?, photoFile: File?)

    fun removeNoteFromOrder(noteId: UUID)
}