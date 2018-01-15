package com.fed.notes

import android.app.Application

import com.fed.notes.di.AppComponent
import com.fed.notes.di.AppModule
import com.fed.notes.di.DaggerAppComponent


class App : Application() {
    companion object {
        var instance: App? = null
            private set

        var component: AppComponent? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        component = buildComponent()
    }

    private fun buildComponent(): AppComponent {
        return DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
    }
}
