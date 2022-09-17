/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.playlist

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import tm.alashow.base.util.RemoteLogger
import tm.alashow.base.util.extensions.randomUUID
import tm.alashow.datmusic.domain.entities.PlaylistId

enum class ArtworkImageFolderType(private val path: String) {
    PLAYLIST("playlist");

    fun getArtworkImageFolder(context: Context, prefix: String = "images"): File {
        val folder = File(context.applicationInfo.dataDir, "${File.separator}${prefix}${File.separator}${this.path}/")
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                RemoteLogger.exception(IOException("Failed to create playlists artwork folder: $folder"))
            }
        }
        return folder
    }
}

enum class ArtworkImageFileType(val prefix: String) {
    PLAYLIST_AUTO_GENERATED("artwork_auto_generated"), PLAYLIST_USER_SET("artwork_user_custom");

    companion object {
        fun String?.checkArtworkPathType(type: ArtworkImageFileType) = values().firstOrNull { this != null && this.contains(it.prefix) } == type
        fun String?.isUserSetArtworkPath() = checkArtworkPathType(PLAYLIST_USER_SET)
    }
}

object PlaylistArtworkUtils {

    private const val IMAGE_SIZE = 1600

    fun joinImages(list: List<Bitmap>): Bitmap {
        assert(list.isNotEmpty())
        val arranged = arrangeBitmaps(list)

        return create(
            arranged,
            IMAGE_SIZE
        )
    }

    private fun arrangeBitmaps(list: List<Bitmap>): List<Bitmap> {
        return when (list.size) {
            4 -> list
            else -> listOf(list.first())
        }
    }

    private fun create(images: List<Bitmap>, imageSize: Int): Bitmap {
        val parts = when (images.size) {
            1 -> 1
            else -> 2
        }
        val result = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val onePartSize = imageSize / parts

        images.forEachIndexed { i, bitmap ->
            val bit = Bitmap.createScaledBitmap(bitmap, onePartSize, onePartSize, true)
            canvas.drawBitmap(bit, (onePartSize * (i % parts)).toFloat(), (onePartSize * (i / parts)).toFloat(), paint)
            bit.recycle()
        }

        return result
    }

    private fun PlaylistId.getPlaylistArtworkImageFile(context: Context, type: ArtworkImageFileType, extension: String = ".webp"): File {
        val folder = ArtworkImageFolderType.PLAYLIST.getArtworkImageFolder(context)
        val random = randomUUID()
        return File(folder, "${type.prefix}_${this}_${random}$extension")
    }

    fun PlaylistId.savePlaylistArtwork(
        context: Context,
        bitmap: Bitmap,
        type: ArtworkImageFileType,
        recycle: Boolean = true,
    ): File {
        val dest = getPlaylistArtworkImageFile(context, type)
        val out = FileOutputStream(dest)
        bitmap.compress(Bitmap.CompressFormat.WEBP, 90, out)
        if (recycle) bitmap.recycle()
        out.close()
        return dest
    }
}
