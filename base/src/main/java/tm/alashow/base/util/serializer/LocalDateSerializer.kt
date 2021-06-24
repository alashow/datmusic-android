/*
 * Copyright (C) 2020, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

@ExperimentalSerializationApi
@Serializer(forClass = LocalDate::class)
object LocalDateSerializer : KSerializer<LocalDate> {
    private val DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("localDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(DEFAULT_FORMATTER.format(value))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val string = decoder.decodeString().trim { it <= ' ' }

        if (string.isNotEmpty()) {
            return LocalDate.parse(string, DEFAULT_FORMATTER)
        }
        return LocalDate.now()
    }
}
