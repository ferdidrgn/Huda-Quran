plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "org.ferdidrgn.hudaquran.wear"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 30
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        applicationId = "org.ferdidrgn.hudaquran"
        versionCode = 25
        versionName = "1.21.4"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
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
