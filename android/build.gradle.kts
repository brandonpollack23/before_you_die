plugins {
  id("org.jetbrains.compose")
  id("com.android.application")
  kotlin("android")
}

group = "com.beforeyoudie"
version = "1.0"

android {
  compileSdk = 33

  defaultConfig {
    applicationId = "com.beforeyoudie.beforeyoudie"
    minSdk = 24
    targetSdk = 31
    versionCode = 1
    versionName = "1.0"
  }

  signingConfigs {
    create("release") {
      storeFile = file("./key/key.jks")
      com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir).apply {
        storePassword = getProperty("storePwd")
        keyAlias = getProperty("keyAlias")
        keyPassword = getProperty("keyPwd")
      }
    }
  }

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  buildTypes {
    create("debugPG") {
      isDebuggable = false
      isMinifyEnabled = true
      versionNameSuffix = " debugPG"
      matchingFallbacks.add("debug")

      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        file("proguard-rules.pro")
      )
    }
    getByName("release") {
      isMinifyEnabled = true
      signingConfig = signingConfigs.getByName("release")

      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        file("proguard-rules.pro")
      )
    }
  }

  buildFeatures {
    compose = true
  }

  dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(compose.material)
    implementation(libs.kermit)
    implementation(libs.decompose.extensions.jetpack)
    implementation(libs.kotlinx.coroutines.android)
    implementation(project(":common-core"))
    implementation(project(":common-ui"))
    implementation("androidx.activity:activity-compose:1.6.0")
  }
}
