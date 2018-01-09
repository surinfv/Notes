package com.fed.notes.database

import android.os.Environment
import com.fed.notes.App
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.util.*

class DbHelper(private val noteDAO: NoteDAO) {

    fun getNote(uuid: UUID?): Note? = noteDAO.getNote(uuid)

    fun getNoteRx(uuid: UUID): Single<Note> = noteDAO.getNoteRx(uuid)

    fun insert(note: Note) = noteDAO.insert(note)

    fun insertRx(note: Note): Completable = Completable.fromAction { noteDAO.insert(note) }

    fun delete(note: Note) {
        getPhotoFile(note)?.delete()
        noteDAO.delete(note)
    }

    fun deleteRx(note: Note?): Completable = Completable.fromAction {
        getPhotoFile(note)?.delete()
        noteDAO.delete(note)
    }

    fun getPhotoFile(note: Note?): File? {
        val externalFileDir: File? = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            App.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        } else {
            App.getInstance().filesDir
        }
        return if (externalFileDir == null) null else File(externalFileDir, note?.getPhotoFilename())
    }
}
