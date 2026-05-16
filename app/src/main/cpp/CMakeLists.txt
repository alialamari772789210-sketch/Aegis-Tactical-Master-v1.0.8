cmake_minimum_required(VERSION 3.22.1)
project("aegis-core")

# [تعديل تكتيكي]: تحديث ملفات المصدر لتطابق بدقة الأسماء المحصنة والمنقحة هندسياً لمنع تعارض الرموز
add_library(aegis-core SHARED
    aegis-core.cpp
    tactical_radar_core.cpp
    aegis_demodulator.cpp
    aegis_security_core.cpp
    aegis_hacker_core.cpp
)

# البحث عن مكتبة سجلات أندرويد لتمكين التنبيهات والاعتراض الطيفي الفوري بالـ Logcat
find_library(log-lib log)

# ربط المكتبات الأساسية بالنواة الصلبة للمنظومة
target_link_libraries(aegis-core ${log-lib})

# استخدام معيار C++17 الصارم لضمان سرعة معالجة تحويلات فورية والـ Vectors الحيوية
set_target_properties(aegis-core PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED ON
)

# خيارات التجميع العسكرية: التحسين الفائق للأداء (-O3)، التسريع الرياضي الجبري للحلقات (-ffast-math)
target_compile_options(aegis-core PRIVATE 
    -O3 
    -ffast-math 
    -DOPERATION_MODE=1
    -funroll-loops
    -fvisibility=hidden # إخفاء الرموز الداخلية للمكتبة لمنع رصد خوارزميات التحييد عبر الهندسة العكسية
)

# 🛠️ [حاقن المسار الاستراتيجي الشامل]: توجيه المترجم لقراءة الحزم الحقيقية
# تم ضبط المسار ليعود خطوة للخلف ليفهم المترجم وجود مجلد liquid/liquid.h الذي أنشأته أنت
target_include_directories(aegis-core PRIVATE 
    ${CMAKE_SOURCE_DIR}
    ${CMAKE_SOURCE_DIR}/..
)

# [تأمين وحقن مكتبة liquid-dsp العسكرية لربط الـ Static Binary]
if(EXISTS "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libliquid.a")
    add_library(liquid STATIC IMPORTED)
    set_target_properties(liquid PROPERTIES IMPORTED_LOCATION
        "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libliquid.a")
    
    target_link_libraries(aegis-core liquid)
    message(STATUS "[AEGIS_BUILD] Liquid-DSP Static Library located and linked successfully.")
else()
    message(WARNING "[AEGIS_BUILD] libliquid.a not found in jniLibs path. Downed to fallback software processing mode.")
endif()
