package com.fed.notes.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.content.FileProvider
import com.fed.notes.BuildConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*

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

fun getUri(context: Context, photoFile: File?): Uri =
        if (Build.VERSION.SDK_INT > 23) {
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", photoFile)
        } else {
            Uri.fromFile(photoFile)
        }