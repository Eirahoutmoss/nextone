plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Sürüm bilgisi CI'dan gelir (etiket → versionName, çalıştırma numarası → versionCode)
val nexVersionName = System.getenv("NEXTONE_VERSION_NAME") ?: "0.1.0-yerel"
val nexVersionCode = System.getenv("NEXTONE_VERSION_CODE")?.toIntOrNull() ?: 1
val keystoreFile: String? = System.getenv("NEXTONE_KEYSTORE_FILE")

android {
    namespace = "com.eirahoutmoss.nextone"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.eirahoutmoss.nextone"
        minSdk = 24
        targetSdk = 35
        versionCode = nexVersionCode
        versionName = nexVersionName
    }

    signingConfigs {
        create("release") {
            if (keystoreFile != null) {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("NEXTONE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("NEXTONE_KEY_ALIAS")
                keyPassword = System.getenv("NEXTONE_KEYSTORE_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Anahtar yoksa (yerel derleme) hata ayıklama anahtarıyla imzalanır; bu APK dağıtılmaz.
            signingConfig = if (keystoreFile != null) signingConfigs.getByName("release")
            else signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
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
    implementation(project(":core"))
    implementation(platform("androidx.compose:compose-bom:2025.05.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.core:core-ktx:1.16.0")
}
