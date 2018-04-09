package com.fed.notes.view.preview

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ShareCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fed.notes.R
import com.fed.notes.database.Note
import com.fed.notes.presenter.PreviewPresenter
import com.fed.notes.utils.*
import com.fed.notes.view.MainActivity
import kotlinx.android.synthetic.main.fragment_note_preview.*
import java.io.File
import java.util.*


class NotePreviewFragment : Fragment(), PreviewInterface {
    companion object {
        private const val ARGS_NOTE_ID = "args_note_id"

        fun newInstance(id: UUID): NotePreviewFragment {
            val args = Bundle()
            args.putSerializable(ARGS_NOTE_ID, id)

            val noteFragment = NotePreviewFragment()
            noteFragment.arguments = args
            return noteFragment
        }
    }

    private lateinit var presenter: PreviewPresenter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_note_preview)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter = PreviewPresenter()
        presenter.init(arguments.getSerializable(ARGS_NOTE_ID) as UUID)

    }

    override fun onResume() {
        super.onResume()
        presenter.attach(this)
        presenter.onViewResumed()
        fab_menu.collapse()
    }

    override fun onStop() {
        super.onStop()
        presenter.detach()
    }

    override fun showNote(note: Note, photoFile: File?) {
        showTextFromNote(note)
        initFabs()
        updatePhotoView(photoFile)
    }

    override fun sendEmail(photoFile: File?, note: Note) {

        val intent: Intent = if (photoFile?.exists() == true) {
            getImageIntent(photoFile, note)
        } else {
            getTextIntent(note)
        }

        if (intent.resolveActivity(activity.packageManager) != null) {
            startActivity(intent)
        } else {
            showEmailWarningDialog()
        }
    }

    override fun showEditNoteFragment(note: Note) = (activity as MainActivity).openNoteFragmentEditor(note)

    override fun showDeleteNoteDialog() {
        AlertDialog.Builder(activity).apply {
            setTitle(R.string.alert_on_del_title)
            setMessage(R.string.alert_on_del_text)
            setPositiveButton(R.string.alert_on_del_yes) { _, _ -> presenter.noteDeleteClicked() }
            setNeutralButton(R.string.alert_on_del_cancel, null)
        }.show()
    }

    override fun removeNoteFromOrder(noteId: UUID) {
        removeNoteFromOrderList(noteId, context)
    }

    override fun closeFragment() = activity.supportFragmentManager.popBackStack()

    override fun onBackPressed() = activity.onBackPressed()

    private fun initFabs() {
        fab_send.setIcon(R.drawable.ic_send_email)
        fab_send.setOnClickListener { presenter.sendEmailFabClicked() }

        fab_edit.setIcon(R.drawable.ic_edit_mode)
        fab_edit.setOnClickListener { presenter.editFabClicked() }

        fab_delete.setIcon(R.drawable.ic_delete_note)
        fab_delete.setOnClickListener { presenter.deleteFabClicked() }
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
        if (photoFile?.exists() == true) {
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

    private fun showEmailWarningDialog() {
        AlertDialog.Builder(activity).apply {
            setTitle(R.string.email_intent_title)
            setMessage(R.string.email_intent_text)
            setNeutralButton(R.string.ok_button, null)
        }.show()
    }
}