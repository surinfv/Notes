package com.fed.notes.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fed.notes.App;
import com.fed.notes.R;
import com.fed.notes.database.DbHelper;
import com.fed.notes.database.Note;
import com.fed.notes.touchhelper.ItemTouchHelperAdapter;
import com.fed.notes.touchhelper.SimpleItemTouchHelperCallback;
import com.fed.notes.utils.NotesOrderUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import io.reactivex.schedulers.Schedulers;

/**
 * Created by Fedor SURIN on 10.05.2017.
 */

public class ListFragment extends Fragment {

    private RecyclerView noteRecyclerView;
    private com.getbase.floatingactionbutton.FloatingActionButton fab;

    private NoteAdapter adapter;
    private List<UUID> notesOrder;

    @Inject
    DbHelper dbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_note_list, container, false);

        noteRecyclerView = view.findViewById(R.id.note_recycler_view);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        fab = view.findViewById(R.id.fab_add_note);
        fab.setIcon(R.drawable.ic_new_note);
        fab.setOnClickListener((view1 -> createNewNote()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(noteRecyclerView);
    }

    private void updateUI() {
        notesOrder = NotesOrderUtil.loadOrder(getContext());

        //fixme add reactive getNotes
        List<Note> notes = new ArrayList<>();
        if (notesOrder.size() > 0) {
            //TODO: get notes list in one query in order from order list
//            UUID[] ids = new UUID[notesOrder.size()];
//            notesOrder.toArray(ids);
//            notes = noteDAO.getNotes(ids);
            for (UUID id : notesOrder) {
                notes.add(dbHelper.getNote(id));
            }
        }
//        if (adapter == null) {
        adapter = new NoteAdapter(notes);
        noteRecyclerView.setAdapter(adapter);
//        } else {
        adapter.setNotes(notes);
        adapter.notifyDataSetChanged();
//        }
    }

    private void createNewNote() {
        Note note = new Note();

        notesOrder.add(0, note.id);
        dbHelper.insertRx(note)
                .subscribeOn(Schedulers.io())
                .subscribe(() ->
                                ((MainActivity) getActivity()).openNoteFragmentEditor(note),
                        Throwable::printStackTrace);
    }

    @Override
    public void onPause() {
        super.onPause();
        NotesOrderUtil.saveOrder(notesOrder, getContext());
    }

    private class NoteHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView itemTitle;
        private TextView itemDescription;
        private TextView itemDate;
        private Note note;

        NoteHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            itemTitle = itemView.findViewById(R.id.item_list_title);
            itemDescription = itemView.findViewById(R.id.item_list_description);
            itemDate = itemView.findViewById(R.id.item_list_date);
        }

        void bindNote(Note note) {
            if (note == null) return;
            this.note = note;
            itemTitle.setText(this.note.title);
            itemDescription.setText(this.note.description);
            DateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.ENGLISH);
            itemDate.setText(dateFormat.format(this.note.date));
        }

        @Override
        public void onClick(View v) {
            ((MainActivity) getActivity()).openNoteFragmentPreview(note);
        }
    }

    private class NoteAdapter extends RecyclerView.Adapter<NoteHolder> implements ItemTouchHelperAdapter {

        private int noteTmpPos;
        private Note noteTmp;
        private List<Note> notes;
        View.OnClickListener snackbarOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ///return just deleted note
                Toast.makeText(getActivity(), noteTmp.title + getResources().getString(R.string.snackbar_return), Toast.LENGTH_SHORT).show();
                notes.add(noteTmpPos, noteTmp);
                notesOrder.add(noteTmpPos, noteTmp.id);
                notifyItemInserted(noteTmpPos);
            }
        };

        NoteAdapter(List<Note> notes) {
            this.notes = notes;
        }

        @Override
        public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item, parent, false);
            return new NoteHolder(view);
        }

        @Override
        public void onBindViewHolder(NoteHolder holder, int position) {
            Note note = notes.get(position);
            holder.bindNote(note);
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        public void setNotes(List<Note> notes) {
            this.notes = notes;
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(notes, i, i + 1);
                    Collections.swap(notesOrder, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(notes, i, i - 1);
                    Collections.swap(notesOrder, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {
            cloneNote(notes.get(position), position);

            notes.remove(position);
            notesOrder.remove(position);
            notifyItemRemoved(position);

            Snackbar mSnackBar = Snackbar.make(noteRecyclerView, noteTmp.title + getResources().getString(R.string.snackbar_delete), Snackbar.LENGTH_LONG);
            View snackbarView = mSnackBar.getView();
            snackbarView.setBackgroundColor(getResources().getColor(R.color.snack_bar_background));
            mSnackBar.setAction(getResources().getString(R.string.snackbar_undo), snackbarOnClickListener);
            mSnackBar.show();

            mSnackBar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        dbHelper.deleteRx(noteTmp)
                                .subscribeOn(Schedulers.io())
                                .subscribe(() -> {
                                        },
                                        Throwable::printStackTrace);
                    }
                }

//                        @Override
//                        public void onShown(Snackbar snackbar) {
//                            Toast.makeText(getActivity(), "Snackbar is showed", Toast.LENGTH_SHORT).show();
//                        }
            });
        }

        private void cloneNote(Note note, int pos) {
            noteTmpPos = pos;
            noteTmp = new Note();
            noteTmp.title = note.title;
            noteTmp.description = note.description;
            noteTmp.id = note.id;
            noteTmp.date = note.date;
        }
    }
}