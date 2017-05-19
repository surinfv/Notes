package com.fed.notes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.fed.notes.utils.touchhelper.ItemTouchHelperAdapter;
import com.fed.notes.utils.touchhelper.SimpleItemTouchHelperCallback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by f on 10.05.2017.
 */

public class NoteListFragment extends Fragment {

    private RecyclerView mNoteRecyclerView;
    private NoteAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            mLastClickedItemPos = savedInstanceState.getInt(LASTCLICKEDITEM);
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
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mNotes, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {
            NoteBook.get(getActivity()).deleteNote(mNotes.get(position));
            mNotes.remove(position);
            notifyItemRemoved(position);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {

        NoteBook notebook = NoteBook.get(getActivity());
        List<Note> notes = notebook.getNotes();

        if (mAdapter == null) {
            mAdapter = new NoteAdapter(notes);
            mNoteRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setNotes(notes);
            mAdapter.notifyDataSetChanged();
//            mAdapter.notifyItemChanged(mLastClickedItemPos);
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
//        outState.putInt(LASTCLICKEDITEM, mLastClickedItemPos);
    }
}
