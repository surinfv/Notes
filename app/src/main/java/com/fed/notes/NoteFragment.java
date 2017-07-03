package com.fed.notes;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fed.notes.utils.EditTextModif;
import com.fed.notes.utils.ImageDialog;
import com.fed.notes.utils.PictureUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static java.lang.System.in;
import static java.lang.System.out;

/**
 * Created by f on 05.05.2017.
 */

public class NoteFragment extends Fragment {
    private Note mNote;

    private EditTextModif mNoteTitleField;
    private EditTextModif mNoteDescriptionField;
    private TextView mDate;
    private DateFormat mDateFormat;

    private ImageView mPhotoView;
    private File mPhotoFile;
    private boolean mCanTakePhoto;
    private Intent mCapturePhotoIntent;
    private Uri mUriPhotoFile;


    public static final String ARGS_NOTE_ID = "argsnoteid";
    public static final int REQUEST_PHOTO_CAM = 0;
    public static final int REQUEST_PHOTO_GAL = 1;


    public static NoteFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_NOTE_ID, id);

        NoteFragment noteFragment = new NoteFragment();
        noteFragment.setArguments(args);
        return noteFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID noteID = (UUID) getArguments().getSerializable(ARGS_NOTE_ID);
        mNote = NoteBook.get(getActivity()).getNote(noteID);
        mPhotoFile = NoteBook.get(getActivity()).getPhotoFile(mNote);

        //check 4 photo:
        PackageManager packageManager = getActivity().getPackageManager();
        mCapturePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mCanTakePhoto = mPhotoFile != null &&
                mCapturePhotoIntent.resolveActivity(packageManager) != null;

        mUriPhotoFile = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", mPhotoFile);

        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPhotoFile = NoteBook.get(getActivity()).getPhotoFile(mNote);
        NoteBook.get(getActivity()).updateNote(mNote);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note, container, false);

        mNoteTitleField = (EditTextModif) v.findViewById(R.id.note_title);
        mNoteTitleField.setText(mNote.getTitle());
        mNoteTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mNote.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mNoteDescriptionField = (EditTextModif) v.findViewById(R.id.note_description);
        mNoteDescriptionField.setText(mNote.getDescription());
        mNoteDescriptionField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mNote.setDescription(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDate = (TextView) v.findViewById(R.id.create_date);
        mDateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.ENGLISH);
        mDate.setText(mDateFormat.format(mNote.getDate()));

        mPhotoView = (ImageView) v.findViewById(R.id.note_photo);
        updatePhotoView();


        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                ImageDialog dialog = ImageDialog.newInstance(mPhotoFile.getPath());
                dialog.show(manager, "IMAGE_FULL");
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.item_menu, menu);

        MenuItem photoButton = menu.findItem(R.id.menu_item_take_photo);
        photoButton.setVisible(mCanTakePhoto);
        photoButton.setEnabled(mCanTakePhoto);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_item_take_photo:

                final AlertDialog.Builder photoAlertDialog = new AlertDialog.Builder(getActivity());
                photoAlertDialog.setTitle(R.string.alert_on_photo_title);
                photoAlertDialog.setPositiveButton(R.string.alert_on_photo_cam, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCapturePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoFile);
                        startActivityForResult(mCapturePhotoIntent, REQUEST_PHOTO_CAM);
                    }
                });
                photoAlertDialog.setNegativeButton(R.string.alert_on_photo_gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, REQUEST_PHOTO_GAL);
                    }
                });

                if (!mPhotoFile.exists()) {
                    photoAlertDialog.setMessage(R.string.alert_on_photo_text_first_photo);
                } else {
                    photoAlertDialog.setMessage(R.string.alert_on_photo_text_second_photo);
                    photoAlertDialog.setNeutralButton(R.string.alert_on_photo_remove, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder photoDelDialog = new AlertDialog.Builder(getActivity());
                            photoDelDialog.setTitle(R.string.alert_del_photo_title);
                            photoDelDialog.setMessage(R.string.alert_del_photo_text);
                            photoDelDialog.setPositiveButton(R.string.alert_del_photo_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mPhotoFile.delete();
                                    updatePhotoView();
                                }
                            });
                            photoDelDialog.setNegativeButton(R.string.alert_del_photo_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            photoDelDialog.show();
                        }
                    });
                }
                photoAlertDialog.show();
                return true;

            case R.id.menu_item_send_via_email:
                Intent intent;
                if (mPhotoFile.exists()) {
                    intent = ShareCompat.IntentBuilder.from(getActivity())
                            .setType("plain/text")
//                            .setType("image/*")
                            .setSubject(getResources().getString(R.string.email_text) + mNote.getTitle())
                            .setText(mNote.getDescription())
                            .setStream(mUriPhotoFile)
                            .getIntent();
                } else {
                    intent = ShareCompat.IntentBuilder.from(getActivity())
                            .setType("plain/text")
                            .setSubject(getResources().getString(R.string.email_text) + mNote.getTitle())
                            .setText(mNote.getDescription())
                            .getIntent();
                }
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
//                    startActivity(Intent.createChooser(intent, "send via..."));
                } else {
                    AlertDialog.Builder eMailIntentAlertDialog = new AlertDialog.Builder(getActivity());
                    eMailIntentAlertDialog.setTitle(R.string.email_intent_title);
                    eMailIntentAlertDialog.setMessage(R.string.email_intent_text);
                    eMailIntentAlertDialog.setNeutralButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    eMailIntentAlertDialog.show();
                }
                return true;

            case R.id.menu_item_delete_note:
                AlertDialog.Builder deleteAlertDialog = new AlertDialog.Builder(getActivity());
                deleteAlertDialog.setTitle(R.string.alert_on_del_title);
                deleteAlertDialog.setMessage(R.string.alert_on_del_text);
                deleteAlertDialog.setPositiveButton(R.string.alert_on_del_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NoteBook.get(getActivity()).deleteNote(mNote);
                        delFromOrderList(mNote.getId());
                        getActivity().finish();
                    }
                });
                deleteAlertDialog.setNeutralButton(R.string.alert_on_del_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                deleteAlertDialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void delFromOrderList(UUID uuid) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String json = prefs.getString(NoteListFragment.NOTES_ORDER, "");
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<UUID>>(){}.getType();
        List<UUID> IDs = gson.fromJson(json, listType);
        IDs.remove(uuid);

        SharedPreferences.Editor editor = prefs.edit();
        json = gson.toJson(IDs);
        editor.putString(NoteListFragment.NOTES_ORDER, json);
        editor.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_PHOTO_CAM) {
            updatePhotoView();
        }
        if (requestCode == REQUEST_PHOTO_GAL){
            //тут по полученному УРИ пишем в файл картику
            Uri imgUri = data.getData();

            final int chunkSize = 1024;
            byte[] imageData = new byte[chunkSize];

            try {
                InputStream in = getActivity().getContentResolver().openInputStream(imgUri);
                OutputStream out = new FileOutputStream(mPhotoFile);

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


    private void updatePhotoView() {
        if (mPhotoFile.exists()) {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setVisibility(View.VISIBLE);
        } else {
            mPhotoView.setVisibility(View.GONE);
        }
//        mPhotoView.setImageURI(Uri.fromFile(mPhotoFile));
    }
}
