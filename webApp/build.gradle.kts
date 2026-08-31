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
// segfaults (SIGSEGV, exit 139) on this app's real (non-toy, but not huge either — confirmed
// ~17.5 MB) module on GitHub's CI runners. Confirmed via CI logs across several isolated attempts,
// each verified to have actually taken effect: extra swap space, removing just --gufa, raising the
// process stack ulimit, and stripping every optimization pass down to just the mandatory
// --enable-* feature flags all made zero difference — the exact same crash every time, always
// against .gradle/binaryen/binaryen-version_125. So this pins an older release instead.
//
// Kotlin 2.2 moved Binaryen configuration from a shared root-project extension to a per-project
// one (see "Per-project Binaryen configuration" in the Kotlin 2.2.0 release notes) — confirmed by
// an earlier attempt that searched rootProject.extensions and found nothing binaryen-related at
// all (it logged the full extension list). This searches the current project's own extensions
// instead, still matched by name rather than a static type reference since the exact class isn't
// resolvable from this build script against this Kotlin Gradle plugin version.
val binaryenExtensionName = extensions.extensionsSchema.elements
    .map { it.name }
    .firstOrNull { it.contains("binaryen", ignoreCase = true) }
if (binaryenExtensionName != null) {
    val binaryenExtension = extensions.getByName(binaryenExtensionName)
    binaryenExtension.withGroovyBuilder { setProperty("version", "116") }
    logger.lifecycle("Pinned Binaryen version to 116 via extension '$binaryenExtensionName' (${binaryenExtension::class.java.name})")
} else {
    logger.lifecycle(
        "No binaryen-named extension found on :webApp; registered extensions: " +
            extensions.extensionsSchema.elements.joinToString { it.name },
    )
}

tasks.matching { it.name.contains("Wasm") && it.name.endsWith("Optimize") }.configureEach {
    // Filtering binaryenArgs here — at configuration time, not inside doFirst — because two
    // earlier attempts that mutated it inside doFirst (both replacing the property and mutating
    // the same list in place) still ran wasm-opt with --no-inline= present: "Unknown option
    // '--no-inline'" kept happening even though the doFirst logging proved the list was correctly
    // filtered by the time it printed. That means whatever builds the actual command line reads
    // binaryenArgs (or snapshots it) before task execution begins — the same timing as the version
    // pin above, which we know works because binaryen-version_116 really did show up in the
    // executed process path. So this runs at the same configuration-time point. Confirmed working
    // via CI logs: the before/after print showed --no-inline etc. correctly stripped down to just
    // the --enable-* flags, and wasm-opt no longer errored on them.
    //
    // (A doFirst block here that logged the input .wasm file size and re-printed binaryenArgs at
    // execution time was removed — it referenced fileTree/layout/logger inside the task's action,
    // which captures a Gradle script object reference the configuration cache can't serialize:
    // "cannot serialize object of type 'DefaultProject'... not supported with the configuration
    // cache". Configuration cache is enabled project-wide (org.gradle.configuration-cache=true in
    // gradle.properties), so any such capture fails the whole build even after every task runs
    // successfully. The diagnostic already did its job confirming the fix above; not needed anymore.)
    withGroovyBuilder {
        @Suppress("UNCHECKED_CAST")
        val args = getProperty("binaryenArgs") as MutableList<String>
        args.removeIf { !it.startsWith("--enable-") }
    }
}
