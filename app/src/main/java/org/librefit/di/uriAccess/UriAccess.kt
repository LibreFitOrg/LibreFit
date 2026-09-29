package org.librefit.di.uriAccess

import android.net.Uri

interface UriAccess {
    fun takePersistableReadPermission(uri: Uri)
}