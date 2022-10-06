import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
  kotlin("multiplatform")
  id("com.android.library")
  id("com.squareup.sqldelight")
  id("io.kotest.multiplatform")
  id("com.google.devtools.ksp")
}

group = "com.beforeyoudie"
version = "1.0"

kotlin {
  android()
  jvm("desktop") {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.decompose)
        implementation(libs.kermit)
        implementation(libs.kermit.koin)
        implementation(libs.koin.core)
        implementation(libs.kotlinx.coroutines)
        implementation(libs.uuid)
        implementation(libs.di.kotlinInject.runtime)
      }
    }

    val androidMain by getting {
      dependencies {
        implementation(libs.sqldelight.android)
        implementation(libs.requiry.sqliteandroid)
      }
    }

    val desktopMain by getting {
      dependencies {
        implementation(libs.sqldelight.jvm)
      }
    }

    val commonTest by getting {
      dependencies {
      }
    }

    val androidTest by getting {
      dependencies {
        implementation(libs.kotest.runner.junit5)
        implementation(libs.koin.core)
        implementation(libs.koin.test)
      }
    }

    val desktopTest by getting {
      dependencies {
        implementation(libs.kotest.runner.junit5)
        implementation(libs.kotest)
        implementation(libs.kotest.assertions)
        implementation(libs.kotest.properties)
        implementation(libs.kotest.koin)
        implementation(libs.koin.test)
        implementation(libs.mockk)
      }
    }
  }
}

// Compiler plugin dependencies go at the project level
dependencies {
  ksp(libs.di.kotlinInject.ksp)
}

tasks.named<Copy>("desktopTestProcessResources") {
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

android {
  compileSdk = 33
  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  defaultConfig {
    minSdk = 24
    targetSdk = 31
    multiDexEnabled = true
  }
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
  dependencies {
    coreLibraryDesugaring(libs.desugar)
  }
}

sqldelight {
  database("BeforeYouDieDb") {
    packageName = "com.beforeyoudie.common.storage"
    sourceFolders = listOf("sql")
  }
}

tasks.withType<DokkaTaskPartial>().configureEach {
  moduleName.set("Common Core")
}
