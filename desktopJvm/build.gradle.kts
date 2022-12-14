import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
}

group = "com.beforeyoudie"
version = "1.0"

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }

    withJava()
  }

  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(project(":common-ui"))
        implementation(project(":common-core"))

        implementation(compose.desktop.currentOs)
        implementation(libs.kermit)
        implementation(libs.decompose)
        implementation(libs.decompose.extensions.jetbrains)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.mokoresources.compose)
      }
    }
    val jvmTest by getting


    // TODO(#10) consider using gradle source sets and targets to separate out large fat configurable binary into slim binary that only ships with utilized set implementations.
    // Kinda pointless though.
  }
}

compose.desktop {
  application {
    mainClass = "MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "jvm"
      packageVersion = "1.0.0"
    }
  }
}
