package com.fed.notes.presenter

import com.fed.notes.App
import com.fed.notes.database.DbHelper
import com.fed.notes.database.Note
import com.fed.notes.view.preview.PreviewInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*
import javax.inject.Inject

class PreviewPresenter {

    @Inject
    lateinit var dbHelper: DbHelper
    private var fragment: PreviewInterface? = null
    private var noteId: UUID? = null
    private var note: Note? = null
    private var photoFile: File? = null

    fun init(noteId: UUID?) {
        App.component?.inject(this)
        this.noteId = noteId
    }

    fun attach(view: PreviewInterface) {
        fragment = view
    }

    fun detach() {
        fragment = null
    }

    fun onViewResumed() {
        note = dbHelper.getNote(noteId)
        if (note == null) {
            fragment?.closeFragment()
        } else {
            photoFile = dbHelper.getPhotoFile(note)
            fragment?.showNote(note!!, photoFile)
        }
    }

    fun sendEmailFabClicked() {
        fragment?.sendEmail(photoFile, note!!)
    }

    fun editFabClicked() {
        fragment?.showEditNoteFragment(note!!)
    }

    fun deleteFabClicked() {
        fragment?.showDeleteNoteDialog()
    }

    fun noteDeleteClicked() {
        dbHelper.deleteRx(note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    fragment?.removeNoteFromOrder(note!!.id)
                    fragment?.onBackPressed()
                },
                        Throwable::printStackTrace)
    }
}