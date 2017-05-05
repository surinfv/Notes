package com.fed.notes;

import java.util.UUID;

/**
 * Created by f on 05.05.2017.
 */

public class Note {
    private UUID mId;
    private String mTitle;
    private String mDiscription;

    public Note() {
        mId = UUID.randomUUID();
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

    public String getDiscription() {
        return mDiscription;
    }

    public void setDiscription(String TDiscription) {
        mDiscription = TDiscription;
    }
}