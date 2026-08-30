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
//
// Matching on the runtime class's simple name ("BinaryenExec") turned out to never actually hit
// this build's real optimize task — no trace of it in CI logs, so --gufa was never actually being
// removed by that version of this workaround. The real task name is confirmed directly from CI
// logs (compileProductionExecutableKotlinWasmJsOptimize), so target it by name instead, reached
// through Gradle's Groovy property access rather than a static type reference.
tasks.matching { it.name.contains("Wasm") && it.name.endsWith("Optimize") }.configureEach {
    doFirst {
        withGroovyBuilder {
            @Suppress("UNCHECKED_CAST")
            val args = getProperty("binaryenArgs") as MutableList<String>
            val filtered = args.filterNot { it == "--gufa" }.toMutableList()
            logger.lifecycle("[$name] binaryenArgs before: $args")
            logger.lifecycle("[$name] binaryenArgs after:  $filtered")
            setProperty("binaryenArgs", filtered)
        }
    }
}
