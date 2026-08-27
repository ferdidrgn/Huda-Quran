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
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    // play-services-ads (pulled in via :shared) declares an old androidx.fragment floor
    // (1.1.0) in its own POM, which Play Console's SDK Index flags as outdated. A `constraint`
    // only nudges Gradle's normal version-conflict resolution toward this version when fragment
    // is already present in the graph via that transitive path — unlike a plain `implementation`
    // dependency, it never pulls in fragment's own transitive deps (activity/lifecycle), so it
    // can't cause the classpath skew a direct dependency risked last time.
    constraints {
        implementation("androidx.fragment:fragment:1.8.5") {
            because("Play Console SDK Index flags play-services-ads' transitive fragment:1.1.0 as outdated")
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
        versionCode = 8
        versionName = "1.2.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            // Debug-signed so `assembleRelease` produces something `adb install` can actually
            // install locally for crash testing (R8/minification behavior included) without
            // needing the real upload keystore. Play Console re-signs uploaded bundles with the
            // real app-signing key regardless, so this has no effect on what you actually publish.
            signingConfig = signingConfigs.getByName("debug")
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
    }
}