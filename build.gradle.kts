import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
  id("com.diffplug.spotless") version libs.versions.spotless
  id("com.google.devtools.ksp") version libs.versions.kotlin.ksp apply false
  id("com.squareup.sqldelight") version libs.versions.sqldelight apply false
  id("io.kotest.multiplatform") version libs.versions.kotest
  id("org.jetbrains.compose") version libs.versions.jetbrains.compose apply false
  id("com.github.ben-manes.versions") version libs.versions.gradleversions
  id("org.jetbrains.dokka") version "1.7.20"
  id("dev.icerock.mobile.multiplatform-resources") version libs.versions.mokoresources apply false
}

buildscript {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
  dependencies {
    // Kotlin/Android
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.compiler}")
    classpath("com.android.tools.build:gradle:7.2.2")

    // Moko Resources
    classpath("dev.icerock.moko:resources-generator:${libs.versions.mokoresources}")
  }
}

group = "com.beforeyoudie"
version = "1.0"

allprojects {
  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io") // For requery sqlite
  }
}

subprojects {
  apply(plugin = "com.diffplug.spotless") // Version should be inherited from parent
  apply(plugin = "org.jetbrains.dokka") // Version should be inherited from parent

  // Configure kotest
  tasks.withType<Test> {
    useJUnitPlatform()

    filter {
      isFailOnNoMatchingTests = true
    }
    testLogging {
      showExceptions = true
      showStandardStreams = true
      events = setOf(
        org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
        org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
      )
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
  }

  tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets {
      val moduleFileName = "Module.md"

      configureEach {
        if (file(moduleFileName).exists()) includes.from(moduleFileName)
      }
    }
  }

  spotless {
    java {
      googleJavaFormat("1.8").reflowLongStrings()
    }
    kotlin {
      // version, setUseExperimental, userData and editorConfigOverride are all optional
      target("**/*.kt")
      targetExclude("**/generated/**/*.*")
      trimTrailingWhitespace()
      endWithNewline()

      ktlint("0.47.1")
        // .setUseExperimental(true)
        .editorConfigOverride(mapOf("indent_size" to 2, "max_line_length" to 100))
    }
  }
}

tasks.dokkaHtmlMultiModule.configure {
}