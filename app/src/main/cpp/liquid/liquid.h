#ifndef __LIQUID_H__
#define __LIQUID_H__

#include <complex>

#ifdef __cplusplus
extern "C" {
#endif

// ==============================================================================
// [البنية الأساسية الموحدة]: التوافق التام مع بيئة معالجة الإشارات الرقمية C++
// ==============================================================================
#define LIQUID_VERSION "1.5.0"

// 🛠️ [تصحيح نمط البيانات]: تحويل العتاد الإشاري لاستخدام كلاس المعقد القياسي لـ C++ لإنهاء تعارض المترجم
typedef std::complex<float> liquid_float_complex;

// اتجاهات التحويل الطيفي الفوري الفعلي (FFT Directions)
#define LIQUID_FFT_FORWARD  -1
#define LIQUID_FFT_BACKWARD  1

// ==============================================================================
// [تعريف كائنات معالجة الإشارة الرقمية DSP الحقيقية]
// ==============================================================================
typedef void* firfilt_cccf;     // فلتر الترددات المركبة (Kaiser / Polyphase)
typedef void* fftplan;          // خطة معالجة تحويل فوريه السريع والترددات الطيفية
typedef void* ampmodem;         // وحدة فك تشفير وتعديل السعة الإشارية (AM)
typedef void* freqdem;          // وحدة فك تشفير الترددات الرادارية وموجات الـ FM
typedef void* nco_crcf;         // المذبذب المحلي المحكوم رقمياً لتوليد وتتبع الموجات
typedef void* symsync_crcf;     // وحدة مزامنة النبضات والرموز اللاسلكية فورياً
typedef void* agc_crcf;         // وحدة التحكم التلقائي في مستوى الإشارة وتخفيض الضوضاء
typedef void* firinterp_crcf;   // وحدة استكمال وتكثيف عينات الإشارة المستلمة
typedef void* liquid_fec;       // وحدة تصحيح أخطاء البيانات وحماية الاعتراض من التشويش

// ==============================================================================
// [التوقيعات الهندسية للدوال الحقيقية المربوطة بملف libliquid.a الثابت]
// ==============================================================================

// 1. فلاتر الترددات وحصر النطاق الرقمي (Finite Impulse Response)
firfilt_cccf firfilt_cccf_create_kaiser(unsigned int _len, float _fc, float _as, float _mu);
void firfilt_cccf_execute_block(firfilt_cccf _f, liquid_float_complex * _x, unsigned int _n, liquid_float_complex * _y);
void firfilt_cccf_destroy(firfilt_cccf _f);

// 2. معالجة مصفوفات الأقمار وتحويل فوريه السريع (FFT Analysis)
fftplan fft_create_plan(unsigned int _n, liquid_float_complex * _x, liquid_float_complex * _y, int _dir, int _flags);
void fft_execute(fftplan _p);
void fft_destroy_plan(fftplan _p);

// 3. فك التشفير وتعديل الإشارات اللاسلكية والتكتيكية (Modulation / Demodulation)
ampmodem ampmodem_create(float _mod_index, int _type);
void ampmodem_demodulate(ampmodem _q, liquid_float_complex _r, float * _m);
void ampmodem_destroy(ampmodem _q);

freqdem freqdem_create(float _kf);
void freqdem_demodulate_block(freqdem _q, liquid_float_complex * _r, unsigned int _n, float * _m);
void freqdem_destroy(freqdem _q);

// 4. تتبع الترددات والمذبذبات (Numerically-Controlled Oscillators)
nco_crcf nco_crcf_create(int _type);
void nco_crcf_set_frequency(nco_crcf _q, float _f);
void nco_crcf_mix_block_down(nco_crcf _q, liquid_float_complex * _x, unsigned int _n, liquid_float_complex * _y);
void nco_crcf_destroy(nco_crcf _q);

// 5. المزامنة والتحكم الآلي بمستوى طاقة الإشارة (Synchronization & AGC)
symsync_crcf symsync_crcf_create_rnyquist(int _type, unsigned int _k, unsigned int _m, float _beta, unsigned int _num_filters);
void symsync_crcf_execute(symsync_crcf _q, liquid_float_complex * _x, unsigned int _nx, liquid_float_complex * _y, unsigned int * _ny);
void symsync_crcf_destroy(symsync_crcf _q);

agc_crcf agc_crcf_create(void);
void agc_crcf_set_bandwidth(agc_crcf _q, float _bw);
void agc_crcf_execute_block(agc_crcf _q, liquid_float_complex * _x, unsigned int _n, liquid_float_complex * _y);
void agc_crcf_destroy(agc_crcf _q);

#ifdef __cplusplus
}
#endif

#endif // __LIQUID_H__
