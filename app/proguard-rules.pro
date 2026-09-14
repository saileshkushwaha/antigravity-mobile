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

# Keep app model classes for serialization
-keep class com.example.antigravity.model.** { *; }
-keep class com.example.antigravity.data.** { *; }

# Keep enum entries for .entries usage
-keepclassmembers enum * {
    **[] $VALUES;
    public * **();
}

# Keep Serializable data classes
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Prevent R8 from stripping interface information
-keep,allowobfuscation,allowshrinking interface kotlinx.coroutines.flow.Flow
-keep,allowobfuscation,allowshrinking interface kotlinx.coroutines.flow.StateFlow
-keep,allowobfuscation,allowshrinking interface kotlinx.coroutines.flow.SharedFlow
