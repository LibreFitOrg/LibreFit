package org.librefit.di.uriAccess

import android.content.Context
import android.content.Intent
import android.net.Uri

class UriAccessImpl(
    private val context: Context,
) : UriAccess {

    override fun takePersistableReadPermission(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}