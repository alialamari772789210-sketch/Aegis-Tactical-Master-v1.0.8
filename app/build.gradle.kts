plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.chaquo.python")
}

android {
    // [توحيد العتاد]: النواة المعتمدة لبناء مكتبات dsp المكتوبة بلغة C++
    ndkVersion = "25.2.9519653" 
    namespace = "com.jamesfirstok.aegis"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jamesfirstok.aegis"
        minSdk = 26
        targetSdk = 34
        versionCode = 200
        versionName = "2.0.0-Tactical-Operational"

        // حصر التجميع العتادي على معالجات ARM التكتيكية لضمان سرعة معالجة مصفوفات الأقمار
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true // تشفير وحظر الكود لمنع محاولات كشف خوارزميات التحييد
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    // ربط ملف البناء المركزي لـ CMake لتوليد مكتبة libaegis-core.so 
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

// [تصحيح صياغة الحقن لمحرك بايثون]: دمج الخوارزميات التطورية لـ AegisAutonomousCore.py
chaquopy {
    defaultConfig {
        buildPython = listOf("3.10") // التهيئة البرمجية لتحديد رقم بايثون المدعوم عتادياً
        pip {
            install("numpy")
            install("pyserial")
        }
    }
}

dependencies {
    // المكتبات الأساسية لواجهة الـ HUD الرادارية
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.6.5")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.5")
    
    // [تحصين الـ Room]: إضافة مكتبة ktx لتمكين التخزين اللحظي لإحداثيات الرادار والترددات دون حظر المعالج
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1") 
    kapt("androidx.room:room-compiler:2.6.1")
    
    // [تأمين الأمان السيادي]: التحول للإصدار القياسي المستقر لضمان ثبات حماية مستودع المفاتيح
    implementation("androidx.security:security-crypto:1.0.0")
    
    // محرك الذكاء الاصطناعي المستقل المتكامل مع الأنوية الـ 4 المتوازية 
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    
    // مكتبة الحسابات الطيفية السريعة والـ FFT لطبقة كوتلن الاحتياطية
    implementation("com.github.wendykierp:JTransforms:3.1")
}
