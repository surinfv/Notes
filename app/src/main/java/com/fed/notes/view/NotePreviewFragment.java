package com.fed.notes.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fed.notes.App;
import com.fed.notes.BuildConfig;
import com.fed.notes.R;
import com.fed.notes.database.DbHelper;
import com.fed.notes.database.Note;
import com.fed.notes.utils.ImageDialog;
import com.fed.notes.utils.PictureUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import io.reactivex.schedulers.Schedulers;

/**
 * Created by f on 05.05.2017.
 */

public class NotePreviewFragment extends Fragment {
    private static final String ARGS_NOTE_ID = "argsnoteid";

    private Note note;

    private TextView noteTitleField;
    private TextView noteDescriptionField;
    private TextView date;
    private ImageView photoView;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    private DateFormat dateFormat;
    private File photoFile;
    private Uri uriPhotoFile;
    private UUID noteID;

    @Inject
    DbHelper dbHelper;

    public static NotePreviewFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_NOTE_ID, id);

        NotePreviewFragment noteFragment = new NotePreviewFragment();
        noteFragment.setArguments(args);
        return noteFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getComponent().inject(this);

        noteID = (UUID) getArguments().getSerializable(ARGS_NOTE_ID);
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

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_preview, container, false);

        toolbar = v.findViewById(R.id.toolbar);
        noteTitleField = v.findViewById(R.id.note_title);
        noteDescriptionField = v.findViewById(R.id.note_description);
        date = v.findViewById(R.id.create_date);
        appBarLayout = v.findViewById(R.id.appbar);
        photoView = v.findViewById(R.id.note_photo);
        photoView.setOnClickListener(v1 -> {
            FragmentManager manager = getFragmentManager();
            ImageDialog dialog = ImageDialog.newInstance(photoFile.getPath());
            dialog.show(manager, "IMAGE_FULL");
        });
        init();
        updatePhotoView();
        return v;
    }

    private void init() {
        note = dbHelper.getNote(noteID);
        photoFile = dbHelper.getPhotoFile(note);

        if (Build.VERSION.SDK_INT > 23) {
            uriPhotoFile = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", photoFile);
        } else {
            uriPhotoFile = Uri.fromFile(photoFile);
        }

        toolbar.setTitle(note.title);
        noteTitleField.setText(note.title);
        noteDescriptionField.setText(note.description);
        dateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.ENGLISH);
        date.setText(dateFormat.format(note.date));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_note_preview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_send_via_email:
                sendEmailDialog();
                return true;

            case R.id.menu_item_edit_note:
                ((MainActivity) getActivity()).oneNoteFragmentEditor(note);
                return true;

            case R.id.menu_item_delete_note:
                deleteNoteDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendEmailDialog() {
        Intent intent;
        if (photoFile.exists()) {
            intent = ShareCompat.IntentBuilder.from(getActivity())
                    .setType("plain/text")
//                            .setType("image/*")
                    .setSubject(getResources().getString(R.string.email_text) + note.title)
                    .setText(note.description)
                    .setStream(uriPhotoFile)
                    .getIntent();
        } else {
            intent = ShareCompat.IntentBuilder.from(getActivity())
                    .setType("plain/text")
                    .setSubject(getResources().getString(R.string.email_text) + note.title)
                    .setText(note.description)
                    .getIntent();
        }
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
//                    startActivity(Intent.createChooser(intent, "send via..."));
        } else {
            AlertDialog.Builder eMailIntentAlertDialog = new AlertDialog.Builder(getActivity());
            eMailIntentAlertDialog.setTitle(R.string.email_intent_title);
            eMailIntentAlertDialog.setMessage(R.string.email_intent_text);
            eMailIntentAlertDialog.setNeutralButton(R.string.ok_button, (dialog, which) -> {
            });
            eMailIntentAlertDialog.show();
        }
    }

    private void deleteNoteDialog() {
        AlertDialog.Builder deleteAlertDialog = new AlertDialog.Builder(getActivity());
        deleteAlertDialog.setTitle(R.string.alert_on_del_title);
        deleteAlertDialog.setMessage(R.string.alert_on_del_text);
        deleteAlertDialog.setPositiveButton(R.string.alert_on_del_yes, (dialog, which) -> {
            NotePreviewFragment.this.delFromOrderList(note.id);
//                        dbHelper.delete(note);
            dbHelper.deleteRx(note)
                    .observeOn(Schedulers.io())
                    .subscribe(() -> {
                            },
                            Throwable::printStackTrace);
            NotePreviewFragment.this.getActivity().onBackPressed();
        });
        deleteAlertDialog.setNeutralButton(R.string.alert_on_del_cancel, (dialog, which) -> {
        });
        deleteAlertDialog.show();
    }

    private void delFromOrderList(UUID uuid) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String json = prefs.getString(ListFragment.NOTES_ORDER, "");
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<UUID>>() {
        }.getType();
        List<UUID> IDs = gson.fromJson(json, listType);
        IDs.remove(uuid);

        SharedPreferences.Editor editor = prefs.edit();
        json = gson.toJson(IDs);
        editor.putString(ListFragment.NOTES_ORDER, json);
        editor.apply();
    }

    private void updatePhotoView() {
        if (photoFile.exists()) {
            Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
            photoView.setImageBitmap(bitmap);
            appBarLayout.setVisibility(View.VISIBLE);
            noteTitleField.setVisibility(View.GONE);
        } else {
            appBarLayout.setVisibility(View.GONE);
            noteTitleField.setVisibility(View.VISIBLE);
        }
//        photoView.setImageURI(Uri.fromFile(photoFile));
    }
}
