/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util.extensions

import android.util.Base64
import java.io.*

inline fun <reified T> String.decodeAsBase64Object(): T? {
    val data = Base64.decode(toByteArray(), Base64.DEFAULT)
    val objectInputStream = ObjectInputStream(
        ByteArrayInputStream(data)
    )
    val parsedObject = objectInputStream.readObject()
    objectInputStream.close()

    return if (parsedObject is T) parsedObject
    else null
}

fun Serializable.encodeAsBase64String(): String? {
    val byteArrayOutputStream = ByteArrayOutputStream()
    val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
    objectOutputStream.writeObject(this)
    objectOutputStream.close()
    return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
}
