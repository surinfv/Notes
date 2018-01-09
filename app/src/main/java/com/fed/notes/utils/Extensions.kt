package com.fed.notes.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fed.notes.R
import java.text.SimpleDateFormat
import java.util.*

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}

fun TextView.showDate(context: Context, date: Date) {
    val dateFormat = SimpleDateFormat(context.getString(R.string.date_format), Locale.ENGLISH)
    text = dateFormat.format(date)
}