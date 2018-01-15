package com.fed.notes.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import com.fed.notes.BuildConfig
import java.io.File

object UriFetcherUtil {
    fun getUri(context: Context, photoFile: File?): Uri =
            if (Build.VERSION.SDK_INT > 23) {
                FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", photoFile)
            } else {
                Uri.fromFile(photoFile)
            }
}