plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.chaquo.python")
}

android {
    ndkVersion = "25.2.9519653" // تنويه: تأكد من تحميل إصدار NDK هذا من الـ SDK Manager
    namespace = "com.jamesfirstok.aegis"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jamesfirstok.aegis"
        minSdk = 26
        targetSdk = 34
        versionCode = 200
        versionName = "2.0.0-Tactical-Operational"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

chaquopy {
    defaultConfig {
        version = "3.10"
        pip {
            install("numpy")
            install("pyserial")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.6.5")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.5")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // تم حذف سطر com.chaquo.python:runtime المتسبب بالخطأ من هنا بنجاح

    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("com.github.wendykierp:JTransforms:3.1")
}
