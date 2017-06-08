package com.fed.notes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fed.notes.touchhelper.ItemTouchHelperAdapter;
import com.fed.notes.touchhelper.SimpleItemTouchHelperCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by f on 10.05.2017.
 */

public class NoteListFragment extends Fragment {

    private RecyclerView mNoteRecyclerView;
    private NoteAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;

    SharedPreferences mShPref;
    public static final String NOTES_ORDER = "notesoreder";
    private List<UUID> mNotesOrder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//        }

        View view = inflater.inflate(R.layout.fragment_note_list, container, false);

        mNoteRecyclerView = (RecyclerView) view.findViewById(R.id.note_recycler_view);
        mNoteRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        updateUI();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mNoteRecyclerView);
    }

    private class NoteHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mItemTitle;

        private TextView mItemDescription;
        private TextView mItemDate;
        private Note mNote;

        public NoteHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mItemTitle = (TextView) itemView.findViewById(R.id.item_list_title);
            mItemDescription = (TextView) itemView.findViewById(R.id.item_list_description);
            mItemDate = (TextView) itemView.findViewById(R.id.item_list_date);
        }

        public void bindNote(Note note) {
            mNote = note;
            mItemTitle.setText(mNote.getTitle());
            mItemDescription.setText(mNote.getDescription());
            DateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.ENGLISH);
            mItemDate.setText(dateFormat.format(mNote.getDate()));
        }

        @Override
        public void onClick(View v) {
            Intent intent = NoteActivity.newIntent(getActivity(), mNote.getId());
            startActivity(intent);
        }

    }

    private class NoteAdapter extends RecyclerView.Adapter<NoteHolder> implements ItemTouchHelperAdapter {

        private Note mNoteTmp;
        private int mNoteTmpPos;

        private List<Note> mNotes;
        public NoteAdapter(List<Note> notes) {
            mNotes = notes;
        }

        @Override
        public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item, parent, false);
            return new NoteHolder(view);
        }

        @Override
        public void onBindViewHolder(NoteHolder holder, int position) {
            Note note = mNotes.get(position);
            holder.bindNote(note);
        }

        @Override
        public int getItemCount() {
            return mNotes.size();
        }

        public void setNotes(List<Note> notes) {
            mNotes = notes;
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mNotes, i, i + 1);
                    Collections.swap(mNotesOrder, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mNotes, i, i - 1);
                    Collections.swap(mNotesOrder, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {
            //сохранить заметку во временный экземпляр и её позицию
            cloneNote(mNotes.get(position), position);

//            NoteBook.get(getActivity()).deleteNote(mNotes.get(position));
            mNotes.remove(position);
            mNotesOrder.remove(position);
            notifyItemRemoved(position);

            Snackbar mSnackBar = Snackbar.make(mNoteRecyclerView, mNoteTmp.getTitle() + " removed", Snackbar.LENGTH_LONG);
            View snackbarView = mSnackBar.getView();
            snackbarView.setBackgroundColor(getResources().getColor(R.color.snack_bar_background));
            mSnackBar.setAction("UNDO", snackbarOnClickListener);
            mSnackBar.show();

            mSnackBar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                NoteBook.get(getActivity()).deleteNote(mNoteTmp);
                            }
                        }

//                        @Override
//                        public void onShown(Snackbar snackbar) {
//                            Toast.makeText(getActivity(), "Snackbar is showed", Toast.LENGTH_SHORT).show();
//                        }
                    });
        }

        View.OnClickListener snackbarOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ///вернуть удаленную заметку на место
                Toast.makeText(getActivity(), "returned", Toast.LENGTH_SHORT).show();
                mNotes.add(mNoteTmpPos, mNoteTmp);
                mNotesOrder.add(mNoteTmpPos, mNoteTmp.getId());
                notifyItemInserted(mNoteTmpPos);
            }
        };

        private void cloneNote(Note note, int pos) {
            mNoteTmpPos = pos;
            mNoteTmp = new Note();
            mNoteTmp.setTitle(note.getTitle());
            mNoteTmp.setDescription(note.getDescription());
            mNoteTmp.setId(note.getId());
            mNoteTmp.setDate(note.getDate().getTime());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        loadOrder();

        NoteBook notebook = NoteBook.get(getActivity());

        List<Note> notes = new ArrayList<>();
        if (mNotesOrder.size() > 0) {
            for (UUID id : mNotesOrder) {
                notes.add(notebook.getNote(id));
            }
        }

        if (mAdapter == null) {
            mAdapter = new NoteAdapter(notes);
            mNoteRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setNotes(notes);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_note:
                Note note = new Note();

                mNotesOrder.add(0, note.getId());

                NoteBook.get(getActivity()).addNote(note);
                Intent intent = NoteActivity.newIntent(getActivity(), note.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void saveOrder() {
        mShPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = mShPref.edit();

        Gson gson = new Gson();
        String json = gson.toJson(mNotesOrder);

        editor.putString(NOTES_ORDER, json);
        editor.apply();
    }

    private void loadOrder() {
        mNotesOrder = new ArrayList<>();
        mShPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String json = mShPref.getString(NOTES_ORDER, "");
        if (!json.equals("")) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<UUID>>(){}.getType();
            mNotesOrder = gson.fromJson(json, listType);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveOrder();
    }
}
