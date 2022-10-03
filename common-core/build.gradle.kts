import com.android.build.gradle.internal.res.processResources

val kotlin_version: String by extra
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.squareup.sqldelight")
    id("io.kotest.multiplatform")
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
                implementation(libs.koin.core)
                implementation(libs.kermit)
                implementation(libs.kermit.koin)
                implementation(libs.uuid)
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
                implementation(kotlin("test-common"))
                implementation(libs.kotest)
                implementation(libs.kotest.assertions)
                implementation(libs.kotest.properties)
                implementation(libs.kotest.koin)
                implementation(libs.koin.test)
                implementation(libs.koin.test.junit5)
                implementation(libs.mockk)
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
                implementation(libs.koin.test)
            }
        }
    }
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
    }
    compileOptions {
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