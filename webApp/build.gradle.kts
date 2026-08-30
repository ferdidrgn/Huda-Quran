import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))

            implementation(libs.compose.ui)
        }
    }
}

// The production wasmJs build runs Binaryen's wasm-opt to optimize the compiled module, and its
// default pass list includes --gufa (a heavy whole-program flow analysis) run multiple times.
// That combination segfaults (SIGSEGV, exit 139) on this app's real (non-toy) module size on
// GitHub's CI runners — a documented class of wasm-opt crash, not something extra swap space
// fixed. Dropping just --gufa keeps every other optimization pass and all the required
// --enable-* feature flags the Kotlin/Wasm runtime needs; it trades a little extra optimization
// for a build that actually completes.
tasks.withType<org.jetbrains.kotlin.gradle.targets.js.binaryen.BinaryenExec>().configureEach {
    doFirst {
        binaryenArgs = binaryenArgs.filterNot { it == "--gufa" }.toMutableList()
    }
}
