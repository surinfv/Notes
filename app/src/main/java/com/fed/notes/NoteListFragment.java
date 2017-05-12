package com.fed.notes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by f on 10.05.2017.
 */

public class NoteListFragment extends Fragment {
    private RecyclerView mNoteRecyclerView;
    private NoteAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);

        mNoteRecyclerView = (RecyclerView) view.findViewById(R.id.note_recycler_view);
        mNoteRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        updateUI();
        return view;
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
//            Toast.makeText(getActivity(), mNote.getTitle(), Toast.LENGTH_SHORT).show();
            Intent intent = MainActivity.newIntent(getActivity(), mNote.getId());
            startActivity(intent);
        }
    }

    private class NoteAdapter extends RecyclerView.Adapter<NoteHolder> {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (mAdapter == null) {
            NoteBook notebook = NoteBook.get(getActivity());
            List<Note> notes = notebook.getNotes();
            mAdapter = new NoteAdapter(notes);
            mNoteRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }
}
