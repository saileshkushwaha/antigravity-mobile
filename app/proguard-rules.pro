# Antigravity Mobile Enterprise Production ProGuard / R8 Rules

# Keep Kotlinx Serialization models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <init>(...);
}
-keepclassmembers class com.example.antigravity.model.** {
    *** Companion;
    *** Companion$*;
    kotlinx.serialization.KSerializer serializer(...);
    *;
}

# Keep OkHttp & JSON
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepclassmembers class * extends okhttp3.EventListener {
    <init>(...);
}

# Keep Compose & AndroidX
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepattributes SourceFile,LineNumberTable
