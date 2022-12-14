import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
  kotlin("multiplatform")
  id("com.android.library")
  id("com.squareup.sqldelight")
  id("io.kotest.multiplatform")
  id("com.google.devtools.ksp")
  id("kotlin-parcelize")
  id("dev.icerock.mobile.multiplatform-resources")
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
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.uuid)
        implementation(libs.di.kotlinInject.runtime)
        implementation(libs.mokoresources)
      }
    }

    val androidMain by getting {
      dependencies {
        implementation(libs.sqldelight.android)
        implementation(libs.requiry.sqliteandroid)
        implementation(libs.kotlinx.coroutines.android)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(libs.sqldelight.jvm)
        implementation(libs.kotlinx.coroutines.swing) // Compose uses swing, this gives us access to the dispatcher
      }
      kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
    }

    val commonTest by getting {
      dependencies {
        implementation(libs.mockk)
        implementation(libs.kotest)
        implementation(libs.kotest.assertions)
        implementation(libs.kotest.properties)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.kermit.test)
      }
    }

    val androidTest by getting {
      dependencies {
        implementation(libs.kotest.runner.junit5)
        implementation(libs.sqldelight.jvm) // USE JDBC DRIVER IN UNIT TESTS
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation(libs.kotest.runner.junit5)
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

  dependencies {
    coreLibraryDesugaring(libs.desugar)
  }

  libraryVariants.all {
    kotlin {
      sourceSets {
        getByName("android${name.capitalized()}") {
          kotlin.srcDir("build/generated/ksp/android/$name/kotlin")
        }
      }
    }
  }
}

sqldelight {
  database("BeforeYouDieDb") {
    packageName = "com.beforeyoudie.common.storage.impl"
    sourceFolders = listOf("sql")
    dialect = "sqlite:3.24"
    verifyMigrations = true
  }
}

tasks.withType<DokkaTaskPartial>().configureEach {
  moduleName.set("Common Core")
}

// TODO(#16) make sure to setup iOS integration for resources
multiplatformResources {
  multiplatformResourcesPackage = "com.beforeyoudie.common.resources"
}