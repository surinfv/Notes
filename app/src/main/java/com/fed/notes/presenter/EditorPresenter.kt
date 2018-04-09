package com.fed.notes.presenter

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.fed.notes.App
import com.fed.notes.database.DbHelper
import com.fed.notes.database.Entity.Note
import com.fed.notes.utils.getUri
import com.fed.notes.view.edit.NoteEditorFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*
import javax.inject.Inject

class EditorPresenter {

    @Inject
    lateinit var dbHelper: DbHelper

    private var fragment: NoteEditorFragment? = null
    private lateinit var noteId: UUID
    private lateinit var note: Note
    private var photoFile: File? = null
    private var uriPhotoFile: Uri? = null

    fun init(noteId: UUID) {
        App.component?.inject(this)
        this.noteId = noteId
    }

    fun attach(view: NoteEditorFragment) {
        fragment = view
    }

    fun detach() {
        fragment = null
    }

    fun onViewResumed(context: Context) {
        dbHelper.getNoteRx(noteId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    note = it
                    photoFile = dbHelper.getPhotoFile(note)
                    uriPhotoFile = getUri(context, photoFile)
                    setNote()
                }, Throwable::printStackTrace)
    }

    fun onViewStop() {
        //fixme - close fragment after chain ends
        if (!noteEmpty()) {
            saveNote()
        } else {
            removeNote()
        }
    }

    fun onDescriptionChanges(string: String) {
        note.description = string
    }

    fun onTitleChanges(string: String) {
        note.title = string
    }

    fun photoFabClicked() {
        fragment?.showPhotoDialog(photoFile?.exists() == true)
    }

    fun newPhotoButtonClicked() {
        fragment?.doPhotoIntent(uriPhotoFile)
    }

    fun choosePhotoButtonClicked() {
        fragment?.choosePhotoIntent()
    }

    fun onTakePhotoResult() {
        fragment?.updatePhotoView(photoFile)
    }

    fun onChoosePhotoResult(date: Intent?) {
        fragment?.savePictureToFile(date, photoFile)
        fragment?.updatePhotoView(photoFile)
    }

    fun photoDeleteButtonClicked() {
        fragment?.deletePhotoDialog()
    }

    fun removePhotoClicked() {
        photoFile?.delete()
        fragment?.updatePhotoView(photoFile)
    }

    private fun setNote() {
        fragment?.showTextFromNote(note)
        fragment?.setTextListeners()
        fragment?.initFab()
        fragment?.updatePhotoView(photoFile)
    }

    private fun saveNote() {
        dbHelper.insertRx(note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ }, Throwable::printStackTrace)
    }

    private fun removeNote() {
        fragment?.removeNoteFromOrder(note.id)
        dbHelper.deleteRx(note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ }, Throwable::printStackTrace)
    }

    private fun noteEmpty(): Boolean {
        val photoFileIsEmpty = !(photoFile?.exists() ?: false)
        val descriptionEmpty = note.description.isNullOrBlank()
        val titleEmpty = note.title.isNullOrBlank()
        return photoFileIsEmpty && descriptionEmpty && titleEmpty
    }
}