# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- kotlinx.serialization: official rules (github.com/Kotlin/kotlinx.serialization) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class org.ferdidrgn.hudaquran.**$$serializer { *; }
-keepclassmembers class org.ferdidrgn.hudaquran.** {
    *** Companion;
}
-keepclasseswithmembers class org.ferdidrgn.hudaquran.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Also keep every @Serializable model itself (fields + the class), not just its generated
# serializer — R8 can otherwise rename/remove fields the serializer reflects over at runtime,
# which throws only when that exact code path executes (e.g. parsing a real API response), not
# at compile time. This is the single most common "works in debug, crashes in release" cause for
# a Ktor + kotlinx.serialization app.
-keep,includedescriptorclasses @kotlinx.serialization.Serializable class org.ferdidrgn.hudaquran.** { *; }
-if @kotlinx.serialization.Serializable class org.ferdidrgn.hudaquran.**
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Ktor client: reflection-heavy, a very common release-only crash source ---
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Firebase Crashlytics & Analytics ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keep public class * extends java.lang.Exception
-keep class com.google.android.gms.measurement.** { *; }
# --- Firebase & AdMob Koruma Kuralları ---
-keep class com.google.android.gms.measurement.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.ads.**

# --- AdMob & Reklam Kimliği (AD_ID) Koruma Kuralları ---
-keep class com.google.android.gms.ads.** { *; }
-keep public class com.google.android.gms.ads.identifier.** { *; }
-dontwarn com.google.android.gms.ads.**

# --- Google Play Billing Library ---
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# --- Media3 / ExoPlayer (usually ships its own consumer rules, kept defensively too) ---
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# --- multiplatform-settings ---
-keep class com.russhwolf.settings.** { *; }
-dontwarn com.russhwolf.settings.**

# --- WorkManager / Room (WorkManager builds its internal WorkDatabase via Room at app startup,
# through the androidx.startup.InitializationProvider ContentProvider — this runs before any
# Activity, so a missing keep rule here crashes the app immediately on launch in release builds) ---
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**
-keep class androidx.startup.** { *; }
-dontwarn androidx.startup.**
