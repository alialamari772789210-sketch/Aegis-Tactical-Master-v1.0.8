#include <stdio.h>
#include <stdlib.h>
#include <complex.h>
#include <math.h>

#include <liquid/liquid.h>

extern "C"
void tactical_radar_core(
        float complex* rx_buffer,
        int samples
) {

    // طول فلتر منطقي للهاتف
    unsigned int filter_len = 64;

    float bw = 0.2f;

    float attenuation = 60.0f;

    firfilt_cccf filter =
        firfilt_cccf_create_kaiser(
            filter_len,
            bw,
            attenuation,
            0.0f
        );

    // buffer منفصل للخرج
    float complex* filtered =
        (float complex*)
            malloc(sizeof(float complex) * samples);

    if (!filtered) {
        return;
    }

    // تطبيق الفلتر
    firfilt_cccf_execute_block(
        filter,
        rx_buffer,
        samples,
        filtered
    );

    // FFT output
    float complex* fft_out =
        (float complex*)
            malloc(sizeof(float complex) * samples);

    if (!fft_out) {

        free(filtered);

        return;
    }

    // نسخ البيانات
    memcpy(
        fft_out,
        filtered,
        sizeof(float complex) * samples
    );

    // FFT
    fftplan q =
        fft_create_plan(
            samples,
            fft_out,
            fft_out,
            LIQUID_FFT_FORWARD,
            0
        );

    fft_execute(q);

    // حساب متوسط القدرة
    float avg_power = 0.0f;

    for (int i = 0; i < samples; i++) {

        avg_power += cabsf(fft_out[i]);
    }

    avg_power /= samples;

    // Threshold ديناميكي
    float threshold =
        avg_power * 4.0f;

    // كشف الأهداف
    for (int i = 0; i < samples; i++) {

        float power =
            cabsf(fft_out[i]);

        if (power > threshold) {

            printf(
                "TACTICAL_TARGET bin=%d power=%f\n",
                i,
                power
            );
        }
    }

    // إعادة النتائج
    memcpy(
        rx_buffer,
        fft_out,
        sizeof(float complex) * samples
    );

    // تنظيف
    fft_destroy_plan(q);

    firfilt_cccf_destroy(filter);

    free(filtered);

    free(fft_out);
}
