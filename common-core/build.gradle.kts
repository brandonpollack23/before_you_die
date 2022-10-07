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
  jvm {
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
        implementation(libs.koin.android)
        implementation(libs.sqldelight.android)
        implementation(libs.requiry.sqliteandroid)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(libs.sqldelight.jvm)
      }
      kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
    }

    val commonTest by getting {
      dependencies {
        implementation(libs.mockk)
        implementation(libs.koin.test)
        implementation(libs.kotest.koin)
      }
    }

    val androidTest by getting {
      dependencies {
        implementation(libs.kotest.runner.junit5)
        implementation(libs.koin.core)
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation(libs.kotest.runner.junit5)
        implementation(libs.kotest)
        implementation(libs.kotest.assertions)
        implementation(libs.kotest.properties)
        implementation(libs.koin.test)
      }
      kotlin.srcDir("build/generated/ksp/jvm/jvmTest/kotlin")
    }
  }
}

// Compiler plugin dependencies go at the project level
dependencies {
  add("kspJvm", libs.di.kotlinInject.ksp)
  add("kspJvmTest", libs.di.kotlinInject.ksp)
  add("kspAndroid", libs.di.kotlinInject.ksp)
  add("kspAndroidTest", libs.di.kotlinInject.ksp)
}

ksp {
  arg("me.tatarka.inject.dumpGraph", "true")
}

tasks.named<Copy>("jvmTestProcessResources") {
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
  sourceSets {
    named("release") {
      kotlin.srcDir("build/generated/ksp/android/androidRelease/kotlin")
    }
    named("debug") {
      kotlin.srcDir("build/generated/ksp/android/androidDebug/kotlin")
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