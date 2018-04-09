package com.fed.notes.di

import com.fed.notes.presenter.EditorPresenter
import com.fed.notes.presenter.PreviewPresenter
import com.fed.notes.view.ListFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(listFragment: ListFragment)

    fun inject(previewPresenter: PreviewPresenter)

    fun inject(editorPresenter: EditorPresenter)
}
