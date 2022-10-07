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
    // See the sourceSet below (jvmDecomposeMain) for explanation
    compilations {
      val main by getting

      val jvmDecomposeMain by compilations.creating {
        defaultSourceSet {
          dependencies {
            implementation(main.compileDependencyFiles + main.output.classesDirs)
          }
        }
      }
      // Create a test task to run the tests produced by this compilation:
      tasks.register<Test>("jvmDecomposeMain") {
        // TODO(#10) this is all a wip, register a run task for this.
      }
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


    // TODO(#10)
    // This source set can be used to implement something that depends directly on Decompose using actual/expect.
    // That way I don't need to directly rely on "children" in my main.
    // Source Set info: https://kotlinlang.org/docs/multiplatform-share-on-platforms.html#use-target-shortcuts
    // Build Target info: https://kotlinlang.org/docs/multiplatform-configure-compilations.html#create-a-custom-compilation
    val jvmDecomposeMain by sourceSets.creating {
      dependsOn(jvmMain)
    }
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
