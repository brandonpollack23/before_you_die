pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
rootProject.name = "before_you_die"

include(":android")
include(":desktop")
include(":common-ui")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("androidx-compose-plugin", "1.2.0-beta02")
            version("ktlint", "11.0.0")
            library("kotlinx-coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            library("desugar", "com.android.tools:desugar_jdk_libs:1.1.5")
        }
    }
}
