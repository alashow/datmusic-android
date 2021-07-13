/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.tonyodev.fetch2.Download
import tm.alashow.domain.models.Entity as BaseEntity

@Entity(tableName = "downloads")
data class DownloadRequest(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,

    @ColumnInfo(name = "entity_id")
    val entityId: String = "",

    @ColumnInfo(name = "entity_type")
    val entityType: Type = Type.Audio,

    @ColumnInfo(name = "request_id")
    val requestId: Int = REQUEST_NOT_SET,

    @ColumnInfo(name = "id")
    override val id: String = _id.toString(),
    @ColumnInfo(name = "params")
    override var params: String = "",
) : BaseEntity {

    @Ignore
    var download: Download? = null

    fun hasRequest() = requestId != REQUEST_NOT_SET

    companion object {
        const val REQUEST_NOT_SET = 0
    }

    enum class Type(val type: String) {
        Audio("audio");

        companion object {
            private val map = values().associateBy { it.toString() }

            fun from(value: String) = map[value.lowercase()] ?: Audio
        }
    }
}
