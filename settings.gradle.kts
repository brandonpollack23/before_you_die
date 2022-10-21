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
include(":desktopJvm")
include(":common-core")
include(":common-ui")

/* ktlint-disable max-line-length */
dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      val kotlinVersion = "1.7.20"
      version("kotlin-compiler", kotlinVersion)
      val kspVersion = "1.0.6"
      version("kotlin-ksp", "$kotlinVersion-$kspVersion")
      // WARNING JS won't work until in kotlin 1.7.20 until compose 1.21 according to release notes
      version("jetbrains-compose", "1.2.0")
      version("spotless", "6.11.0")
      version("gradleversions", "0.42.0")

      // kotlin-inject DI
      library("di-kotlinInject-ksp", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.5.1")
      library("di-kotlinInject-runtime", "me.tatarka.inject:kotlin-inject-runtime:0.5.1")

      // Coroutines/Concurrency
      val coroutinesVersion = "1.6.4"
      library("kotlinx-coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
      library("kotlinx-coroutines-android", "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
      library("kotlinx-coroutines-swing", "org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutinesVersion")
      library("kotlinx-coroutines-test", "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

      // Logging
      val kermitVersion = "1.1.3"
      library("kermit", "co.touchlab:kermit:$kermitVersion")
      library("kermit-test", "co.touchlab:kermit-test:$kermitVersion")

      // Storage/SQLite
      val sqlDelightVersion = "1.5.4"
      version("sqldelight", sqlDelightVersion)
      library("sqldelight-android", "com.squareup.sqldelight:android-driver:$sqlDelightVersion")
      library("sqldelight-native", "com.squareup.sqldelight:native-driver:$sqlDelightVersion")
      library("sqldelight-jvm", "com.squareup.sqldelight:sqlite-driver:$sqlDelightVersion")
      library("requiry-sqliteandroid", "com.github.requery:sqlite-android:3.39.2")

      // Kotest framework
      val kotestVersion = "5.5.1"
      version("kotest", kotestVersion)
      library("kotest", "io.kotest:kotest-framework-engine:$kotestVersion")
      library("kotest-runner-junit5", "io.kotest:kotest-runner-junit5:$kotestVersion")
      library("kotest-assertions", "io.kotest:kotest-assertions-core:$kotestVersion")
      library("kotest-properties", "io.kotest:kotest-property:$kotestVersion")
      library("kotest-koin", "io.kotest.extensions:kotest-extensions-koin:1.1.0")

      library("mockk", "io.mockk:mockk:1.13.2")

      val decomposeVersion = "1.0.0-alpha-06"
      library("decompose", "com.arkivanov.decompose:decompose:$decomposeVersion")
      library("decompose-extensions-jetbrains", "com.arkivanov.decompose:extensions-compose-jetbrains:$decomposeVersion")
      library("decompose-extensions-jetpack", "com.arkivanov.decompose:extensions-compose-jetpack:$decomposeVersion")

      // UUID
      library("uuid", "com.benasher44:uuid:0.5.0")

      library("desugar", "com.android.tools:desugar_jdk_libs:1.1.5")

      // Resources
      val mokoResourcesVersion = "0.20.1"
      version("mokoresources", mokoResourcesVersion)
      library("mokoresources", "dev.icerock.moko:resources:$mokoResourcesVersion")
      library("mokoresources-compose", "dev.icerock.moko:resources-compose:$mokoResourcesVersion")
      library("mokoresources-test", "dev.icerock.moko:resources-test:$mokoResourcesVersion")
    }
  }
}
