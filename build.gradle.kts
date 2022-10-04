plugins {
  id("org.jetbrains.dokka") version "1.7.10"
  id("com.squareup.sqldelight") version libs.versions.sqldelight apply false
  id("io.kotest.multiplatform") version libs.versions.kotest
  id("com.diffplug.spotless") version libs.versions.spotless
}

buildscript {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    classpath("com.android.tools.build:gradle:7.2.2")
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

// TODO Dokka
// * Module.md
// * Run on build
// * Run on all subprojects
tasks.dokkaHtmlMultiModule.configure {
  outputDirectory.set(buildDir.resolve("dokkaCustomMultiModuleOutput"))
}
