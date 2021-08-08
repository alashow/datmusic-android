/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloader

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

class WriteableOpenDocumentTree : ActivityResultContracts.OpenDocumentTree() {
    override fun createIntent(context: Context, input: Uri?): Intent {
        return super.createIntent(context, input).addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        )
    }
}
