package com.fed.notes;

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

    public Note(UUID id) {
        this.id = id;
        date = new Date();
    }

//    public UUID getId() {
//        return id;
//    }
//
//    public void setId(UUID id) {
//        id = id;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        title = title;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        description = description;
//    }
//
//    public Date getDate() {
//        return date;
//    }
//
//    public void setDate(long date) {
//        date = new Date(date);
//    }

    public String getPhotoFilename() {
        return "IMG_" + id.toString() + ".jpg";
    }
}