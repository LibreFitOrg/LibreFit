/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.util.textFieldTransformations

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.delete
import androidx.compose.runtime.Immutable
import androidx.core.text.isDigitsOnly


@Immutable
class TimeInputTransformation(
    useAlsoHours: Boolean = false
) : InputTransformation {
    // It means the number of digits in either MM:SS (4) or HH:MM:SS (6)
    val numberOfDigits = if (useAlsoHours) 6 else 4

    override fun TextFieldBuffer.transformInput() {
        if (asCharSequence().isDigitsOnly()) {
            if (length > numberOfDigits) {
                // Delete digits (from start) until result has length equal to `numberOfDigits`
                delete(0, length - numberOfDigits)
            }
        } else {
            revertAllChanges()
        }
    }
}