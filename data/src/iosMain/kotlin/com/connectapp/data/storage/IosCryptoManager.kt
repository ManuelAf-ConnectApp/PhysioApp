package com.connectapp.data.storage

import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.NSDataBase64EncodingEndLineWithLineFeed
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.base64EncodedStringWithOptions

class IosCryptoManager : CryptoManager {
    override fun encrypt(value: String): String {
        // TODO: Implementar cifrado real usando Keychain o CommonCrypto
        // Por ahora usamos una implementación base que permite compilar
        return value 
    }

    override fun decrypt(value: String): String {
        return value
    }
}
