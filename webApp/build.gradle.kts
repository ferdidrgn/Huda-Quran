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
// effect: extra swap space, removing just --gufa, raising the process stack ulimit, and even
// stripping every optimization pass down to just the mandatory --enable-* feature flags all made
// zero difference — the exact same crash every time. The task still runs for ~2 minutes before
// crashing even with virtually no passes left to run, which points away from any specific pass
// and toward wasm-opt 125 itself choking on parsing/writing a module this large — so this pins an
// older, longer-established Binaryen release instead of continuing to tune pass flags. Reached
// dynamically (via the root project's registered extensions, matched by class simple name) rather
// than a static type reference, since that type's exact package isn't resolvable from this build
// script against this Kotlin Gradle plugin version (confirmed the hard way earlier).
val binaryenExtensionName = rootProject.extensions.extensionsSchema.elements
    .map { it.name }
    .firstOrNull { it.contains("binaryen", ignoreCase = true) }
if (binaryenExtensionName != null) {
    val binaryenRootExtension = rootProject.extensions.getByName(binaryenExtensionName)
    binaryenRootExtension.withGroovyBuilder { setProperty("version", "116") }
    logger.lifecycle("Pinned Binaryen version to 116 via extension '$binaryenExtensionName' (${binaryenRootExtension::class.java.name})")
} else {
    logger.lifecycle(
        "No binaryen-named extension found; registered rootProject extensions: " +
            rootProject.extensions.extensionsSchema.elements.joinToString { it.name },
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
            val filtered = args.filter { it.startsWith("--enable-") || it.startsWith("--no-inline=") }.toMutableList()
            logger.lifecycle("[$name] binaryenArgs before: $args")
            logger.lifecycle("[$name] binaryenArgs after:  $filtered")
            setProperty("binaryenArgs", filtered)
        }
    }
}
