/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.base.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

private fun Intent.addReadWriteFlags() = apply {
    addFlags(
        Intent.FLAG_GRANT_READ_URI_PERMISSION
            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
    )
}

class WriteableOpenDocumentTree : ActivityResultContracts.OpenDocumentTree() {
    override fun createIntent(context: Context, input: Uri?): Intent {
        return super.createIntent(context, input).addReadWriteFlags()
    }
}

class CreateFileContract(val params: Params) : ActivityResultContracts.OpenDocument() {
    data class Params(
        val suggestedName: String,
        val fileExtension: String,
        val fileMimeType: String,
    )

    override fun createIntent(context: Context, input: Array<String>): Intent {
        return super.createIntent(context, input).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            action = Intent.ACTION_CREATE_DOCUMENT
            type = params.fileMimeType
            putExtra(Intent.EXTRA_TITLE, "${params.suggestedName}.${params.fileExtension}")
        }
    }
}
