package com.fed.notes.database;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Fedor SURIN on 26.10.2017.
 */

public class Converter {

    @TypeConverter
    public static Date timestampToDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long dbToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static UUID dbToUUID(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, UUID.class);

    }
    @TypeConverter
    public static String uuidToDb(UUID uuid) {
        Gson gson = new Gson();
        return gson.toJson(uuid);
    }
}
