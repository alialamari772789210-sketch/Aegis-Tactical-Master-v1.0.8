#include <jni.h>
#include <vector>
#include <cmath>
#include <complex>
#include <android/log.h>

#define LOG_TAG "AegisTacticalCore"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// تنفيذ خوارزمية FFT حقيقية لتحليل الإشارات الرادارية
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

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_jamesfirstok_aegis_radar_TacticalRadar_processSignal(JNIEnv *env, jobject thiz, jdoubleArray raw_signal) {
    // تحويل البيانات القادمة من Java إلى C++
    jsize len = env->GetArrayLength(raw_signal);
    jdouble *body = env->GetDoubleArrayElements(raw_signal, 0);

    std::vector<std::complex<double>> signal_data(len);
    for (int i = 0; i < len; i++) {
        signal_data[i] = std::complex<double>(body[i], 0);
    }

    // تشغيل معالجة الرادار الحقيقية
    fast_fourier_transform(signal_data);

    // استخراج الطاقة (Magnitude) لتحديد وجود أجسام أو تشويش
    jdoubleArray result = env->NewDoubleArray(len);
    jdouble fill[len];
    for (int i = 0; i < len; i++) {
        fill[i] = std::abs(signal_data[i]);
    }

    env->SetDoubleArrayRegion(result, 0, len, fill);
    env->ReleaseDoubleArrayElements(raw_signal, body, 0);

    LOGI("Aegis Radar: Signal Processed with Real-time FFT.");
    return result;
}
