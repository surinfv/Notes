package com.fed.notes.utils

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Created by Fedor SURIN on 08.12.2017.
 */

object NotesOrderUtil {
    private val NOTES_ORDER = "notesorder"

    fun loadOrder(context: Context): MutableList<UUID> {
        var notesOrder: MutableList<UUID> = ArrayList()
        val shPref = PreferenceManager.getDefaultSharedPreferences(context)
        val json = shPref.getString(NOTES_ORDER, "")
        if (json != "") {
            val gson = Gson()
            val listType = object : TypeToken<ArrayList<UUID>>() {}.type
            notesOrder = gson.fromJson(json, listType)
        }
        return notesOrder
    }

    fun saveOrder(notesOrder: List<UUID>, context: Context) {
        val shPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = shPref.edit()

        val gson = Gson()
        val json = gson.toJson(notesOrder)

        editor.putString(NOTES_ORDER, json)
        editor.apply()
    }

    fun removeNoteFromOrderList(noteID: UUID, context: Context) {
        val notesOrder = loadOrder(context)
        notesOrder.remove(noteID)
        saveOrder(notesOrder, context)
    }
}
