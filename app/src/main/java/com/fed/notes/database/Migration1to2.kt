package com.fed.notes.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration


class Migration1To2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val TABLE_NAME_TEMP = "notesTemp"
        val TABLE_NAME = "notes"
        val ID_COLUMN = "id"
        val TITLE_COLUMN = "title"
        val DESCRIPTION_COLUMN = "description"
        val DATE_COLUMN = "date"

        database.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_NAME_TEMP " +
                "($ID_COLUMN TEXT NOT NULL, " +
                "$TITLE_COLUMN TEXT, " +
                "$DESCRIPTION_COLUMN TEXT, " +
                "$DATE_COLUMN INTEGER NOT NULL, " +
                "PRIMARY KEY($ID_COLUMN))")

        database.execSQL("INSERT INTO $TABLE_NAME_TEMP " +
                "($ID_COLUMN, $TITLE_COLUMN, $DESCRIPTION_COLUMN, $DATE_COLUMN) " +
                "SELECT   $ID_COLUMN, $TITLE_COLUMN, $DESCRIPTION_COLUMN, $DATE_COLUMN FROM $TABLE_NAME")

        database.execSQL("DROP TABLE $TABLE_NAME")

        database.execSQL("ALTER TABLE $TABLE_NAME_TEMP RENAME TO $TABLE_NAME")
    }
}