package com.fed.notes.di

import com.fed.notes.view.ListFragment
import com.fed.notes.view.NoteEditorFragment
import com.fed.notes.view.NotePreviewFragment

import javax.inject.Singleton

import dagger.Component

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(notePreviewFragmentKt: NotePreviewFragment)

    fun inject(noteEditorFragment: NoteEditorFragment)

    fun inject(listFragment: ListFragment)
}
