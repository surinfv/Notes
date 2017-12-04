package com.fed.notes.di;

import com.fed.notes.view.ListFragment;
import com.fed.notes.view.NoteEditorFragment;
import com.fed.notes.view.NotePreviewFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Fedor SURIN on 03.11.2017.
 */

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    void inject(NotePreviewFragment notePreviewFragment);

    void inject(NoteEditorFragment noteEditorFragment);

    void inject(ListFragment listFragment);
}
