<<<<<<< Updated upstream
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

val releaseKeystorePath: String? = System.getenv("ANDROID_KEYSTORE_PATH")
val releaseKeystorePassword: String? = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val releaseKeyAlias: String? = System.getenv("ANDROID_KEY_ALIAS")
val releaseKeyPassword: String? = System.getenv("ANDROID_KEY_PASSWORD")
val ciRunNumber = providers.environmentVariable("GITHUB_RUN_NUMBER").orNull?.toLongOrNull()
val versionCodeBatch = ciRunNumber?.let { runNumber ->
    require(runNumber >= 105) { "The corrected Android version-code sequence starts at CI run 105." }
    val batch = 26L + (runNumber - 105L) * 4L
    require(batch + 3L <= 2_100_000_000L) { "GITHUB_RUN_NUMBER exceeds Android's supported versionCode range." }
    batch.toInt()
} ?: 26
val wearCiVersionCode = versionCodeBatch + 3
val hasReleaseSigning = !releaseKeystorePath.isNullOrBlank() &&
    !releaseKeystorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

tasks.configureEach {
    if (name.startsWith("validateSigning") && name.endsWith("Release")) {
        doFirst {
            check(hasReleaseSigning) {
                "Wear release signing is not configured. Supply the existing Kotlin keystore through " +
                    "ANDROID_KEYSTORE_PATH, ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS, and ANDROID_KEY_PASSWORD."
            }
        }
    }
}

android {
    namespace = "org.ferdidrgn.hudaquran.wear"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 30
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        applicationId = "org.ferdidrgn.hudaquran"
        versionCode = wearCiVersionCode
        versionName = "1.21.5"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.create("release") {
                    storeFile = file(releaseKeystorePath!!)
                    storePassword = releaseKeystorePassword
                    keyAlias = releaseKeyAlias
                    keyPassword = releaseKeyPassword
                }
            } else null
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Include native dependency symbols in the AAB for Play Console crash diagnostics.
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation("androidx.wear.compose:compose-material3:1.5.0")
    implementation("androidx.wear.compose:compose-foundation:1.5.0")
    implementation("com.google.android.gms:play-services-wearable:20.0.1")
    implementation("androidx.wear.tiles:tiles:1.6.2")
    implementation("androidx.wear.protolayout:protolayout:1.4.2")
    implementation("androidx.wear.protolayout:protolayout-material3:1.4.2")
}

=======
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

val releaseKeystorePath: String? = System.getenv("ANDROID_KEYSTORE_PATH")
val releaseKeystorePassword: String? = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val releaseKeyAlias: String? = System.getenv("ANDROID_KEY_ALIAS")
val releaseKeyPassword: String? = System.getenv("ANDROID_KEY_PASSWORD")
val ciRunNumber = providers.environmentVariable("GITHUB_RUN_NUMBER").orNull?.toLongOrNull()
val versionCodeBatch = ciRunNumber?.let { runNumber ->
    require(runNumber >= 105) { "The corrected Android version-code sequence starts at CI run 105." }
    val batch = 26L + (runNumber - 105L) * 4L
    require(batch + 3L <= 2_100_000_000L) { "GITHUB_RUN_NUMBER exceeds Android's supported versionCode range." }
    batch.toInt()
} ?: 26
val wearCiVersionCode = versionCodeBatch + 3
val hasReleaseSigning = !releaseKeystorePath.isNullOrBlank() &&
    !releaseKeystorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

tasks.configureEach {
    if (name.startsWith("validateSigning") && name.endsWith("Release")) {
        doFirst {
            check(hasReleaseSigning) {
                "Wear release signing is not configured. Supply the existing Kotlin keystore through " +
                    "ANDROID_KEYSTORE_PATH, ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS, and ANDROID_KEY_PASSWORD."
            }
        }
    }
}

android {
    namespace = "org.ferdidrgn.hudaquran.wear"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 30
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        applicationId = "org.ferdidrgn.hudaquran"
        versionCode = wearCiVersionCode
        versionName = "1.21.5"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.create("release") {
                    storeFile = file(releaseKeystorePath!!)
                    storePassword = releaseKeystorePassword
                    keyAlias = releaseKeyAlias
                    keyPassword = releaseKeyPassword
                }
            } else null
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Include native dependency symbols in the AAB for Play Console crash diagnostics.
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation("androidx.wear.compose:compose-material3:1.5.0")
    implementation("androidx.wear.compose:compose-foundation:1.5.0")
    implementation("com.google.android.gms:play-services-wearable:20.0.1")
    implementation("androidx.wear.tiles:tiles:1.6.2")
    implementation("androidx.wear.protolayout:protolayout:1.4.2")
    implementation("androidx.wear.protolayout:protolayout-material3:1.4.2")
}
>>>>>>> Stashed changes
