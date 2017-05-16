package com.fed.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by f on 05.05.2017.
 */

public class NoteFragment extends Fragment {
    private Note mNote;

    private EditText mNoteTitleField;
    private EditText mNoteDescriptionField;
    private TextView mDate;
    private DateFormat mDateFormat;

    public static final String ARGS_NOTE_ID = "argsnoteid";

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
//        mNote = new Note();
        UUID noteID = (UUID) getArguments().getSerializable(ARGS_NOTE_ID);
        mNote = NoteBook.get(getActivity()).getNote(noteID);

        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        NoteBook.get(getActivity()).updateNote(mNote);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note, container, false);

        mNoteTitleField = (EditText) v.findViewById(R.id.note_title);
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

        mNoteDescriptionField = (EditText) v.findViewById(R.id.note_description);
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
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.item_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_item_delete_note:
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle(R.string.alert_on_del_title);
            alertDialog.setMessage(R.string.alert_on_del_text);
            alertDialog.setPositiveButton(R.string.alert_on_del_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NoteBook.get(getActivity()).deleteNote(mNote);
                    getActivity().finish();
                }
            });
            alertDialog.setNeutralButton(R.string.alert_on_del_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            alertDialog.show();

            case R.id.menu_item_send_via_email:
                // email method
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, mNote.getTitle());
                intent.putExtra(Intent.EXTRA_TEXT, mNote.getDescription());
                startActivity(intent);

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
