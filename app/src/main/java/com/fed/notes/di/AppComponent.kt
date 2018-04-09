package com.fed.notes.di

import com.fed.notes.presenter.EditorPresenter
import com.fed.notes.presenter.ListPresenter
import com.fed.notes.presenter.PreviewPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(listPresenter: ListPresenter)

    fun inject(previewPresenter: PreviewPresenter)

    fun inject(editorPresenter: EditorPresenter)
}
