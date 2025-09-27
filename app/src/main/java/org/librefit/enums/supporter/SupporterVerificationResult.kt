/*
 * Copyright (c) 2025. LibreFit
 *
 * This file is part of LibreFit
 *
 * LibreFit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LibreFit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LibreFit.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.librefit.enums.supporter

enum class SupporterVerificationResult {
    VALID_COMPANION_APP_SIGNATURE,
    INVALID_COMPANION_APP_SIGNATURE,
    LIBREFIT_APP_NOT_FOUND,
    LIBREFIT_APP_SIGNATURE_ERROR,
    COMPANION_APP_NOT_FOUND,
    COMPANION_APP_SIGNATURE_ERROR,

    VALID_CODE,
    INVALID_CODE,
    PUBLIC_KEY_NOT_INITIALIZED_PROPERLY,
    INVALID_PUBLIC_KEY,
    INAPPROPRIATE_PUBLIC_KEY_STRING,
    ALGORITHM_NOT_AVAILABLE,
    INVALID_SIGNATURE_ENCODING,
    MALFORMED_CODE,
    MISSING_DOT_SEPARATOR,

    INVALID_PUBLIC_KEY_ENCODING,

    UNKNOWN_ERROR
}