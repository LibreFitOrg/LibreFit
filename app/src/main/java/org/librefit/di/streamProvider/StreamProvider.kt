package org.librefit.di.streamProvider

import android.net.Uri
import java.io.InputStream
import java.io.OutputStream

interface StreamProvider {
    fun getInputStream(uri: Uri): InputStream?
    fun getOutputStream(uri: Uri): OutputStream?
}