/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.util.textFieldTransformations

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.placeCursorAtEnd

class TimeOutputTransformation(
    private val useAlsoHours: Boolean = false
) : OutputTransformation {
    // It means the number of digits in either MM:SS (4) or HH:MM:SS (6)
    val numberOfDigits = if (useAlsoHours) 6 else 4

    override fun TextFieldBuffer.transformOutput() {
        // Add padding so length matches numberOfDigits
        if (length < numberOfDigits) {
            insert(0, "0".repeat(numberOfDigits - length))
        }

        // At this point, length is at least numberOfDigits (4 or 6)
        // Output: MM:SS
        insert(2, ":")

        if (useAlsoHours) {
            // Output: HH:MM:SS
            insert(5, ":")
        }

        placeCursorAtEnd()
    }
}
