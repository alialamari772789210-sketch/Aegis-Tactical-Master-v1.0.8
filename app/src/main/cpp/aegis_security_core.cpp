#include <jni.h>
#include <string>
#include <cstring>
#include <sys/system_properties.h>
#include <fstream>
#include <sstream>
#include <iomanip>
#include <vector>
#include <android/log.h>

#define LOG_TAG "AegisSecurityCore"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jboolean JNICALL
Java_com_jamesfirstok_aegis_security_SovereigntyVerifier_isHardwareVerified(JNIEnv* env, jobject /* this */) {
    char hardware[PROP_VALUE_MAX];
    __system_property_get("ro.hardware", hardware);

    std::ifstream cpuinfo("/proc/cpuinfo");
    std::string line;
    bool has_serial = false;

    if (cpuinfo.is_open()) {
        while (std::getline(cpuinfo, line)) {
            if (line.find("Serial") != std::string::npos) {
                has_serial = true;
                break;
            }
        }
        cpuinfo.close();
    }

    if (has_serial || std::strlen(hardware) > 0) return JNI_TRUE;
    
    LOGE("[CRITICAL] Hardware verification failed. Unauthorized tactical deployment detected!");
    return JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_jamesfirstok_aegis_security_SovereigntyVerifier_getNativeEntropy(JNIEnv* env, jobject /* this */) {
    std::ifstream urandom("/dev/urandom", std::ios::binary);
    if (!urandom) return env->NewStringUTF("ERROR_ACCESS_DENIED");

    unsigned char buffer[32];
    urandom.read(reinterpret_cast<char*>(buffer), 32);
    urandom.close();

    std::ostringstream hexStream;
    hexStream << std::hex << std::setfill('0');
    for (int i = 0; i < 32; ++i) hexStream << std::setw(2) << static_cast<int>(buffer[i]);

    return env->NewStringUTF(hexStream.str().c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_jamesfirstok_aegis_security_SovereigntyVerifier_isSystemSecure(JNIEnv* env, jobject thiz) {
    jboolean hardware_ok = Java_com_jamesfirstok_aegis_security_SovereigntyVerifier_isHardwareVerified(env, thiz);
    
    std::ifstream maps("/proc/self/maps");
    std::string line;
    if (maps.is_open()) {
        while (std::getline(maps, line)) {
            if (line.find("frida") != std::string::npos || line.find("xposed") != std::string::npos) {
                LOGE("[SECURITY ALERT] Injection tool detected in runtime memory maps!");
                maps.close();
                return JNI_FALSE; 
            }
        }
        maps.close();
    }
    return hardware_ok;
}
