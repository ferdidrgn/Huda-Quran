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

# --- kotlinx.serialization: keep generated serializers + companion members ---
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
