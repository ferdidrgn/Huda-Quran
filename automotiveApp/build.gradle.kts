import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

val releaseKeystorePath = System.getenv("ANDROID_KEYSTORE_PATH")
val releaseKeystorePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val releaseKeyAlias = System.getenv("ANDROID_KEY_ALIAS")
val releaseKeyPassword = System.getenv("ANDROID_KEY_PASSWORD")
val ciRunNumber = providers.environmentVariable("GITHUB_RUN_NUMBER").orNull?.toLongOrNull()
if (ciRunNumber != null) {
    require(ciRunNumber in 1..524_999_999) { "GITHUB_RUN_NUMBER is outside Android's supported versionCode range." }
}
val automotiveCiVersionCode = ciRunNumber?.times(4)?.plus(3)?.toInt() ?: 33
val hasReleaseSigning = !releaseKeystorePath.isNullOrBlank() &&
    !releaseKeystorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

tasks.configureEach {
    if (name.startsWith("validateSigning") && name.endsWith("Release")) {
        doFirst {
            check(hasReleaseSigning) {
                "Automotive release signing requires the existing Kotlin upload keystore environment variables."
            }
        }
    }
}

android {
    namespace = "org.ferdidrgn.hudaquran.automotive"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    androidResources {
        localeFilters += setOf("tr", "en", "ar", "de", "fr", "uz", "ky", "tk")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    defaultConfig {
        applicationId = "org.ferdidrgn.hudaquran"
        minSdk = 28
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // Reserve four unique version codes per CI run across this package's device artifacts.
        versionCode = automotiveCiVersionCode
        versionName = "1.21.5"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {}
        release {
            signingConfig = if (hasReleaseSigning) signingConfigs.getByName("release") else null
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            ndk { debugSymbolLevel = "FULL" }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // The AAOS media-only artifact doesn't need phone monetization, Wear Data Layer, or Firebase.
    // Cast UI/provider metadata is not registered; the shared AudioPlayer currently links the Cast
    // runtime, so retain that SDK until the shared player is split behind a platform interface.
    implementation(project(":shared")) {
        exclude(group = "com.google.android.gms", module = "play-services-ads")
        exclude(group = "com.google.android.gms", module = "play-services-wearable")
        exclude(group = "com.google.firebase")
        exclude(group = "com.revenuecat.purchases")
    }
    // Cast's media transfer backend uses Firebase's small encoder/protobuf utilities. Keep only
    // those required codecs, without pulling AAOS Firebase analytics/messaging/crash reporting.
    implementation("com.google.firebase:firebase-encoders")
    implementation("com.google.firebase:firebase-encoders-json:18.0.1")
    implementation("com.google.firebase:firebase-encoders-proto:16.0.0")
}
