package com.fed.notes.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.fed.notes.R
import com.fed.notes.database.Note
import com.fed.notes.view.preview.NotePreviewFragment


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment == null) {
            fragment = ListFragment()
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
        }
    }

    fun openNoteFragmentPreview(note: Note) {
        val fragment = NotePreviewFragment.newInstance(note.id)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
    }

    fun openNoteFragmentEditor(note: Note) {
        val fragment = NoteEditorFragment.newInstance(note.id)
        supportFragmentManager
                .beginTransaction().replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
    }
}
