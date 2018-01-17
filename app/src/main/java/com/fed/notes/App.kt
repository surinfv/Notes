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
//        initStetho()
    }

    private fun buildComponent(): AppComponent {
        return DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
    }

    private fun initStetho() {
//        // Create an InitializerBuilder
//        val initializerBuilder = Stetho.newInitializerBuilder(this)
//
//        // Enable Chrome DevTools
//        initializerBuilder.enableWebKitInspector(
//                Stetho.defaultInspectorModulesProvider(this)
//        )
//
//        // Enable command line interface
//        initializerBuilder.enableDumpapp(
//                Stetho.defaultDumperPluginsProvider(this)
//        )
//
//        // Use the InitializerBuilder to generate an Initializer
//        val initializer = initializerBuilder.build()
//
//        // Initialize Stetho with the Initializer
//        Stetho.initialize(initializer)
    }
}
