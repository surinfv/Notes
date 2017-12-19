package com.fed.notes.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.fed.notes.App
import com.fed.notes.BuildConfig
import com.fed.notes.R
import com.fed.notes.database.DbHelper
import com.fed.notes.database.Note
import com.fed.notes.utils.ImageDialog
import com.fed.notes.utils.NotesOrderUtil
import com.fed.notes.utils.PictureUtils
import com.fed.notes.utils.inflate
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_note_editor.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.System.`in`
import java.lang.System.out
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by f on 05.05.2017.
 */

class NoteEditorFragment : Fragment() {
    companion object {
        private val ARGS_NOTE_ID = "argsnoteid"
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

    @Inject
    lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.getComponent().inject(this)
        val noteID = arguments.getSerializable(ARGS_NOTE_ID) as UUID

        note = dbHelper.getNote(noteID)
        photoFile = dbHelper.getPhotoFile(note)
        uriPhotoFile = if (Build.VERSION.SDK_INT > 23) {
            FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", photoFile)
        } else {
            Uri.fromFile(photoFile)
        }

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

        note_title_text_view.setText(note?.title)
        note_title_text_view.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                note!!.title = s.toString()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        note_description_text_view.setText(note?.description)
        note_description_text_view.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                note!!.description = s.toString()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        val dateFormat = SimpleDateFormat(getString(R.string.date_format), Locale.ENGLISH)
        note_date_text_view.text = dateFormat.format(note?.date)

        fab_photo.setIcon(R.drawable.ic_take_photo)
        fab_photo.setOnClickListener { takePhotoDialog() }
        if (!canTakePhoto) fab_photo.visibility = View.GONE

        updatePhotoView()
        note_photo_image_view.setOnClickListener { showPhotoDialog() }
    }

    private fun showPhotoDialog() {
        val manager = fragmentManager
        val dialog = ImageDialog.newInstance(photoFile?.path)
        dialog.show(manager, "IMAGE_FULL")
    }

    private fun takePhotoDialog() {
        val photoAlertDialog = AlertDialog.Builder(activity)
        photoAlertDialog.setTitle(R.string.alert_on_photo_title)
                .setPositiveButton(R.string.alert_on_photo_cam) { dialog, which ->
                    capturePhotoIntent!!.putExtra(MediaStore.EXTRA_OUTPUT, uriPhotoFile)
                    startActivityForResult(capturePhotoIntent, REQUEST_PHOTO_CAM)
                }
                .setNegativeButton(R.string.alert_on_photo_gallery) { dialog, which ->
                    val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                    photoPickerIntent.type = "image/*"
                    startActivityForResult(photoPickerIntent, REQUEST_PHOTO_GAL)
                }

        if (!photoFile!!.exists()) {
            photoAlertDialog.setMessage(R.string.alert_on_photo_text_first_photo)
        } else {
            photoAlertDialog.setMessage(R.string.alert_on_photo_text_second_photo)
            photoAlertDialog.setNeutralButton(R.string.alert_on_photo_remove) { dialog, which ->
                AlertDialog.Builder(activity)
                        .setTitle(R.string.alert_del_photo_title)
                        .setMessage(R.string.alert_del_photo_text)
                        .setPositiveButton(R.string.alert_del_photo_yes) { dialog12, which12 ->
                            photoFile!!.delete()
                            updatePhotoView()
                        }
                        .setNegativeButton(R.string.alert_del_photo_no) { dialog1, which1 -> }
                        .show()
            }
        }
        photoAlertDialog.show()
    }

    private fun updatePhotoView() {
        if (photoFile!!.exists()) {
            val bitmap = PictureUtils.getScaledBitmap(photoFile?.path, activity)
            note_photo_image_view.setImageBitmap(bitmap)
            note_photo_image_view.visibility = View.VISIBLE
        } else {
            note_photo_image_view.visibility = View.GONE
        }
        //        photoView.setImageURI(Uri.fromFile(photoFile));
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_PHOTO_CAM -> updatePhotoView()
            REQUEST_PHOTO_GAL -> {
                //по полученному УРИ пишем в файл картику - жесть какая-то
                val imgUri = data?.data
                val chunkSize = 1024
                val imageData = ByteArray(chunkSize)
                try {
                    val inputStream = activity.contentResolver.openInputStream(imgUri)
                    val outputStream = FileOutputStream(photoFile)
                    while (true) {
                        val bytesRead = inputStream.read(imageData)
                        if (bytesRead > 0) {
                            outputStream.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)))
                        } else break
                    }
                } catch (e: Exception) {
                    Log.e("Something wrong.", e.toString())
                } finally {
                    try {
                        `in`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    out.close()
                }
                updatePhotoView()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!noteEmpty()) {
            dbHelper.insert(note!!)
            dbHelper.insertRx(note!!)
                    .subscribeOn(Schedulers.io())
                    .subscribe({ })
//                            ,
//                            Consumer<Throwable> { it.printStackTrace() })
        } else {
            removeNote()
        }
    }

    private fun noteEmpty(): Boolean {
        val photofileIsEmpty = !photoFile!!.exists()
        val descriptionEmpty = note?.description.isNullOrBlank()
        val titleEmpty = note?.title.isNullOrBlank()
        return photofileIsEmpty && descriptionEmpty && titleEmpty
    }

    private fun removeNote() {
        NotesOrderUtil.removeNoteFromOrderList(note?.id, context)
        dbHelper.delete(note!!)
    }
}
