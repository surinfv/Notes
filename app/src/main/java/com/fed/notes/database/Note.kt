package com.fed.notes.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.util.Date
import java.util.UUID

/**
 * Created by f on 05.05.2017.
 */

@Entity(tableName = "notes")
class Note private constructor(@field:PrimaryKey
                               var id: UUID) {
    constructor() : this(UUID.randomUUID())

    var title: String? = null
    var description: String? = null
    var date: Date = Date()
    val photoFilename: String
        get() = "IMG_" + id.toString() + ".jpg"
}