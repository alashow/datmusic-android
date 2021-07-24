/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import tm.alashow.datmusic.data.repos.album.DatmusicAlbumParams
import tm.alashow.datmusic.data.repos.artist.DatmusicArtistParams
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.domain.entities.Genre

object AppTypeConverters {

    @TypeConverter
    @JvmStatic
    fun fromDatmusicSearchParams(params: DatmusicSearchParams) = params.toString()

    @TypeConverter
    @JvmStatic
    fun fromArtistSearchParams(params: DatmusicArtistParams) = params.toString()

    @TypeConverter
    @JvmStatic
    fun fromAlbumSearchParams(params: DatmusicAlbumParams) = params.toString()

    @TypeConverter
    @JvmStatic
    fun toAudioList(value: String): List<Audio> = Json.decodeFromString(ListSerializer(Audio.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun fromAudioList(value: List<Audio>): String = Json.encodeToString(ListSerializer(Audio.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun toArtistList(value: String): List<Artist> = Json.decodeFromString(ListSerializer(Artist.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun fromArtistList(value: List<Artist>): String = Json.encodeToString(ListSerializer(Artist.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun toAlbumList(value: String): List<Album> = Json.decodeFromString(ListSerializer(Album.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun fromAlbumList(value: List<Album>): String = Json.encodeToString(ListSerializer(Album.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun toAlbumPhoto(value: String): Album.Photo = Json.decodeFromString(Album.Photo.serializer(), value)

    @TypeConverter
    @JvmStatic
    fun fromAlbumPhoto(value: Album.Photo): String = Json.encodeToString(Album.Photo.serializer(), value)

    @TypeConverter
    @JvmStatic
    fun toArtistPhoto(value: String): List<Artist.Photo> = Json.decodeFromString(ListSerializer(Artist.Photo.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun fromArtistPhoto(value: List<Artist.Photo>?): String = Json.encodeToString(ListSerializer(Artist.Photo.serializer()), value ?: emptyList())

    @TypeConverter
    @JvmStatic
    fun toGenres(value: String): List<Genre> = Json.decodeFromString(ListSerializer(Genre.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun fromGenres(value: List<Genre>): String = Json.encodeToString(ListSerializer(Genre.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun toDownloadType(value: String): DownloadRequest.Type = DownloadRequest.Type.from(value)

    @TypeConverter
    @JvmStatic
    fun fromDownloadType(value: DownloadRequest.Type): String = value.name
}
