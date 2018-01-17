package com.fed.notes.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration


@Database(entities = arrayOf(Note::class), version = 2)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val noteDao: NoteDAO

    companion object {
        @JvmField
        val MIGRATION_1_2: Migration = Migration1To2()
    }
}
