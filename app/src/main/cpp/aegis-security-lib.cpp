#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_jamesfirstok_aegis_MainActivity_validateSecurity(
        JNIEnv* env,
        jobject /* this */) {

    std::string msg = "AEGIS SECURITY CORE ACTIVE";

    return env->NewStringUTF(msg.c_str());
}
