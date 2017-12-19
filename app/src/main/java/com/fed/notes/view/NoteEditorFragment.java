package com.fed.notes.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fed.notes.App;
import com.fed.notes.BuildConfig;
import com.fed.notes.R;
import com.fed.notes.database.DbHelper;
import com.fed.notes.database.Note;
import com.fed.notes.utils.EditTextModif;
import com.fed.notes.utils.ImageDialog;
import com.fed.notes.utils.NotesOrderUtil;
import com.fed.notes.utils.PictureUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import io.reactivex.schedulers.Schedulers;

import static java.lang.System.in;
import static java.lang.System.out;

/**
 * Created by f on 05.05.2017.
 */

public class NoteEditorFragment extends Fragment {
    private static final String ARGS_NOTE_ID = "argsnoteid";
    private static final int REQUEST_PHOTO_CAM = 0;
    private static final int REQUEST_PHOTO_GAL = 1;

    private ImageView photoView;

    private Note note;
    private File photoFile;
    private boolean canTakePhoto;
    private Intent capturePhotoIntent;
    private Uri uriPhotoFile;

    @Inject
    DbHelper dbHelper;

    public static NoteEditorFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_NOTE_ID, id);

        NoteEditorFragment noteFragment = new NoteEditorFragment();
        noteFragment.setArguments(args);
        return noteFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getComponent().inject(this);

        UUID noteID = (UUID) getArguments().getSerializable(ARGS_NOTE_ID);

        note = dbHelper.getNote(noteID);
        photoFile = dbHelper.getPhotoFile(note);

//        dbHelper.getNoteRx(noteID)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                    nt -> {
//                        note = nt;
//                        photoFile = dbHelper.getPhotoFile(note);
//                        if (Build.VERSION.SDK_INT > 23) {
//                            uriPhotoFile = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", photoFile);
//                        } else {
//                            uriPhotoFile = Uri.fromFile(photoFile);
//                        }
//                    },
//                    Throwable::printStackTrace
//                );

        //check 4 photo:
        PackageManager packageManager = getActivity().getPackageManager();
        capturePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        canTakePhoto = photoFile != null &&
                capturePhotoIntent.resolveActivity(packageManager) != null;

        if (Build.VERSION.SDK_INT > 23) {
            uriPhotoFile = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", photoFile);
        } else {
            uriPhotoFile = Uri.fromFile(photoFile);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_editor, container, false);

        EditTextModif noteTitleField = v.findViewById(R.id.note_title);
        noteTitleField.setText(note.getTitle());
        noteTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                note.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditTextModif noteDescriptionField = v.findViewById(R.id.note_description);
        noteDescriptionField.setText(note.getDescription());
        noteDescriptionField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                note.setDescription(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        TextView date = v.findViewById(R.id.note_date);
        DateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.ENGLISH);
        date.setText(dateFormat.format(note.getDate()));

        com.getbase.floatingactionbutton.FloatingActionButton fab = v.findViewById(R.id.fab_photo);
        fab.setIcon(R.drawable.ic_take_photo);
        fab.setOnClickListener((view1 -> takePhotoDialog()));
        if (!canTakePhoto) fab.setVisibility(View.GONE);

        photoView = v.findViewById(R.id.note_photo_image_view);
        updatePhotoView();
        photoView.setOnClickListener(v1 -> {
            FragmentManager manager = getFragmentManager();
            ImageDialog dialog = ImageDialog.newInstance(photoFile.getPath());
            dialog.show(manager, "IMAGE_FULL");
        });

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!noteEmpty()) {
            dbHelper.insert(note);
            dbHelper.insertRx(note)
                    .subscribeOn(Schedulers.io())
                    .subscribe(() -> {
                            },
                            Throwable::printStackTrace);
        } else {
            removeNote();
        }
    }

    private boolean noteEmpty() {
        boolean photofileExist = photoFile.exists();
        boolean discriptionExist = note.getDescription() != null && note.getDescription().length() > 0;
        boolean titleExist = note.getTitle() != null && note.getTitle().length() > 0;
        return !photofileExist && !discriptionExist && !titleExist;
    }

    private void removeNote() {
        NotesOrderUtil.removeNoteFromOrderList(note.getId(), getContext());
        dbHelper.delete(note);
    }

    private void takePhotoDialog() {
        final AlertDialog.Builder photoAlertDialog = new AlertDialog.Builder(getActivity());
        photoAlertDialog.setTitle(R.string.alert_on_photo_title);
        photoAlertDialog.setPositiveButton(R.string.alert_on_photo_cam, (dialog, which) -> {
            capturePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriPhotoFile);
            startActivityForResult(capturePhotoIntent, REQUEST_PHOTO_CAM);
        });
        photoAlertDialog.setNegativeButton(R.string.alert_on_photo_gallery, (dialog, which) -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, REQUEST_PHOTO_GAL);
        });

        if (!photoFile.exists()) {
            photoAlertDialog.setMessage(R.string.alert_on_photo_text_first_photo);
        } else {
            photoAlertDialog.setMessage(R.string.alert_on_photo_text_second_photo);
            photoAlertDialog.setNeutralButton(R.string.alert_on_photo_remove, (dialog, which) -> {
                AlertDialog.Builder photoDelDialog = new AlertDialog.Builder(getActivity());
                photoDelDialog.setTitle(R.string.alert_del_photo_title);
                photoDelDialog.setMessage(R.string.alert_del_photo_text);
                photoDelDialog.setPositiveButton(R.string.alert_del_photo_yes, (dialog12, which12) -> {
                    photoFile.delete();
                    updatePhotoView();
                });
                photoDelDialog.setNegativeButton(R.string.alert_del_photo_no, (dialog1, which1) -> {
                });
                photoDelDialog.show();
            });
        }
        photoAlertDialog.show();
    }

    private void updatePhotoView() {
        if (photoFile.exists()) {
            Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
            photoView.setImageBitmap(bitmap);
            photoView.setVisibility(View.VISIBLE);
        } else {
            photoView.setVisibility(View.GONE);
        }
//        photoView.setImageURI(Uri.fromFile(photoFile));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_PHOTO_CAM) {
            updatePhotoView();
        }
        if (requestCode == REQUEST_PHOTO_GAL) {
            //по полученному УРИ пишем в файл картику
            Uri imgUri = data.getData();

            final int chunkSize = 1024;
            byte[] imageData = new byte[chunkSize];

            try {
                InputStream in = getActivity().getContentResolver().openInputStream(imgUri);
                OutputStream out = new FileOutputStream(photoFile);

                int bytesRead;
                while ((bytesRead = in.read(imageData)) > 0) {
                    out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
                }

            } catch (Exception ex) {
                Log.e("Something wrong.", String.valueOf(ex));
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out.close();
            }
            updatePhotoView();
        }
    }
}
