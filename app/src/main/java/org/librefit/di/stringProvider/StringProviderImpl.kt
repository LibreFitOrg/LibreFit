package org.librefit.di.stringProvider

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import org.librefit.R

class StringProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StringProvider {
    override fun importDataFailed(): String =
        context.getString(R.string.import_data_failed)

    override val unsupportedSchemaVersion: String =
        context.getString(R.string.unsupported_schema_version)

    override val importSuccessToast = context.getString(R.string.import_data_success)
    override val importFailedToast = context.getString(R.string.import_data_failed)
    override val exportSuccessToast = context.getString(R.string.export_data_success)
    override val exportFailedToast = context.getString(R.string.export_data_failed)
}
