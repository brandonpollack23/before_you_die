plugins {
    id("org.jetbrains.dokka") version "1.7.10"
    id("org.jlleitschuh.gradle.ktlint") version libs.versions.ktlint
    id("com.squareup.sqldelight") version libs.versions.sqldelight apply false
    id("io.kotest.multiplatform") version libs.versions.kotest
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
    apply(plugin = "org.jlleitschuh.gradle.ktlint") // Version should be inherited from parent

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

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            // exclude { element -> element.file.path.contains("$buildDir/generated/") }
            exclude("**/build/**")
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
