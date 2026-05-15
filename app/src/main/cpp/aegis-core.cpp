#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <complex>
#include <vector>
#include <android/log.h>
#include <cstring>
#include <liquid/liquid.h>

#define LOG_TAG "AegisTacticalCore"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

extern "C" void tactical_radar_core(std::complex<float> * rx_buffer, int samples);

bool detect_drone_signature(const double* magnitude_data, int len) {
    if (len <= 0) return false;
    double sum = 0.0;
    for (int i = 0; i < len; i++) sum += magnitude_data[i];
    double avg = sum / len;
    double threshold = avg * 3.5;

    int signal_count = 0;
    for (int i = 0; i < len; i++) {
        if (magnitude_data[i] > threshold) signal_count++;
    }
    return signal_count > (len * 0.02);
}

extern "C" {
    void send_to_radio_driver(const uint8_t* data, size_t len) {
        LOGI("[PHY] Broadcasting Inject Frame (%zu bytes) to SDR Array.", len);
    }

    void injectProprietaryFrame(const char* frame_data, size_t len) {
        if (frame_data != nullptr && len > 0) {
            send_to_radio_driver(reinterpret_cast<const uint8_t*>(frame_data), len);
        }
    }

    bool executeMavlinkV2Override(int last_seq) {
        LOGW("[EW] MAVLink Command Hijack Triggered. Target Seq: %d", last_seq);
        uint8_t packet[128];
        uint16_t i = 0;
        packet[i++] = 0xFD; // Start Sign MAVLink v2
        packet[i++] = 33;   
        packet[i++] = 0x00; packet[i++] = 0x00;
        packet[i++] = static_cast<uint8_t>((last_seq + 3) % 256); 
        packet[i++] = 255;  packet[i++] = 190; 
        packet[i++] = 0x4C; packet[i++] = 0x00; packet[i++] = 0x00;
        
        float cmd = 21.0f; // NAV_LAND
        std::memcpy(&packet[i], &cmd, 4); i += 4;
        for (int j = 0; j < 24; j++) packet[i++] = 0x00;
        packet[i++] = 1; packet[i++] = 1; packet[i++] = 0;

        send_to_radio_driver(packet, i);
        return true;
    }

    float activateControlLoop() {
        static float last_error = 0.0f;
        static float integral = 0.0f;
        float error = 2412.0f - 2415.0f; 
        integral += error;
        float derivative = error - last_error;
        float output = (0.5f * error) + (0.1f * integral) + (0.05f * derivative);
        last_error = error;
        return output;
    }
}

extern "C" {
    JNIEXPORT jdoubleArray JNICALL
    Java_com_jamesfirstok_aegis_radar_TacticalRadar_processSignal(
            JNIEnv *env, jobject thiz, jdoubleArray raw_signal) {
        
        jsize len = env->GetArrayLength(raw_signal);
        jdouble *body = env->GetDoubleArrayElements(raw_signal, nullptr);
        size_t complex_len = len / 2;

        if (complex_len == 0) {
            env->ReleaseDoubleArrayElements(raw_signal, body, 0);
            return env->NewDoubleArray(0);
        }

        std::vector<std::complex<float>> rx_buffer(complex_len);
        for (size_t i = 0; i < complex_len; i++) {
            rx_buffer[i] = std::complex<float>(static_cast<float>(body[2 * i]), static_cast<float>(body[2 * i + 1]));
        }

        tactical_radar_core(rx_buffer.data(), complex_len);

        jdoubleArray result = env->NewDoubleArray(complex_len);
        std::vector<double> fill(complex_len);
        for (size_t i = 0; i < complex_len; i++) fill[i] = static_cast<double>(std::abs(rx_buffer[i]));

        if (detect_drone_signature(fill.data(), complex_len)) {
            LOGW("⚠️ [ALERT] Drone Signature Detected via Liquid-DSP Complex Stream.");
        }

        env->SetDoubleArrayRegion(result, 0, complex_len, fill.data());
        env->ReleaseDoubleArrayElements(raw_signal, body, 0);
        return result;
    }
}
