package com.fed.notes.view.edit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fed.notes.R
import com.fed.notes.database.Entity.Note
import com.fed.notes.presenter.EditorPresenter
import com.fed.notes.utils.*
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_note_editor.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

class NoteEditorFragment : Fragment(), EditorInterface {
    companion object {
        private const val ARGS_NOTE_ID = "args_note_id"
        private const val REQUEST_PHOTO_CAM = 0
        private const val REQUEST_PHOTO_GAL = 1

        fun newInstance(id: UUID): NoteEditorFragment {
            val args = Bundle()
            args.putSerializable(ARGS_NOTE_ID, id)

            val noteFragment = NoteEditorFragment()
            noteFragment.arguments = args
            return noteFragment
        }
    }

    private lateinit var presenter: EditorPresenter

    private var capturePhotoIntent: Intent? = null
    private lateinit var disposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = EditorPresenter()
        presenter.init(arguments.getSerializable(ARGS_NOTE_ID) as UUID)

//        val uriPhotoFile = getUri(context, photoFile)

//
        //can take photo check:
//        val packageManager = activity.packageManager
        capturePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        canTakePhoto = photoFile != null && capturePhotoIntent?.resolveActivity(packageManager) != null
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_note_editor)
    }

    override fun onResume() {
        super.onResume()
        presenter.attach(this)
        presenter.onViewResumed(context)
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
        presenter.onViewStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach()
    }

    override fun showTextFromNote(note: Note) {
        note_title_text_view.setText(note.title)
        note_description_text_view.setText(note.description)
        note_date_text_view.showDate(context, note.date)
    }

    override fun setTextListeners() {
        disposable = CompositeDisposable()
        disposable.addAll(
                RxTextView.textChanges(note_description_text_view)
                        .subscribe { presenter.onDescriptionChanges(it.toString()) }
                ,
                RxTextView.textChanges(note_title_text_view)
                        .subscribe { presenter.onTitleChanges(it.toString()) }
        )
    }

    override fun initFab() {
        fab_photo.setIcon(R.drawable.ic_take_photo)
        fab_photo.setOnClickListener { presenter.photoFabClicked() }
//        if (!canTakePhoto) fab_photo.visibility = View.GONE
    }

    override fun showPhotoDialog(isPhotoFileExist: Boolean) {
        val photoAlertDialog = AlertDialog.Builder(activity)
        photoAlertDialog.setTitle(R.string.alert_on_photo_title)
                .setPositiveButton(R.string.alert_on_photo_cam) { _, _ ->
                    presenter.newPhotoButtonClicked()
                }
                .setNegativeButton(R.string.alert_on_photo_gallery) { _, _ ->
                    presenter.choosePhotoButtonClicked()
                }

        if (!isPhotoFileExist) {
            photoAlertDialog.setMessage(R.string.alert_on_photo_text_first_photo)
        } else {
            photoAlertDialog.setMessage(R.string.alert_on_photo_text_second_photo)
            photoAlertDialog.setNeutralButton(R.string.alert_on_photo_remove) { _, _ ->
                presenter.photoDeleteButtonClicked()
            }
        }
        photoAlertDialog.show()
    }

    override fun doPhotoIntent(uriPhotoFile: Uri?) {
        capturePhotoIntent?.putExtra(MediaStore.EXTRA_OUTPUT, uriPhotoFile)
        startActivityForResult(capturePhotoIntent, REQUEST_PHOTO_CAM)
    }

    override fun choosePhotoIntent() {
        val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, REQUEST_PHOTO_GAL)
    }

    override fun deletePhotoDialog() {
        AlertDialog.Builder(activity).apply {
            setTitle(R.string.alert_del_photo_title)
            setMessage(R.string.alert_del_photo_text)
            setPositiveButton(R.string.alert_del_photo_yes) { _, _ -> presenter.removePhotoClicked() }
            setNegativeButton(R.string.alert_del_photo_no) { _, _ -> }
        }.show()
    }

    override fun updatePhotoView(photoFile: File?) {
        if (photoFile?.exists() == true) {
            val bitmap = PictureUtils.getScaledBitmap(photoFile.path, activity)
            note_photo_image_view.setImageBitmap(bitmap)
            note_photo_image_view.visibility = View.VISIBLE
            note_photo_image_view.setOnClickListener { showFullScreenPhotoDialog(photoFile) }
        } else {
            note_photo_image_view.visibility = View.GONE
        }
    }

    override fun savePictureToFile(data: Intent?, photoFile: File?) {
        val imgUri = data?.data
        val chunkSize = 1024
        val imageData = ByteArray(chunkSize)
        val inputStream = activity.contentResolver.openInputStream(imgUri)
        val outputStream = FileOutputStream(photoFile)

        inputStream.use {
            outputStream.use {
                while (true) {
                    val bytesRead = inputStream.read(imageData)
                    if (bytesRead > 0) {
                        outputStream.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)))
                    } else break
                }
            }
        }
    }

    override fun removeNoteFromOrder(noteId: UUID) {
        removeNoteFromOrderList(noteId, context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_PHOTO_CAM -> presenter.onTakePhotoResult()
            REQUEST_PHOTO_GAL -> presenter.onChoosePhotoResult(data)
        }
    }

    private fun showFullScreenPhotoDialog(photoFile: File) {
        val manager = fragmentManager
        val dialog = ImageDialog.newInstance(photoFile.path)
        dialog.show(manager, "IMAGE_FULL")
    }
}
