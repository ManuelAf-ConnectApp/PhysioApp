package com.connectapp.data.storage

import platform.Foundation.*
import platform.Security.*
import platform.CoreCrypto.*
import kotlinx.cinterop.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
class IosCryptoManager : CryptoManager {

    private val keyBytes = "fixed-key-for-now-123456789012345".encodeToByteArray() // 32 bytes for AES-256

    override fun encrypt(value: String): String {
        val dataToEncrypt = value.encodeToByteArray()
        val encryptedData = crypt(dataToEncrypt, kCCEncrypt)
        return encryptedData?.let { Base64.encode(it) } ?: value
    }

    override fun decrypt(value: String): String {
        return try {
            val dataToDecrypt = Base64.decode(value)
            val decryptedData = crypt(dataToDecrypt, kCCDecrypt)
            decryptedData?.decodeToString() ?: value
        } catch (e: Exception) {
            value
        }
    }

    private fun crypt(data: ByteArray, operation: CCOperation): ByteArray? = memScoped {
        val keyData = keyBytes.toCValues()
        val dataIn = data.toCValues()
        val dataOut = allocArray<ByteVar>(data.size + kCCBlockSizeAES128.toInt())
        val dataOutMoved = alloc<size_tVar>()

        val status = CCCrypt(
            operation,
            kCCAlgorithmAES,
            kCCOptionPKCS7Padding,
            keyData,
            kCCKeySizeAES256.toULong(),
            null, 
            dataIn,
            data.size.toULong(),
            dataOut,
            (data.size + kCCBlockSizeAES128.toInt()).toULong(),
            dataOutMoved.ptr
        )

        if (status == kCCSuccess) {
            dataOut.readBytes(dataOutMoved.value.toInt())
        } else {
            null
        }
    }
}
