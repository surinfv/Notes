package com.fed.notes.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

/**
 * Created by f on 05.05.2017.
 */

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey
    @NonNull
    public UUID id;
    public String title;
    public String description;
    public Date date;

    public Note() {
        this(UUID.randomUUID());
    }

    private Note(@NonNull UUID id) {
        this.id = id;
        date = new Date();
    }

    public String getPhotoFilename() {
        return "IMG_" + id.toString() + ".jpg";
    }
}