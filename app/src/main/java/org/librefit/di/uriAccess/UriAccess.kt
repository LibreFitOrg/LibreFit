package org.librefit.di.uriAccess

import android.net.Uri
import java.io.InputStream
import java.io.OutputStream

interface UriAccess {
    fun takePersistableReadPermission(uri: Uri)
}