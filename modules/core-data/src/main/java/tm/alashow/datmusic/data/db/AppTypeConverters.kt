/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import tm.alashow.datmusic.data.DatmusicAlbumParams
import tm.alashow.datmusic.data.DatmusicArtistParams
import tm.alashow.datmusic.data.DatmusicSearchParams
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.domain.entities.Genre
import tm.alashow.domain.models.DEFAULT_JSON_FORMAT

object AppTypeConverters {

    private val json = DEFAULT_JSON_FORMAT

    @TypeConverter
    @JvmStatic
    fun fromArtistSearchParams(params: DatmusicArtistParams) = params.toString()

    @TypeConverter
    @JvmStatic
    fun fromDatmusicSearchParams(params: DatmusicSearchParams) = params.toString()

    @TypeConverter
    @JvmStatic
    fun fromAlbumSearchParams(params: DatmusicAlbumParams) = params.toString()

    @TypeConverter
    @JvmStatic
    fun toAudioList(value: String): List<Audio> = json.decodeFromString(ListSerializer(Audio.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun fromAudioList(value: List<Audio>): String = json.encodeToString(ListSerializer(Audio.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun toArtistList(value: String): List<Artist> = json.decodeFromString(ListSerializer(Artist.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun fromArtistList(value: List<Artist>): String = json.encodeToString(ListSerializer(Artist.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun toAlbumList(value: String): List<Album> = json.decodeFromString(ListSerializer(Album.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun fromAlbumList(value: List<Album>): String = json.encodeToString(ListSerializer(Album.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun toAlbumPhoto(value: String): Album.Photo = json.decodeFromString(Album.Photo.serializer(), value)

    @TypeConverter
    @JvmStatic
    fun fromAlbumPhoto(value: Album.Photo): String = json.encodeToString(Album.Photo.serializer(), value)

    @TypeConverter
    @JvmStatic
    fun toArtistPhoto(value: String): List<Artist.Photo> = json.decodeFromString(ListSerializer(Artist.Photo.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun fromArtistPhoto(value: List<Artist.Photo>?): String = json.encodeToString(ListSerializer(Artist.Photo.serializer()), value ?: emptyList())

    @TypeConverter
    @JvmStatic
    fun toGenres(value: String): List<Genre> = json.decodeFromString(ListSerializer(Genre.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun fromGenres(value: List<Genre>): String = json.encodeToString(ListSerializer(Genre.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun toDownloadType(value: String): DownloadRequest.Type = DownloadRequest.Type.from(value)

    @TypeConverter
    @JvmStatic
    fun fromDownloadType(value: DownloadRequest.Type): String = value.name
}
