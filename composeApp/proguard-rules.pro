# Android ProGuard Rules

# --- Kotlin Multiplatform & Compose ---
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions, SourceFile, LineNumberTable

# --- Koin (Dependency Injection) ---
-keep class org.koin.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}

# --- SQLDelight ---
-keep class app.cash.sqldelight.** { *; }
# Mantener las clases generadas de la base de datos
-keep class com.connectapp.data.database.** { *; }

# --- Kotlinx Serialization ---
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# --- DataStore & Okio ---
-keep class androidx.datastore.** { *; }
-keep class okio.** { *; }

# --- AndroidX Security (Crypto) ---
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# --- General project persistence/domain classes ---
# Asegura que las clases que se serializan o se inyectan no se pierdan
-keep class com.connectapp.domain.model.** { *; }
-keep class com.connectapp.data.model.** { *; }

# Mantener ViewModels si se usan por reflexión o Koin
-keep class * extends androidx.lifecycle.ViewModel { *; }
