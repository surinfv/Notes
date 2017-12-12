package com.fed.notes.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.fed.notes.R;
import com.fed.notes.database.Note;

/**
 * Created by f on 10.05.2017.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new ListFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public void openNoteFragmentPreview(Note note) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = NotePreviewFragment.newInstance(note.id);
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void openNoteFragmentEditor(Note note) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = NoteEditorFragment.newInstance(note.id);
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
