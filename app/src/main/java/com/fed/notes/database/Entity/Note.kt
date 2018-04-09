package com.fed.notes.database.Entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.util.Date
import java.util.UUID

@Entity(tableName = "notes")
class Note private constructor(@field:PrimaryKey
                               var id: UUID,
                               var title: String? = null,
                               var description: String? = null,
                               var date: Date = Date()) {

    constructor() : this(UUID.randomUUID())

    fun getPhotoFilename() = "IMG_" + id.toString() + ".jpg"
}