#include <jni.h>
#include <vector>
#include <cmath>
#include <complex>
#include <android/log.h>

#define LOG_TAG "AegisTacticalCore"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

// 1. خوارزمية FFT حقيقية لتحليل الإشارات الرادارية في الوقت الفعلي
void fast_fourier_transform(std::vector<std::complex<double>>& data) {
    size_t n = data.size();
    if (n <= 1) return;

    std::vector<std::complex<double>> even(n / 2), odd(n / 2);
    for (size_t i = 0; i < n / 2; ++i) {
        even[i] = data[i * 2];
        odd[i] = data[i * 2 + 1];
    }

    fast_fourier_transform(even);
    fast_fourier_transform(odd);

    for (size_t k = 0; k < n / 2; ++k) {
        std::complex<double> t = std::polar(1.0, -2 * M_PI * k / n) * odd[k];
        data[k] = even[k] + t;
        data[k + n / 2] = even[k] - t;
    }
}

// 2. دالة كشف بصمة المسيرة بناءً على تحليل قمم الطاقة (Peak Detection)
bool detect_drone_signature(const double* magnitude_data, int len) {
    double threshold = 0.85; // عتبة الطاقة التكتيكية (Threshold)
    int signal_count = 0;
    
    for (int i = 0; i < len; i++) {
        if (magnitude_data[i] > threshold) {
            signal_count++;
        }
    }
    
    // إذا تجاوزت النقاط المكتشفة 5% من عرض النطاق، يعتبر هدفاً محققاً
    return signal_count > (len * 0.05);
}

// 3. دالة الجسر (JNI) الرئيسية
extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_jamesfirstok_aegis_radar_TacticalRadar_processSignal(JNIEnv *env, jobject thiz, jdoubleArray raw_signal) {
    
    // سحب البيانات الخام من طبقة جافا
    jsize len = env->GetArrayLength(raw_signal);
    jdouble *body = env->GetDoubleArrayElements(raw_signal, 0);

    // تحويل الإشارة إلى أعداد مركبة (Complex Numbers) للمعالجة
    std::vector<std::complex<double>> signal_data(len);
    for (int i = 0; i < len; i++) {
        signal_data[i] = std::complex<double>(body[i], 0);
    }

    // التنفيذ: تحويل فورييه السريع (FFT)
    fast_fourier_transform(signal_data);

    // حساب حجم الطاقة (Magnitude) وتخزينها
    jdoubleArray result = env->NewDoubleArray(len);
    double fill[len];
    for (int i = 0; i < len; i++) {
        fill[i] = std::abs(signal_data[i]);
    }

    // تفعيل كاشف الأهداف المهددة تلقائياً
    if (detect_drone_signature(fill, len)) {
        LOGW("⚠️ [ALERT] Aegis Radar: DRONE SIGNATURE DETECTED!");
        // هنا يمكن إرسال إشارة مقاطعة (Interrupt) فورية لنظام التحييد
    } else {
        LOGI("Aegis Radar: Spectrum Clear.");
    }

    // إعادة النتائج لطبقة التطبيق لعرضها على شاشة الشلال (Waterfall)
    env->SetDoubleArrayRegion(result, 0, len, fill);
    env->ReleaseDoubleArrayElements(raw_signal, body, 0);

    return result;
}
