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

package org.librefit.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import org.librefit.enums.supporter.SignatureVerificationResult
import org.librefit.enums.supporter.SupporterVerificationResult
import org.librefit.util.SupporterVerifier.TEMP_PUBLIC_KEY_STRING
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

object SupporterVerifier {
    private const val COMPANION_PACKAGE_NAME = "org.librefit.companion"

    /**
     * Verifies that the companion app is installed and signed with the same key as this app.
     *
     */
    fun verifyCompanionApp(context: Context): SupporterVerificationResult {
        return runCatching {
            val appSignatures =
                when (val result = getAppSignatures(context, context.packageName)) {
                    SignatureVerificationResult.NoSignatureFound -> return SupporterVerificationResult.LIBREFIT_APP_SIGNATURE_ERROR
                    SignatureVerificationResult.PackageNotFound -> return SupporterVerificationResult.LIBREFIT_APP_NOT_FOUND
                    SignatureVerificationResult.UnknownError -> return SupporterVerificationResult.UNKNOWN_ERROR
                    is SignatureVerificationResult.Success -> result.signatures
                }

            val supporterAppSignatures =
                when (val result = getAppSignatures(context, COMPANION_PACKAGE_NAME)) {
                    SignatureVerificationResult.NoSignatureFound -> return SupporterVerificationResult.COMPANION_APP_SIGNATURE_ERROR
                    SignatureVerificationResult.PackageNotFound -> return SupporterVerificationResult.COMPANION_APP_NOT_FOUND
                    SignatureVerificationResult.UnknownError -> return SupporterVerificationResult.UNKNOWN_ERROR
                    is SignatureVerificationResult.Success -> result.signatures
                }

            appSignatures == supporterAppSignatures
        }
            .fold(
                onSuccess = { verified ->
                    if (verified) SupporterVerificationResult.VALID_COMPANION_APP_SIGNATURE else SupporterVerificationResult.INVALID_COMPANION_APP_SIGNATURE
                },
                onFailure = { SupporterVerificationResult.UNKNOWN_ERROR }
            )
    }

    private fun getAppSignatures(
        context: Context,
        packageName: String
    ): SignatureVerificationResult {
        val packageManager = context.packageManager
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo?.apkContentsSigners?.toSet()
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                ).signatures?.toSet()
            }
        }
            .fold(
                onSuccess = { signatures ->
                    if (signatures.isNullOrEmpty()) {
                        SignatureVerificationResult.NoSignatureFound
                    } else {
                        SignatureVerificationResult.Success(signatures)
                    }
                },
                onFailure = { throwable ->
                    return when (throwable) {
                        is PackageManager.NameNotFoundException -> SignatureVerificationResult.PackageNotFound
                        else -> SignatureVerificationResult.UnknownError
                    }
                }
            )
    }


    private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    private const val KEY_ALGORITHM = "EC"
    private const val CODE_SEPARATOR = "."


    /**
     * Loads a PublicKey from a Base64-encoded X.509 string.
     */
    fun loadPublicKey(base64PublicKey: String): PublicKey {
        val keyBytes = Base64.getDecoder().decode(base64PublicKey)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
        return keyFactory.generatePublic(keySpec)
    }

    // TODO: change this temporary public key
    const val TEMP_PUBLIC_KEY_STRING =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEhUpEordwrpsQ7JGkL0BHSMg9sySXP7Q7S2FWBJqLua8ZzsNSTRkgvef0Va987bwwN2fXfCBStRjGsBy3Sg4kJg=="

    /**
     * Verifies a [code] string using the [TEMP_PUBLIC_KEY_STRING].
     * @param code The code string with the format `id.signature_of_id`.
     * @return A [SupporterVerificationResult] for each possible outcome.
     */
    fun verifyCode(code: String): SupporterVerificationResult {
        return runCatching {
            val separatorIndex = code
                .lastIndexOf(CODE_SEPARATOR)
                .takeIf { it != -1 }
                ?: return SupporterVerificationResult.MISSING_DOT_SEPARATOR

            val id = code.take(separatorIndex)
            val encodedSignature = code.substring(separatorIndex + 1)

            if (id.isEmpty() || encodedSignature.isEmpty()) return SupporterVerificationResult.MALFORMED_CODE

            // Decode the signature from Base64
            val signatureBytes = runCatching {
                Base64.getUrlDecoder().decode(encodedSignature)
            }.getOrElse {
                return if (it is IllegalArgumentException) {
                    SupporterVerificationResult.INVALID_SIGNATURE_ENCODING
                } else {
                    SupporterVerificationResult.UNKNOWN_ERROR
                }
            }

            // Get an instance of the Signature object
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM)

            // Initialize it for verification
            runCatching {
                signature.initVerify(loadPublicKey(TEMP_PUBLIC_KEY_STRING))
            }.onFailure {
                return if (it is IllegalArgumentException) {
                    SupporterVerificationResult.INVALID_PUBLIC_KEY_ENCODING
                } else {
                    SupporterVerificationResult.UNKNOWN_ERROR
                }
            }

            // Add the original data that was signed
            signature.update(id.toByteArray(Charsets.UTF_8))

            // Perform the verification
            signature.verify(signatureBytes)
        }
            .fold(
                onSuccess = { verified ->
                    if (verified) SupporterVerificationResult.VALID_CODE else SupporterVerificationResult.INVALID_CODE
                },
                onFailure = { throwable ->
                    when (throwable) {
                        is SignatureException -> SupporterVerificationResult.PUBLIC_KEY_NOT_INITIALIZED_PROPERLY
                        is InvalidKeyException -> SupporterVerificationResult.INVALID_PUBLIC_KEY
                        is InvalidKeySpecException -> SupporterVerificationResult.INAPPROPRIATE_PUBLIC_KEY_STRING
                        is NoSuchAlgorithmException -> SupporterVerificationResult.ALGORITHM_NOT_AVAILABLE
                        else -> SupporterVerificationResult.UNKNOWN_ERROR
                    }
                }
            )
    }
}