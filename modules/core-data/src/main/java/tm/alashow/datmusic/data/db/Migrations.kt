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
internal class PlaylistRenameIdMigration : AutoMigrationSpec

@DeleteColumn.Entries(
    DeleteColumn(tableName = "albums", columnName = "subtitle"),
    DeleteColumn(tableName = "albums", columnName = "plays"),
    DeleteColumn(tableName = "albums", columnName = "followers"),
    DeleteColumn(tableName = "albums", columnName = "create_time"),
    DeleteColumn(tableName = "albums", columnName = "update_time"),
    DeleteColumn(tableName = "albums", columnName = "genres"),
)
internal class AlbumDeleteOldColumnsMigration : AutoMigrationSpec

@DeleteColumn(tableName = "albums", columnName = "albumId")
internal class AlbumDeleteAlbumIdColumnMigration : AutoMigrationSpec

@RenameColumn(tableName = "albums", fromColumnName = "owner_id", toColumnName = "artist_id")
internal class RenameAlbumOwnerIdToArtistIdColumnMigration : AutoMigrationSpec

@DeleteColumn(tableName = "download_requests", columnName = "entity_id")
internal class DownloadRequestDeleteEntityIdColumnMigration : AutoMigrationSpec
