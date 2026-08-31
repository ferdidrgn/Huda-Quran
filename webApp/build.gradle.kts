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
    doFirst {
        // Ground truth on the module this crashes on, since nothing about tuning wasm-opt's pass
        // flags has changed the outcome so far — worth knowing the actual size involved.
        val wasmFiles = fileTree(layout.buildDirectory.get().asFile).matching { include("**/*.wasm") }.files
        wasmFiles.forEach {
            logger.lifecycle("[$name] input wasm file: ${it.path} (${it.length() / 1024} KB)")
        }

        withGroovyBuilder {
            @Suppress("UNCHECKED_CAST")
            val args = getProperty("binaryenArgs") as MutableList<String>
            // Binaryen 116 (pinned above) doesn't understand --no-inline=..., a flag Kotlin's
            // default arg list assumes is available — confirmed by CI failing cleanly with
            // "Unknown option '--no-inline'" (exit 1, not a crash) once the version pin actually
            // took effect. So --enable-* only; the version pin is the fix, this is just keeping
            // wasm-opt's own CLI happy about it.
            val filtered = args.filter { it.startsWith("--enable-") }.toMutableList()
            logger.lifecycle("[$name] binaryenArgs before: $args")
            logger.lifecycle("[$name] binaryenArgs after:  $filtered")
            setProperty("binaryenArgs", filtered)
        }
    }
}
