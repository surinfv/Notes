package com.fed.notes;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.UUID;

public class MainActivity extends SingleFragmentActivity {

    public static final String EXTRA_NOTE_ID = "extranoteid";

    @Override
    protected Fragment createFragment() {
        return new NoteFragment();
    }

    public static Intent newIntent(Context packageContext, UUID noteID) {
        Intent intent = new Intent(packageContext, MainActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, noteID);
        return intent;
    }
}
