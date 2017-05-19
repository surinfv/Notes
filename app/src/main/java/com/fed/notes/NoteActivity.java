package com.fed.notes;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.fed.notes.utils.SingleFragmentActivity;

import java.util.UUID;

public class NoteActivity extends SingleFragmentActivity {

    private static final String EXTRA_NOTE_ID = "extranoteid";

    @Override
    protected Fragment createFragment() {
        UUID id = (UUID) getIntent().getSerializableExtra(EXTRA_NOTE_ID);
        return NoteFragment.newInstance(id);
    }

    public static Intent newIntent(Context packageContext, UUID noteID) {
        Intent intent = new Intent(packageContext, NoteActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, noteID);
        return intent;
    }
}