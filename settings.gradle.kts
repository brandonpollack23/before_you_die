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
include(":common-core")
include(":common-ui")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("androidx-compose-plugin", "1.2.0-beta02")
            version("ktlint", "11.0.0")

            library("kotlinx-coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

            // Koin
            val koinVersion = "3.2.2"
            val koinAndroidVersion = "3.2.2"
            val koinAndroidComposeVersion = "3.2.1"
            val koinKtorVersion = "3.2.2"
            library("koin-core", "io.insert-koin:koin-core:$koinVersion")
            library("koin-test", "io.insert-koin:koin-test:$koinVersion")
            library("koin-test-junit4", "io.insert-koin:koin-test-junit4:$koinVersion")
            library("koin-android", "io.insert-koin:koin-android:$koinAndroidVersion")
            library("koin-android-javacompat", "io.insert-koin:koin-android-compat:$koinAndroidVersion") // Java Compatibility
            library("koin-androidx-workmanager", "io.insert-koin:koin-androidx-workmanager:$koinAndroidVersion") // Jetpack WorkManager
            library("koin-androidx-navigation", "io.insert-koin:koin-androidx-navigation:$koinAndroidVersion") // Navigation Graph
            library("koin-androidx-compose", "io.insert-koin:koin-androidx-compose:$koinAndroidComposeVersion")
            library("koin-ktor", "io.insert-koin:koin-ktor:$koinKtorVersion")
            library("koin-ktor-logger-slf4j", "io.insert-koin:koin-logger-slf4j:$koinKtorVersion")

            library("desugar", "com.android.tools:desugar_jdk_libs:1.1.5")
        }
    }
}
