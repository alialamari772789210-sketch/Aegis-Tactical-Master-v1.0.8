#include <stdio.h>
#include <stdlib.h>
#include <complex>
#include <math.h>
#include <string.h>
#include <vector>
#include <android/log.h>
#include <liquid/liquid.h>

#define LOG_TAG "AegisTacticalRadarCore"
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

extern "C"
void tactical_radar_core(std::complex<float>* rx_buffer, int samples) {
    if (samples <= 0 || rx_buffer == nullptr) return;

    unsigned int filter_len = 64;
    float bw = 0.2f; 
    float attenuation = 60.0f; 

    firfilt_cccf filter = firfilt_cccf_create_kaiser(filter_len, bw, attenuation, 0.0f);
    std::vector<std::complex<float>> filtered(samples);

    firfilt_cccf_execute_block(
        filter,
        reinterpret_cast<liquid_float_complex*>(rx_buffer),
        samples,
        reinterpret_cast<liquid_float_complex*>(filtered.data())
    );

    std::vector<std::complex<float>> fft_out(samples);
    std::memcpy(fft_out.data(), filtered.data(), sizeof(std::complex<float>) * samples);

    fftplan q = fft_create_plan(
        samples,
        reinterpret_cast<liquid_float_complex*>(fft_out.data()),
        reinterpret_cast<liquid_float_complex*>(fft_out.data()),
        LIQUID_FFT_FORWARD,
        0
    );
    fft_execute(q);

    float avg_power = 0.0f;
    for (int i = 0; i < samples; i++) avg_power += std::abs(fft_out[i]);
    avg_power /= samples;

    float threshold = avg_power * 3.8f; 
    for (int i = 0; i < samples; i++) {
        float power = std::abs(fft_out[i]);
        if (power > threshold) {
            LOGW("[!] HARDWARE LOCK: Signal at Spectrum Bin [%d], Power: %.2f", i, power);
        }
    }

    std::memcpy(rx_buffer, fft_out.data(), sizeof(std::complex<float>) * samples);
    fft_destroy_plan(q);
    firfilt_cccf_destroy(filter);
}
