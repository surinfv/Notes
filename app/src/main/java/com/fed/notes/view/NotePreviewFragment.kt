package com.fed.notes.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fed.notes.App
import com.fed.notes.BuildConfig
import com.fed.notes.R
import com.fed.notes.database.DbHelper
import com.fed.notes.database.Note
import com.fed.notes.utils.ImageDialog
import com.fed.notes.utils.NotesOrderUtil
import com.fed.notes.utils.PictureUtils
import com.fed.notes.utils.inflate
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_note_preview.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by Fedor SURIN on 05.05.2017.
 */

class NotePreviewFragment : Fragment() {
    companion object {
        private val ARGS_NOTE_ID = "argsnoteid"

        fun newInstance(id: UUID): NotePreviewFragment {
            val args = Bundle()
            args.putSerializable(ARGS_NOTE_ID, id)

            val noteFragment = NotePreviewFragment()
            noteFragment.arguments = args
            return noteFragment
        }
    }

    private var note: Note? = null
    private var photoFile: File? = null
    private var uriPhotoFile: Uri? = null

    lateinit private var noteID: UUID
    @Inject
    lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.getComponent().inject(this)
        noteID = arguments.getSerializable(ARGS_NOTE_ID) as UUID
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_note_preview)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        note = dbHelper.getNote(noteID)
        noteLoaded()

//        dbHelper.getNoteRx(noteID)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    note = it
//                    noteLoaded()
//                }, Throwable::printStackTrace)
    }

    private fun noteLoaded() {
        if (note != null) {
            initFabs()
            updateInfo()
            updatePhotoView()
        } else {
            activity.supportFragmentManager.popBackStack()
        }
    }

    private fun initFabs() {
        fab_send.setIcon(R.drawable.ic_send_email)
        fab_send.setOnClickListener { sendEmailDialog() }

        fab_edit.setIcon(R.drawable.ic_edit_mode)
        fab_edit.setOnClickListener { editNoteFragment() }

        fab_delete.setIcon(R.drawable.ic_delete_note)
        fab_delete.setOnClickListener { deleteNoteDialog() }
    }

    private fun updateInfo() {
        photoFile = dbHelper.getPhotoFile(note)
        uriPhotoFile = if (Build.VERSION.SDK_INT > 23) {
            FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", photoFile)
        } else {
            Uri.fromFile(photoFile)
        }

        toolbar.title = note?.title
        if (note?.title != null) {
            note_title_text_view.visibility = View.VISIBLE
            note_title_text_view.text = note?.title
        } else {
            note_title_text_view.visibility = View.GONE
        }
        if (note?.description != null) {
            note_description_text_view.visibility = View.VISIBLE
            note_description_text_view.text = note?.description
        } else {
            note_description_text_view.visibility = View.GONE
        }
        val dateFormat = SimpleDateFormat(getString(R.string.date_format), Locale.ENGLISH)
        note_date_text_view.text = dateFormat.format(note?.date)
    }

    private fun updatePhotoView() {
        if (photoFile!!.exists()) {
            val bitmap = PictureUtils.getScaledBitmap(photoFile?.path, activity)
            note_photo_image_view.setImageBitmap(bitmap)
            note_photo_image_view.setOnClickListener { showPhotoDialog() }
            appbar_layout.visibility = View.VISIBLE
            note_title_text_view.visibility = View.GONE
        } else {
            appbar_layout.visibility = View.GONE
            note_title_text_view.visibility = View.VISIBLE
        }
    }

    private fun showPhotoDialog() {
        val manager = fragmentManager
        val dialog = ImageDialog.newInstance(photoFile?.path)
        dialog.show(manager, "IMAGE_FULL")
    }

    private fun sendEmailDialog() {
        val intent: Intent = if (photoFile!!.exists()) {
            ShareCompat.IntentBuilder.from(activity)
                    .setType("plain/text")
                    //.setType("image/*")
                    .setSubject(resources.getString(R.string.email_text) + note?.title)
                    .setText(note?.description)
                    .setStream(uriPhotoFile)
                    .intent
        } else {
            ShareCompat.IntentBuilder.from(activity)
                    .setType("plain/text")
                    .setSubject(resources.getString(R.string.email_text) + note?.title)
                    .setText(note?.description)
                    .intent
        }
        if (intent.resolveActivity(activity.packageManager) != null) {
            startActivity(intent)
//            startActivity(Intent.createChooser(intent, "send via..."));
        } else {
            val eMailIntentAlertDialog = AlertDialog.Builder(activity)
            eMailIntentAlertDialog.setTitle(R.string.email_intent_title)
            eMailIntentAlertDialog.setMessage(R.string.email_intent_text)
            eMailIntentAlertDialog.setNeutralButton(R.string.ok_button) { dialog, which -> }
            eMailIntentAlertDialog.show()
        }
    }

    private fun editNoteFragment() {
        (activity as MainActivity).openNoteFragmentEditor(note!!)
    }

    private fun deleteNoteDialog() {
        val deleteAlertDialog = AlertDialog.Builder(activity)
        deleteAlertDialog.setTitle(R.string.alert_on_del_title)
        deleteAlertDialog.setMessage(R.string.alert_on_del_text)
        deleteAlertDialog.setPositiveButton(R.string.alert_on_del_yes) { dialog, which ->
            NotesOrderUtil.removeNoteFromOrderList(note!!.id, context)
            dbHelper.deleteRx(note)
                    .subscribeOn(Schedulers.io())
                    .subscribe({},
                            Throwable::printStackTrace)
            activity.onBackPressed()
        }
        deleteAlertDialog.setNeutralButton(R.string.alert_on_del_cancel) { dialog, which -> }
        deleteAlertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        fab_menu.collapse()
    }
}