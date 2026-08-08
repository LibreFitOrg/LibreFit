package org.librefit.di.uriAccess

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UriAccessImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UriAccess {

    override fun takePersistableReadPermission(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}