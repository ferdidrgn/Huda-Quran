import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

// Firebase plugins are applied only when the user has dropped their own
// google-services.json into androidApp/ — this keeps the build green out of
// the box before Firebase is configured. See README/Firebase setup notes.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
// Release signing, sourced from environment variables so the same keystore you already use in
// Android Studio's "Generate Signed App Bundle" wizard can also sign CI/CD builds — nothing here
// changes when those variables are unset. Release signing fails closed when the existing upload
// key is not supplied; CI gets it from repository secrets (see .github/workflows/ci.yml).
val releaseKeystorePath: String? = System.getenv("ANDROID_KEYSTORE_PATH")
val releaseKeystorePassword: String? = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val releaseKeyAlias: String? = System.getenv("ANDROID_KEY_ALIAS")
val releaseKeyPassword: String? = System.getenv("ANDROID_KEY_PASSWORD")
val ciRunNumber = providers.environmentVariable("GITHUB_RUN_NUMBER").orNull?.toLongOrNull()
if (ciRunNumber != null) {
    require(ciRunNumber in 1..524_999_999) { "GITHUB_RUN_NUMBER is outside Android's supported versionCode range." }
}
val ciVersionCodeBase = ciRunNumber?.times(4)
val phoneCiVersionCode = ciVersionCodeBase?.plus(1)?.toInt() ?: 30
val tvCiVersionCode = ciVersionCodeBase?.plus(2)?.toInt() ?: 32
val hasReleaseSigning = !releaseKeystorePath.isNullOrBlank() &&
    !releaseKeystorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

tasks.configureEach {
    if (name.startsWith("validateSigning") && name.endsWith("Release")) {
        doFirst {
            check(hasReleaseSigning) {
                "Release signing is not configured. Set ANDROID_KEYSTORE_PATH, ANDROID_KEYSTORE_PASSWORD, " +
                    "ANDROID_KEY_ALIAS, and ANDROID_KEY_PASSWORD. Release builds never use the debug key."
            }
        }
    }
}

android {
    namespace = "org.ferdidrgn.hudaquran"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.ferdidrgn.hudaquran"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = phoneCiVersionCode
        versionName = "1.21.5"
        // Public RevenueCat SDK key (goog_…), from the REVENUECAT_GOOGLE_KEY environment variable
        // (a CI secret) or `revenuecatGoogleKey` in ~/.gradle/gradle.properties. Blank = purchases off.
        val revenueCatKey = providers.environmentVariable("REVENUECAT_GOOGLE_KEY")
            .orElse(providers.gradleProperty("revenuecatGoogleKey"))
            .getOrElse("")
        buildConfigField("String", "REVENUECAT_GOOGLE_KEY", "\"$revenueCatKey\"")
    }
    flavorDimensions += "device"
    productFlavors {
        create("phone") {
            dimension = "device"
            buildConfigField("boolean", "IS_TV_BUILD", "false")
            versionCode = phoneCiVersionCode
        }
        create("tv") {
            dimension = "device"
            buildConfigField("boolean", "IS_TV_BUILD", "true")
            versionCode = tvCiVersionCode
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // The app's own UI text is fully custom-localized (LocalStrings), not Android string
    // resources, so there's no reason to ship the dozens of `values-xx` locale folders that
    // AndroidX/Play Services/Firebase bundle for their own internal strings — this drops them
    // from the release build except for the languages the app actually offers.
    androidResources {
        localeFilters += setOf("tr", "en", "ar", "de", "fr", "uz", "ky", "tk")
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
        debug {
            // RevenueCat Test Store key — debug builds ONLY (never shipped; release reads the real
            // goog_… key from the environment / gradle property above).
            buildConfigField("String", "REVENUECAT_GOOGLE_KEY", "\"test_FZbXAoHhWzdZpQKvlnraKRilWDb\"")
        }
        release {
            // A release without the existing Play upload key must fail, never produce a
            // misleading debug-signed AAB that can be mistaken for a Play-ready artifact.
            signingConfig = if (hasReleaseSigning) signingConfigs.getByName("release") else null
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    add("phoneImplementation", project(":shared"))
    add("tvImplementation", project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.datetime)
    implementation(libs.android.appUpdate)
    // Forces a modern Fragment resolution so lint's InvalidFragmentVersionForActivityResult
    // check passes for registerForActivityResult() call sites — whatever transitively pulled
    // in Fragment before resolved to something below the 1.3.0 minimum that check requires.
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
    testImplementation(libs.junit)
}

configurations.configureEach {
    if (name.contains("tv", ignoreCase = true)) {
        exclude(group = "com.google.android.gms", module = "play-services-ads")
        exclude(group = "com.google.android.gms", module = "play-services-wearable")
        exclude(group = "com.google.firebase")
        exclude(group = "com.revenuecat.purchases")
    }
}
