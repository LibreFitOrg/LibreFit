package org.librefit.di.streamProvider

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class StreamProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StreamProvider {
    override fun getInputStream(uri: Uri): InputStream? {
        return context.contentResolver.openInputStream(uri)
    }

    override fun getOutputStream(uri: Uri): OutputStream? {
        return context.contentResolver.openOutputStream(uri)
    }
}