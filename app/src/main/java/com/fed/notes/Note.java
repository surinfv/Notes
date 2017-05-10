package com.fed.notes;

import java.util.Date;
import java.util.UUID;

/**
 * Created by f on 05.05.2017.
 */

public class Note {
    private UUID mId;
    private String mTitle;
    private String mDescription;
    private Date mDate;

    public Note() {
        mId = UUID.randomUUID();
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String TDiscription) {
        mDescription = TDiscription;
    }

    public Date getDate() {
        return mDate;
    }
}