#include <liquid/liquid.h>
#include <complex.h>
#include <stdio.h>

/*
 * FM Demodulator + Audio Lowpass Filter
 * Sovereign Signal Processing Layer
 */

extern "C"
void demodulate_signal(
        float complex* input,
        float* output,
        int samples
) {

    // إنشاء مفكك FM
    float deviation = 0.5f;

    freqdem dem =
        freqdem_create(deviation);

    // فك التضمين
    freqdem_demodulate_block(
        dem,
        input,
        samples,
        output
    );

    // مرشح تمرير منخفض للصوت
    float fs = 24000.0f;

    float cutoff = 4000.0f;

    unsigned int order = 7;

    iirfilt_rrrf lowpass =
        iirfilt_rrrf_create_lowpass(
            order,
            cutoff / fs
        );

    // تطبيق الفلتر
    for (int i = 0; i < samples; i++) {

        float filtered;

        iirfilt_rrrf_execute(
            lowpass,
            output[i],
            &filtered
        );

        output[i] = filtered;
    }

    // تنظيف الذاكرة
    freqdem_destroy(dem);

    iirfilt_rrrf_destroy(lowpass);
}
