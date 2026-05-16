#ifndef __LIQUID_H__
#define __LIQUID_H__

// تأمين جلب مكتبات الأعداد المعقدة للغتين معاً لمنع أي تعارض إشاري
#include <complex>

#ifndef __cplusplus
#include <complex.h>
#endif

#ifdef __cplusplus
extern "C" {
#endif

#define LIQUID_VERSION "1.5.0"

// [حاقن التوافقية الطيفية]: تعريف مرن يفهمه المترجم كـ std::complex في C++ وكـ complex float في C
#ifdef __cplusplus
typedef std::complex<float> liquid_float_complex;
#else
typedef float complex liquid_float_complex;
#endif

#define LIQUID_FFT_FORWARD  -1
#define LIQUID_FFT_BACKWARD  1

// ==============================================================================
// [كائنات معالجة الإشارة الرقمية DSP والتحييد]
// ==============================================================================
typedef void* firfilt_cccf;
typedef void* fftplan;
typedef void* ampmodem;
typedef void* freqdem;
typedef void* nco_crcf;
typedef void* symsync_crcf;
typedef void* agc_crcf;
typedef void* firinterp_crcf;
typedef void* liquid_fec;
typedef void* iirfilt_rrrf;     // 🛠️ [إضافة حاسمة]: كائن الفلتر الصوتي للـ Demodulator

// ==============================================================================
// [الدوال العملياتية الحقيقية المستدعاة في النواة والأمن والتعديل والصوت]
// ==============================================================================

// دالات الفلترة والـ FFT
firfilt_cccf firfilt_cccf_create_kaiser(unsigned int _len, float _fc, float _as, float _mu);
void firfilt_cccf_execute_block(firfilt_cccf _f, liquid_float_complex * _x, unsigned int _n, liquid_float_complex * _y);
void firfilt_cccf_destroy(firfilt_cccf _f);

fftplan fft_create_plan(unsigned int _n, liquid_float_complex * _x, liquid_float_complex * _y, int _dir, int _flags);
void fft_execute(fftplan _p);
void fft_destroy_plan(fftplan _p);

// 🛠️ [إضافة استراتيجية]: دالات فلتر الصوت IIR المضافة لتلبية شروط ملف aegis_demodulator.cpp
iirfilt_rrrf iirfilt_rrrf_create_lowpass(unsigned int _order, float _fc);
void iirfilt_rrrf_execute_block(iirfilt_rrrf _f, float * _x, unsigned int _n, float * _y);
void iirfilt_rrrf_destroy(iirfilt_rrrf _f);

// دالات فك التعديل الطيفي والترددي 
ampmodem ampmodem_create(float _mod_index, int _type);
void ampmodem_demodulate(ampmodem _q, liquid_float_complex _r, float * _m);
void ampmodem_demodulate_block(ampmodem _q, liquid_float_complex * _r, unsigned int _n, float * _m);
void ampmodem_destroy(ampmodem _q);

freqdem freqdem_create(float _kf);
void freqdem_demodulate_block(freqdem _q, liquid_float_complex * _r, unsigned int _n, float * _m);
void freqdem_destroy(freqdem _q);

// دالات التحكم بالمذبذبات والتتبع 
nco_crcf nco_crcf_create(int _type);
void nco_crcf_set_frequency(nco_crcf _q, float _f);
void nco_crcf_mix_block_down(nco_crcf _q, liquid_float_complex * _x, unsigned int _n, liquid_float_complex * _y);
void nco_crcf_mix_block_up(nco_crcf _q, liquid_float_complex * _x, unsigned int _n, liquid_float_complex * _y);
void nco_crcf_destroy(nco_crcf _q);

// دالات المزامنة والتحكم التلقائي في الكسب (AGC)
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
