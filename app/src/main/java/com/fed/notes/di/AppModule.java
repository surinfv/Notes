package com.fed.notes.di;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.fed.notes.App;
import com.fed.notes.database.AppDatabase;
import com.fed.notes.database.DbHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private Context context;

    public AppModule(App app) {
        context = app;
    }

    @Singleton
    @Provides
    Context provideContext() {
        return context;
    }

    @Singleton
    @Provides
    AppDatabase provideDB(Context context) {
        return Room
                .databaseBuilder(context, AppDatabase.class, "notes-db")
                .allowMainThreadQueries()   //TODO: remove this - synk db queries
                .build();
    }

    @Singleton
    @Provides
    DbHelper provideDbHelper(AppDatabase appDatabase) {
        return new DbHelper(appDatabase.getNoteDao());
    }
}
