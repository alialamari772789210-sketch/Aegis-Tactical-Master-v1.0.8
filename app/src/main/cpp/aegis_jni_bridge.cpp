#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <complex.h>
#include <liquid/liquid.h>

// الإعلان عن الدالة التي كتبناها أعلاه
extern void tactical_radar_core(float complex * rx_buffer, int samples);

/**
 * جسر JNI: استدعاء Kotlin لـ processSignal(double[])
 * 1. يستقبل مصفوفة double من جافا
 * 2. يحولها إلى float complex
 * 3. يستدعي نواة الرادار
 * 4. يحول النتائج (الطيف) إلى double[] ويعيدها
 */
JNIEXPORT jdoubleArray JNICALL
Java_com_jamesfirstok_aegis_radar_TacticalRadar_processSignal(
    JNIEnv *env, jobject thiz, jdoubleArray raw_signal) {

    // 1. سحب البيانات من جافا
    jsize len = (*env)->GetArrayLength(env, raw_signal);
    jdouble *body = (*env)->GetDoubleArrayElements(env, raw_signal, 0);

    // 2. تحويل إلى float complex (يستخدمها liquid-dsp)
    float complex *rx_buffer = (float complex*) malloc(len * sizeof(float complex));
    for (int i = 0; i < len; i++) {
        // الإشارة الخام هي الجزء الحقيقي، الجزء التخيلي صفر
        rx_buffer[i] = (float)body[i] + 0.0f * I;
    }

    // 3. استدعاء النواة التكتيكية
    tactical_radar_core(rx_buffer, len);

    // 4. إعادة النتيجة (قدرة الإشارة بعد FFT) إلى جافا
    jdoubleArray result = (*env)->NewDoubleArray(env, len);
    jdouble *fill = (jdouble*) malloc(len * sizeof(jdouble));
    for (int i = 0; i < len; i++) {
        fill[i] = (jdouble)cabsf(rx_buffer[i]); // طيف القدرة
    }
    (*env)->SetDoubleArrayRegion(env, result, 0, len, fill);

    // 5. تنظيف
    free(fill);
    free(rx_buffer);
    (*env)->ReleaseDoubleArrayElements(env, raw_signal, body, 0);

    return result;
}
