#include <stdio.h>
#include <stdlib.h>
#include <complex>
#include <android/log.h>

// تضمين مكتبة معالجة الإشارات اللاسلكية العسكرية liquid-dsp
#include <liquid/liquid.h>

#define LOG_TAG "AegisDemodulator"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
void demodulate_signal(
        std::complex<float>* input,
        float* output,
        int samples
) {
    if (samples <= 0 || input == nullptr || output == nullptr) return;

    // 1. إعدادات فك تضمين التردد اللاسلكي (FM Demodulation) وملاحقة الإرسال التماثلي
    float deviation = 0.5f; 

    freqdem dem = freqdem_create(deviation);

    // فك التضمين التكتيكي لكتلة العينات بالكامل دفعة واحدة لسرعة معالجة قصوى
    freqdem_demodulate_block(
        dem,
        reinterpret_cast<liquid_float_complex*>(input),
        samples,
        output
    );

    // 2. تصميم الفلتر الصوتي عالي الكفاءة لقمع موجات الضوضاء (Audio IIR Lowpass Filter)
    float fs = 24000.0f;     // معدل عينات النطاق الصوتي المستهدف
    float cutoff = 4000.0f;  // حاجز التردد العالي للتصفية (4 كيلوهرتز لعزل التشويش)
    unsigned int order = 7;  // رتبة المرشح الكاشف لضمان كبت حاد ونقي للإشارات الجانبية

    iirfilt_rrrf lowpass = iirfilt_rrrf_create_lowpass(
        order,
        cutoff / fs
    );

    // [تعديل تكتيكي حاسم]: معالجة كتل جماعية مدمجة للاستفادة التامة من التسريع العتادي لمعالجات ARM
    iirfilt_rrrf_execute_block(
        lowpass,
        output,
        samples,
        output
    );

    LOGI("[COMMS] Audio Signal Demodulated and Cleaned Successfully. Length: %d samples", samples);

    // 3. التطهير الفوري والآمن للموارد لمنع تفتت وانهيار ذاكرة الهاتف في الخلفية
    freqdem_destroy(dem);
    iirfilt_rrrf_destroy(lowpass);
}
