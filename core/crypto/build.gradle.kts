plugins {
    id("physioapp.kmp.base")
}

// Platform cryptographic primitives (CSPRNG, PBKDF2-HMAC-SHA256, constant-time comparison) and
// the password hasher built on them. No third-party dependencies: Android uses the JCA
// providers bundled with the OS, iOS uses CommonCrypto/Security.framework.
