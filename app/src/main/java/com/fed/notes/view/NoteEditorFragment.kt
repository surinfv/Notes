package com.fed.notes.view

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
import com.fed.notes.App
import com.fed.notes.R
import com.fed.notes.database.DbHelper
import com.fed.notes.database.Note
import com.fed.notes.utils.*
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_note_editor.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject

class NoteEditorFragment : Fragment() {
    companion object {
        private val ARGS_NOTE_ID = "args_note_id"
        private val REQUEST_PHOTO_CAM = 0
        private val REQUEST_PHOTO_GAL = 1

        fun newInstance(id: UUID): NoteEditorFragment {
            val args = Bundle()
            args.putSerializable(ARGS_NOTE_ID, id)

            val noteFragment = NoteEditorFragment()
            noteFragment.arguments = args
            return noteFragment
        }
    }

    private var note: Note? = null
    private var photoFile: File? = null
    private var canTakePhoto: Boolean = false
    private var capturePhotoIntent: Intent? = null
    private var uriPhotoFile: Uri? = null
    lateinit private var disposable: CompositeDisposable

    @Inject
    lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.getComponent().inject(this)
        val noteID = arguments.getSerializable(ARGS_NOTE_ID) as UUID

        note = dbHelper.getNote(noteID)
        photoFile = dbHelper.getPhotoFile(note)
        uriPhotoFile = UriFetcherUtil.getUri(context, photoFile)

        //can take photo check:
        val packageManager = activity.packageManager
        capturePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        canTakePhoto = photoFile != null && capturePhotoIntent?.resolveActivity(packageManager) != null
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_note_editor)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showTextFromNote()
        setTextListeners()
        initFab()
        updatePhotoView()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
        /**
         * Вообще не подходящее место для такой операции.
         * Поскольку у тебя запись идет асинхронно - может получиться так, что ты откроешь предыдущий фрагмент
         * еще до того, как данные запишутся\удалятся.
         * Я бы посоветовал "контролировать" все выходы с экрана, а именно onBackPressed и нажатие на завершающий контрол
         * Можно выводить диалог для подтверждения выхода по onBackPressed()
         * Если в диалоге подтвердили изменения - то покрутить какой нить прогресс, а закрывать уже из .subscribe({ тут }
         *
         * И в случае удаления откатывать стек фраментов сразу на список, без попадания на фрагмент редактирования
         *
         */
        if (!noteEmpty()) {
            dbHelper.insertRx(note!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({},
                            Throwable::printStackTrace)
        } else {
            removeNote()
        }
    }

    private fun showTextFromNote() {
        note_title_text_view.setText(note?.title)
        note_description_text_view.setText(note?.description)
        note_date_text_view.showDate(context, note!!.date)
    }

    private fun setTextListeners() {
        disposable = CompositeDisposable()
        disposable.addAll(
                RxTextView.textChanges(note_description_text_view)
                        .subscribe { it -> note!!.description = it.toString() }
                ,
                RxTextView.textChanges(note_title_text_view)
                        .subscribe { it -> note!!.title = it.toString() }
        )
    }

    private fun initFab() {
        fab_photo.setIcon(R.drawable.ic_take_photo)
        fab_photo.setOnClickListener { onTakePhotoClicked() }
        if (!canTakePhoto) fab_photo.visibility = View.GONE
    }

    private fun onTakePhotoClicked() {
        val photoAlertDialog = AlertDialog.Builder(activity)
        photoAlertDialog.setTitle(R.string.alert_on_photo_title)
                .setPositiveButton(R.string.alert_on_photo_cam) { _, _ -> doPhotoIntent() }
                .setNegativeButton(R.string.alert_on_photo_gallery) { _, _ -> choosePhotoIntent() }

        if (!photoFile!!.exists()) {
            photoAlertDialog.setMessage(R.string.alert_on_photo_text_first_photo)
        } else {
            photoAlertDialog.setMessage(R.string.alert_on_photo_text_second_photo)
            photoAlertDialog.setNeutralButton(R.string.alert_on_photo_remove) { _, _ -> deletePhotoDialog() }
        }
        photoAlertDialog.show()
    }

    private fun doPhotoIntent() {
        capturePhotoIntent!!.putExtra(MediaStore.EXTRA_OUTPUT, uriPhotoFile)
        startActivityForResult(capturePhotoIntent, REQUEST_PHOTO_CAM)
    }

    private fun choosePhotoIntent() {
        val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, REQUEST_PHOTO_GAL)
    }

    private fun deletePhotoDialog() {
        AlertDialog.Builder(activity).apply {
            setTitle(R.string.alert_del_photo_title)
            setMessage(R.string.alert_del_photo_text)
            setPositiveButton(R.string.alert_del_photo_yes) { _, _ ->
                photoFile!!.delete()
                updatePhotoView()
            }
            setNegativeButton(R.string.alert_del_photo_no) { _, _ -> }
        }.show()
    }

    private fun updatePhotoView() {
        if (photoFile!!.exists()) {
            val bitmap = PictureUtils.getScaledBitmap(photoFile?.path, activity)
            note_photo_image_view.setImageBitmap(bitmap)
            note_photo_image_view.visibility = View.VISIBLE
            note_photo_image_view.setOnClickListener { showFullScreenPhotoDialog() }
        } else {
            note_photo_image_view.visibility = View.GONE
        }
    }

    private fun showFullScreenPhotoDialog() {
        val manager = fragmentManager
        val dialog = ImageDialog.newInstance(photoFile?.path)
        dialog.show(manager, "IMAGE_FULL")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_PHOTO_CAM -> updatePhotoView()
            REQUEST_PHOTO_GAL -> {
                savePictureToFile(data)
                updatePhotoView()
            }
        }
    }

    private fun savePictureToFile(data: Intent?) {
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

    private fun noteEmpty(): Boolean {
        val photoFileIsEmpty = !photoFile!!.exists()
        val descriptionEmpty = note?.description.isNullOrBlank()
        val titleEmpty = note?.title.isNullOrBlank()
        return photoFileIsEmpty && descriptionEmpty && titleEmpty
    }

    private fun removeNote() {
        NotesOrderUtil.removeNoteFromOrderList(note!!.id, context)
        dbHelper.delete(note!!)
    }
}
