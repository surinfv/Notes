package com.fed.notes.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Fedor SURIN on 13.12.2017.
 */
fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}