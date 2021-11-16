/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db

import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE playlist_audios RENAME COLUMN `index` TO position;")
    }
}

@DeleteColumn(tableName = "playlists", columnName = "id")
@RenameColumn(
    tableName = "playlists",
    fromColumnName = "_id",
    toColumnName = "id"
)
class PlaylistRenameIdMigration : AutoMigrationSpec

@DeleteColumn.Entries(
    DeleteColumn(tableName = "albums", columnName = "subtitle"),
    DeleteColumn(tableName = "albums", columnName = "plays"),
    DeleteColumn(tableName = "albums", columnName = "followers"),
    DeleteColumn(tableName = "albums", columnName = "create_time"),
    DeleteColumn(tableName = "albums", columnName = "update_time"),
    DeleteColumn(tableName = "albums", columnName = "genres"),
)
class AlbumDeleteOldColumnsMigration : AutoMigrationSpec
