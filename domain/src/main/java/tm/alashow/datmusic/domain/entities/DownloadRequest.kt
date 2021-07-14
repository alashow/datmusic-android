/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tm.alashow.domain.models.Entity as BaseEntity

@Entity(tableName = "downloads")
data class DownloadRequest(
    @ColumnInfo(name = "entity_id")
    val entityId: String = "",

    @ColumnInfo(name = "entity_type")
    val entityType: Type = Type.Audio,

    @ColumnInfo(name = "request_id")
    val requestId: Int = REQUEST_NOT_SET,

    @PrimaryKey
    @ColumnInfo(name = "id")
    override val id: String = entityId,

    @ColumnInfo(name = "params")
    override var params: String = "",

) : BaseEntity {

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
