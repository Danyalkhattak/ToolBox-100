# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Compose classes
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep Room entities
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn org.sqlite.**

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn com.google.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep data model classes
-keep class com.dannyk.toolbox.data.local.entity.** { *; }
