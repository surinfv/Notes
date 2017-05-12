package com.fed.notes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fed.notes.database.NoteDbSchema.NoteTable;

/**
 * Created by f on 12.05.2017.
 */

public class NoteBaseHelper extends SQLiteOpenHelper{
    private static final int VERSION = 1;
    public static final String DATABASE_NAME = "noteDataBase.db";

    public NoteBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + NoteTable.TABLE_NAME +"(" +
                " _id integer primary key autoincrement, " +
                NoteTable.Columns.UUID + ", " +
                NoteTable.Columns.TITLE + ", " +
                NoteTable.Columns.DESCRIPTION + ", " +
                NoteTable.Columns.DATE + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
