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
        App.getComponent().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_note_preview)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteID = arguments.getSerializable(ARGS_NOTE_ID) as UUID

        val note = dbHelper.getNote(noteID)
        if (note == null) {
            // Я бы советовал вообще не открывать этот фрагмент, если такой заметки нет, ибо это оч странно
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

    // Методы в классе обычно распологают по степени открытости, паблики, потом протектед, потом приватные.
    // ЖЦ обычно вверху, но это не строго уже, а дело вкуса. Лично мне так удобнее читать
    override fun onResume() {
        super.onResume()
        fab_menu.collapse()
    }

    private fun showNote(note: Note) {
        val photoFile = dbHelper.getPhotoFile(note)

        showTextFromNote(note)
        initFabs(photoFile, note)
        updateInfo(photoFile)

    }

    private fun initFabs(photoFile: File?, note: Note) {
        fab_send.setIcon(R.drawable.ic_send_email)
        fab_send.setOnClickListener { onSendEmailClicked(photoFile, note) }

        fab_edit.setIcon(R.drawable.ic_edit_mode)
        fab_edit.setOnClickListener { editNoteFragment(note) }

        fab_delete.setIcon(R.drawable.ic_delete_note)
        fab_delete.setOnClickListener { deleteNoteDialog(note) }
    }

    private fun updateInfo(photoFile: File?) {
        updatePhotoView(photoFile)
    }

    private fun showTextFromNote(note: Note) {
        toolbar.title = note.title

        note.title?.let {
            // по дефолту невидимые, если надо - показываешь
            note_title_text_view.visibility = View.VISIBLE
            note_title_text_view.text = note.title
        }

        note.description?.let {
            note_description_text_view.visibility = View.VISIBLE
            note_description_text_view.text = note.description
        }

        note_date_text_view.showDate(context, note.date) // Это я просто подвыебнуться :)
        // что бы наглядно показать как экстеншны можно заюзать.
        // Удобно, если у тя такой код повторяется, для одного раза конечно не надо
    }

    private fun updatePhotoView(photoFile: File?) {
        if (photoFile!!.exists()) {
            val bitmap = PictureUtils.getScaledBitmap(photoFile?.path, activity)
            note_photo_image_view.setImageBitmap(bitmap)
            note_photo_image_view.setOnClickListener { showPhotoDialog(photoFile?.path) }
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

        val uriPhotoFile = if (Build.VERSION.SDK_INT > 23) { //я бы вынес в экстеншн, или утилку какую-нить
            FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", photoFile)
        } else {
            Uri.fromFile(photoFile)
        }

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

    private fun editNoteFragment(note: Note) {
        (activity as MainActivity).openNoteFragmentEditor(note)
    }

    private fun deleteNoteDialog(note: Note) {
        val deleteAlertDialog = AlertDialog.Builder(activity).apply {

            setTitle(R.string.alert_on_del_title)
            setMessage(R.string.alert_on_del_text)
            setPositiveButton(R.string.alert_on_del_yes) { _, _ -> onDeleteClicked(note) }
            setNeutralButton(R.string.alert_on_del_cancel, null)
        }
        deleteAlertDialog.show()
    }

    private fun onDeleteClicked(note: Note) {
        NotesOrderUtil.removeNoteFromOrderList(note.id, context)
        dbHelper.deleteRx(note)
                .subscribeOn(Schedulers.io())
                .subscribe({},
                        Throwable::printStackTrace)
        activity.onBackPressed()
    }
}