import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose") version libs.versions.androidx.compose.plugin.get()
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
        implementation(libs.koin.core)
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
