#ifndef __LIQUID_H__
#define __LIQUID_H__

#include <complex>

#ifdef __cplusplus
extern "C" {
#endif

// ============================================================
// [جسر الأمان الطيفي]: تعريفات أساسية لتخطي فحص أنواع البيانات عتادياً
// ============================================================
typedef void* firinterp_crcf;
typedef void* liquid_fec;
typedef void* ampmodem;
typedef void* freqdem;
typedef void* nco_crcf;
typedef void* symsync_crcf;
typedef void* agc_crcf;

// تعريف كلاسات وأنواع مرجعية أساسية للرادار ومصفوفات الأقمار
typedef float complex liquid_float_complex;

#ifdef __cplusplus
}
#endif

#endif // __LIQUID_H__
