package com.fed.notes.di

import com.fed.notes.presenter.PreviewPresenter
import com.fed.notes.view.ListFragment
import com.fed.notes.view.NoteEditorFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(noteEditorFragment: NoteEditorFragment)

    fun inject(listFragment: ListFragment)

    fun inject(previewPresenter: PreviewPresenter)
}
