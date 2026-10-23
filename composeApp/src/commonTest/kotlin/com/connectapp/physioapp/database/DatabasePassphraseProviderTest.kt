package com.connectapp.physioapp.database

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private class InMemorySecretStore(var failWrites: Boolean = false) : SecretStore {
    val values = mutableMapOf<String, String>()
    override fun read(key: String): String? = values[key]
    override fun write(key: String, value: String): Boolean {
        if (failWrites) return false
        values[key] = value
        return true
    }
}

class DatabasePassphraseProviderTest {

    @Test
    fun `first use generates a 256-bit hex passphrase and stores it`() {
        val store = InMemorySecretStore()
        val passphrase = DatabasePassphraseProvider(store).getOrCreate()

        assertEquals(64, passphrase.length)
        assertTrue(passphrase.all { it in '0'..'9' || it in 'a'..'f' })
        assertEquals(passphrase, store.values[DatabasePassphraseProvider.KEY])
    }

    @Test
    fun `later uses return the stored passphrase and never regenerate it`() {
        val store = InMemorySecretStore()
        var generated = 0
        val provider = DatabasePassphraseProvider(store) { size -> generated++; ByteArray(size) { 7 } }

        val first = provider.getOrCreate()
        val second = provider.getOrCreate()

        assertEquals(first, second)
        assertEquals(1, generated)
    }

    @Test
    fun `a keystore write failure is reported instead of returning an unsaved key`() {
        assertFailsWith<IllegalStateException> {
            DatabasePassphraseProvider(InMemorySecretStore(failWrites = true)).getOrCreate()
        }
    }
}
