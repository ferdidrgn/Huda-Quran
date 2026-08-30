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

// The production wasmJs build runs Binaryen's wasm-opt to optimize the compiled module, and it
// segfaults (SIGSEGV, exit 139) on this app's real (non-toy) module size on GitHub's CI runners.
// Confirmed via CI logs across several isolated attempts, each verified to have actually taken
// effect: extra swap space, removing just --gufa, and raising the process stack ulimit each made
// zero difference — the exact same crash every time, including a run where the before/after log
// below proved --gufa really was gone from the args. That rules out memory pressure, that one
// pass, and stack depth as the cause, which points at wasm-opt's heavier IR-analysis passes in
// general (-O3/-Oz, --closed-world, --type-ssa, --type-merging, --gufa) rather than any single
// one of them. So this drops every optimization pass and keeps only the --enable-* feature flags
// (required for the module to validate/run) and the --no-inline= entries (protect specific
// exception-handling intrinsics from being inlined away, unrelated to the crash-prone passes) —
// trading real optimization for a build that completes.
tasks.matching { it.name.contains("Wasm") && it.name.endsWith("Optimize") }.configureEach {
    doFirst {
        withGroovyBuilder {
            @Suppress("UNCHECKED_CAST")
            val args = getProperty("binaryenArgs") as MutableList<String>
            val filtered = args.filter { it.startsWith("--enable-") || it.startsWith("--no-inline=") }.toMutableList()
            logger.lifecycle("[$name] binaryenArgs before: $args")
            logger.lifecycle("[$name] binaryenArgs after:  $filtered")
            setProperty("binaryenArgs", filtered)
        }
    }
}
