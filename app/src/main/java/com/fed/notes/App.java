package com.fed.notes;

import android.app.Application;

import com.fed.notes.di.AppComponent;
import com.fed.notes.di.AppModule;
import com.fed.notes.di.DaggerAppComponent;

/**
 * Created by Fedor SURIN on 03.11.2017.
 */

public class App extends Application {

    private static App app;
    private static AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        component = buildComponent();
    }

    private AppComponent buildComponent() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public static App getInstance() {
        return app;
    }

    public static AppComponent getComponent() {
        return component;
    }
}
