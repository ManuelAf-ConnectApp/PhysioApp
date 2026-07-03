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

# --- Missing classes detected by R8 ---
-dontwarn com.google.api.client.http.GenericUrl
-dontwarn com.google.api.client.http.HttpHeaders
-dontwarn com.google.api.client.http.HttpRequest
-dontwarn com.google.api.client.http.HttpRequestFactory
-dontwarn com.google.api.client.http.HttpResponse
-dontwarn com.google.api.client.http.HttpTransport
-dontwarn com.google.api.client.http.javanet.NetHttpTransport$Builder
-dontwarn com.google.api.client.http.javanet.NetHttpTransport
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.InlineMe
-dontwarn com.google.errorprone.annotations.RestrictedApi
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy
-dontwarn javax.annotation.concurrent.ThreadSafe
-dontwarn org.joda.time.Instant
