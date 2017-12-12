package com.fed.notes.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Fedor SURIN on 08.12.2017.
 */

public class NotesOrderUtil {
    private static final String NOTES_ORDER = "notesorder";

    public static List<UUID> loadOrder(Context context) {
        List<UUID> notesOrder = new ArrayList<>();
        SharedPreferences shPref = PreferenceManager.getDefaultSharedPreferences(context);
        String json = shPref.getString(NOTES_ORDER, "");
        if (!json.equals("")) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<UUID>>() {
            }.getType();
            notesOrder = gson.fromJson(json, listType);
        }
        return notesOrder;
    }

    public static void saveOrder(List<UUID> notesOrder, Context context) {
        SharedPreferences shPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = shPref.edit();

        Gson gson = new Gson();
        String json = gson.toJson(notesOrder);

        editor.putString(NOTES_ORDER, json);
        editor.apply();
    }

    public static void removeNoteFromOrderList(UUID noteID, Context context) {
        List<UUID> notesOrder = loadOrder(context);
        notesOrder.remove(noteID);
        saveOrder(notesOrder, context);
    }
}
