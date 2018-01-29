package com.fed.notes.view

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ShareCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fed.notes.App
import com.fed.notes.R
import com.fed.notes.database.DbHelper
import com.fed.notes.database.Note
import com.fed.notes.utils.*
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_note_preview.*
import java.io.File
import java.util.*
import javax.inject.Inject

class NotePreviewFragment : Fragment() {
    companion object {
        private val ARGS_NOTE_ID = "args_note_id"

        fun newInstance(id: UUID): NotePreviewFragment {
            val args = Bundle()
            args.putSerializable(ARGS_NOTE_ID, id)

            val noteFragment = NotePreviewFragment()
            noteFragment.arguments = args
            return noteFragment
        }
    }

    @Inject
    lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component?.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_note_preview)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteID = arguments.getSerializable(ARGS_NOTE_ID) as UUID

        val note = dbHelper.getNote(noteID)
        if (note == null) {
            activity.supportFragmentManager.popBackStack()
        } else {
            showNote(note)
        }

//        dbHelper.getNoteRx(noteID)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    note = it
//                    showNote()
//                }, Throwable::printStackTrace)
    }

    override fun onResume() {
        super.onResume()
        fab_menu.collapse()
    }

    private fun showNote(note: Note) {
        val photoFile = dbHelper.getPhotoFile(note)

        showTextFromNote(note)
        initFabs(photoFile, note)
        updatePhotoView(photoFile)
    }

    private fun initFabs(photoFile: File?, note: Note) {
        fab_send.setIcon(R.drawable.ic_send_email)
        fab_send.setOnClickListener { onSendEmailClicked(photoFile, note) }

        fab_edit.setIcon(R.drawable.ic_edit_mode)
        fab_edit.setOnClickListener { editNoteFragment(note) }

        fab_delete.setIcon(R.drawable.ic_delete_note)
        fab_delete.setOnClickListener { deleteNoteDialog(note) }
    }

    private fun showTextFromNote(note: Note) {
        toolbar.title = note.title

        note.title?.let {
            note_title_text_view.visibility = View.VISIBLE
            note_title_text_view.text = note.title
        }

        note.description?.let {
            note_description_text_view.visibility = View.VISIBLE
            note_description_text_view.text = note.description
        }

        note_date_text_view.showDate(context, note.date)
    }

    private fun updatePhotoView(photoFile: File?) {
        if (photoFile!!.exists()) {
            val bitmap = PictureUtils.getScaledBitmap(photoFile.path, activity)
            note_photo_image_view.setImageBitmap(bitmap)
            note_photo_image_view.setOnClickListener { showPhotoDialog(photoFile.path) }
            appbar_layout.visibility = View.VISIBLE
            note_title_text_view.visibility = View.GONE
        } else {
            appbar_layout.visibility = View.GONE
            note_title_text_view.visibility = View.VISIBLE
        }
    }

    private fun showPhotoDialog(path: String) {
        val dialog = ImageDialog.newInstance(path)
        dialog.show(fragmentManager, "IMAGE_FULL")
    }

    private fun onSendEmailClicked(photoFile: File?, note: Note) {

        val intent: Intent = if (photoFile?.exists() == true) {
            getImageIntent(photoFile, note)
        } else {
            getTextIntent(note)
        }

        if (intent.resolveActivity(activity.packageManager) != null) {
            startActivity(intent)
        } else {
            showEmailDialog()
        }
    }

    private fun getImageIntent(photoFile: File?, note: Note): Intent {
        val uriPhotoFile = getUri(context, photoFile)
        return ShareCompat.IntentBuilder.from(activity)
                .setType("plain/text")
                .setSubject(resources.getString(R.string.email_text) + note.title)
                .setText(note.description)
                .setStream(uriPhotoFile)
                .intent
    }

    private fun getTextIntent(note: Note): Intent {
        return ShareCompat.IntentBuilder.from(activity)
                .setType("plain/text")
                .setSubject(resources.getString(R.string.email_text) + note.title)
                .setText(note.description)
                .intent
    }

    private fun showEmailDialog() {
        AlertDialog.Builder(activity).apply {
            setTitle(R.string.email_intent_title)
            setMessage(R.string.email_intent_text)
            setNeutralButton(R.string.ok_button, null)
        }.show()
    }

    private fun editNoteFragment(note: Note) = (activity as MainActivity).openNoteFragmentEditor(note)

    private fun deleteNoteDialog(note: Note) {
        AlertDialog.Builder(activity).apply {
            setTitle(R.string.alert_on_del_title)
            setMessage(R.string.alert_on_del_text)
            setPositiveButton(R.string.alert_on_del_yes) { _, _ -> onDeleteClicked(note) }
            setNeutralButton(R.string.alert_on_del_cancel, null)
        }.show()
    }

    private fun onDeleteClicked(note: Note) {
        removeNoteFromOrderList(note.id, context)
        dbHelper.deleteRx(note)
                .subscribeOn(Schedulers.io())
                .subscribe({},
                        Throwable::printStackTrace)
        activity.onBackPressed()
    }
}