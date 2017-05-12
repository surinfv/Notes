package com.fed.notes.database;

/**
 * Created by f on 12.05.2017.
 */

public class NoteDbSchema {
    public static final class NoteTable {
        public static final String TABLE_NAME = "notes";

        public static final class Columns {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DESCRIPTION = "description";
            public static final String DATE = "date";
        }
    }
}
