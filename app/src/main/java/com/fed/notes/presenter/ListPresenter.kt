package com.fed.notes.presenter

import com.fed.notes.App
import com.fed.notes.database.DbHelper
import com.fed.notes.database.Entity.Note
import com.fed.notes.view.list.ListInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ListPresenter {

    @Inject
    lateinit var dbHelper: DbHelper
    private var fragment: ListInterface? = null


    fun init() {
        App.component?.inject(this)
    }

    fun attach(view: ListInterface) {
        fragment = view
    }

    fun detach() {
        fragment = null
    }

    fun createNewNoteClicked() {
        val note = Note()

        fragment?.addNoteToOrder(note.id)
        dbHelper.insertRx(note)
                .subscribeOn(Schedulers.io())
                .subscribe({ fragment?.openNoteEditorFragment(note) },
                        Throwable::printStackTrace)
    }

    fun deleteNote(note: Note) {
        dbHelper.deleteRx(note)
                .subscribeOn(Schedulers.io())
                .subscribe({},
                        Throwable::printStackTrace)
    }

    fun updateUI(notesOrder: MutableList<UUID>) {
        dbHelper.getNotesRx(notesOrder)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    fragment?.setNotes(sortNotesById(it, notesOrder))
                }, Throwable::printStackTrace)
    }

    private fun sortNotesById(list: List<Note>, notesOrder: MutableList<UUID>): ArrayList<Note> {
        val sortedList = ArrayList<Note>()
        for (id in notesOrder) {
            for (note in list) {
                if (note.id == id) {
                    sortedList.add(note)
                }
            }
        }
        return sortedList
    }

    fun onViewPause() {
        fragment?.saveListOrder()
    }
}