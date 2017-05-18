package com.fed.notes;

import android.support.v4.app.Fragment;

import com.fed.notes.utils.SingleFragmentActivity;

/**
 * Created by f on 10.05.2017.
 */

public class NoteListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new NoteListFragment();
    }
}
