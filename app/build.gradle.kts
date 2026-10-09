import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.lywsd02bledashboard"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.lywsd02bledashboard"
        minSdk = 24
        targetSdk = 36
        versionCode = 9
        versionName = "1.2.5"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}

// 로컬 환경설정(local.properties) 또는 환경 변수에 배포 경로가 지정된 경우 APK 자동 복사
val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    FileInputStream(localPropsFile).use { stream ->
        localProps.load(stream)
    }
}
val deployDir = localProps.getProperty("apk.deploy.dir") ?: System.getenv("APK_DEPLOY_DIR")

if (!deployDir.isNullOrBlank()) {
    val currentVersion = android.defaultConfig.versionName ?: "1.2.5"

    val copyDebugApkToDeployDir = tasks.register<Copy>("copyDebugApkToDeployDir") {
        val apkFolder = layout.buildDirectory.dir("outputs/apk/debug")
        from(apkFolder) {
            include("app-debug.apk")
            rename("app-debug.apk", "lywsd02-ble-android-app-v${currentVersion}.apk")
        }
        from(apkFolder) {
            include("app-debug.apk")
            rename("app-debug.apk", "lywsd02-ble-android-app.apk")
        }
        from(apkFolder) {
            include("app-debug.apk")
            rename("app-debug.apk", "LYWSD02_BLE_Android_App_v${currentVersion}.apk")
        }
        from(apkFolder) {
            include("app-debug.apk")
            rename("app-debug.apk", "LYWSD02_BLE_Android_App.apk")
        }
        from(apkFolder) {
            include("app-debug.apk")
            rename("app-debug.apk", "LYWSD02_BLE_Tool_v${currentVersion}.apk")
        }
        from(apkFolder) {
            include("app-debug.apk")
            rename("app-debug.apk", "LYWSD02_BLE_Tool.apk")
        }
        from(apkFolder) {
            include("app-debug.apk")
        }
        into(file(deployDir))
    }

    val copyReleaseApkToDeployDir = tasks.register<Copy>("copyReleaseApkToDeployDir") {
        val apkFolder = layout.buildDirectory.dir("outputs/apk/release")
        from(apkFolder) {
            include("app-release.apk")
            rename("app-release.apk", "lywsd02-ble-android-app.apk")
        }
        from(apkFolder) {
            include("app-release.apk")
            rename("app-release.apk", "lywsd02-ble-android-app-v${currentVersion}.apk")
        }
        from(apkFolder) {
            include("app-release.apk")
            rename("app-release.apk", "LYWSD02_BLE_Android_App.apk")
        }
        from(apkFolder) {
            include("app-release.apk")
            rename("app-release.apk", "LYWSD02_BLE_Android_App_v${currentVersion}.apk")
        }
        from(apkFolder) {
            include("app-release.apk")
            rename("app-release.apk", "LYWSD02_BLE_Tool.apk")
        }
        from(apkFolder) {
            include("app-release.apk")
            rename("app-release.apk", "LYWSD02_BLE_Tool_v${currentVersion}.apk")
        }
        from(apkFolder) {
            include("app-release.apk")
        }
        into(file(deployDir))
    }

    afterEvaluate {
        tasks.named("assembleDebug").configure {
            finalizedBy(copyDebugApkToDeployDir)
        }
        tasks.named("assembleRelease").configure {
            finalizedBy(copyReleaseApkToDeployDir)
        }
    }
}

