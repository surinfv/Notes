package com.fed.notes.di;

import com.fed.notes.view.ListFragment;
import com.fed.notes.view.NoteFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Fedor SURIN on 03.11.2017.
 */

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    void inject(NoteFragment noteFragment);

    void inject(ListFragment listFragment);
}
