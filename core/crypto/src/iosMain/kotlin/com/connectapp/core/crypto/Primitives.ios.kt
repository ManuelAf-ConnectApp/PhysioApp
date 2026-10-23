@file:OptIn(ExperimentalForeignApi::class)

package com.connectapp.core.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCKeyDerivationPBKDF
import platform.CoreCrypto.kCCPBKDF2
import platform.CoreCrypto.kCCPRFHmacAlgSHA256
import platform.CoreCrypto.kCCSuccess
import platform.Foundation.NSString
import platform.Foundation.precomposedStringWithCanonicalMapping
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

actual fun secureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    if (size == 0) return bytes
    val status = bytes.usePinned { pinned ->
        SecRandomCopyBytes(kSecRandomDefault, size.convert(), pinned.addressOf(0))
    }
    check(status == errSecSuccess) { "SecRandomCopyBytes failed with status $status" }
    return bytes
}

// CommonCrypto's `const char *password` is bridged by Kotlin/Native as String?, which it passes
// as a UTF-8 C string — the same encoding the Android implementation uses. passwordLen is the
// UTF-8 byte length, so CommonCrypto never relies on the terminating NUL.
actual fun pbkdf2HmacSha256(password: String, salt: ByteArray, iterations: Int, keyLengthBytes: Int): ByteArray {
    require(salt.isNotEmpty()) { "salt must not be empty" }
    val passwordLength = password.encodeToByteArray().size
    val derived = ByteArray(keyLengthBytes)
    val status = derived.usePinned { out ->
        salt.usePinned { saltPinned ->
            CCKeyDerivationPBKDF(
                algorithm = kCCPBKDF2,
                password = password,
                passwordLen = passwordLength.convert(),
                salt = saltPinned.addressOf(0).reinterpret(),
                saltLen = salt.size.convert(),
                prf = kCCPRFHmacAlgSHA256,
                rounds = iterations.convert(),
                derivedKey = out.addressOf(0).reinterpret(),
                derivedKeyLen = keyLengthBytes.convert(),
            )
        }
    }
    check(status == kCCSuccess) { "CCKeyDerivationPBKDF failed with status $status" }
    return derived
}

@Suppress("CAST_NEVER_SUCCEEDS")
actual fun normalizeNfc(value: String): String =
    (value as NSString).precomposedStringWithCanonicalMapping
