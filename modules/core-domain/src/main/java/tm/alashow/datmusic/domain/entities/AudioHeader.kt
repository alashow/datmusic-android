/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import android.media.MediaFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class AudioHeader(
    val mime: String = "",
    val format: String = "",
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val channelCount: Int = 0,
    val durationMicros: Long = 0
) {
    fun info(): String {
        val otherSymbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#.#", otherSymbols)
        val sample = "%s kHz".format(df.format((sampleRate.toFloat() / 1000f)))
        val bitrate = "%.0f kbps".format((bitrate.toFloat() / 1000))
        return "$sample $bitrate $format".uppercase()
    }

    companion object {
        private fun MediaFormat.integer(key: String, default: Int): Int = try {
            getInteger(key)
        } catch (e: Exception) {
            default
        }

        private fun MediaFormat.string(key: String, default: String): String = try {
            getString(key) ?: default
        } catch (e: Exception) {
            default
        }

        private fun MediaFormat.long(key: String, default: Long): Long = try {
            getLong(key)
        } catch (e: Exception) {
            default
        }

        fun from(audioDownloadItem: AudioDownloadItem, mediaFormat: MediaFormat): AudioHeader {
            var header = AudioHeader(
                mime = mediaFormat.string(MediaFormat.KEY_MIME, "audio/mpeg"),
                bitrate = mediaFormat.integer(MediaFormat.KEY_BIT_RATE, 0),
                sampleRate = mediaFormat.integer(MediaFormat.KEY_SAMPLE_RATE, 0),
                channelCount = mediaFormat.integer(MediaFormat.KEY_CHANNEL_COUNT, 0),
                durationMicros = mediaFormat.long(MediaFormat.KEY_DURATION, 0L),
            )

            if (header.bitrate == 0) {
                header = header.copy(
                    bitrate = ((audioDownloadItem.downloadInfo.total * 8) / (header.durationMicros / 1E6)).toInt()
                )
            }

            header = header.copy(
                format = when (header.mime) {
                    "audio/mpeg" -> "mp3"
                    "audio/raw", "audio/flac" -> "flac"
                    else -> header.mime.replace("audio/", "")
                }
            )

            return header
        }
    }
}
