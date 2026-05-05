# تحديث الإصدار ليتطابق تماماً مع ملف build.gradle الخاص بك
cmake_minimum_required(VERSION 3.22.1)

project("aegis-core")

# إضافة ملفات المصدر للعمليات الحقيقية
add_library(
        aegis-core
        SHARED
        AegisCore.cpp
        drone_fusion_bridge.cpp
)

# ربط مكتبة السجلات (Logs) لمراقبة أداء الرادار
find_library(
        log-lib
        log
)

# ربط المكتبة بالموارد الأساسية
target_link_libraries(
        aegis-core
        ${log-lib}
)

# إعدادات معيار C++17 والتحسين القتالي (Optimization)
set_target_properties(aegis-core PROPERTIES 
                     CXX_STANDARD 17 
                     CXX_STANDARD_REQUIRED ON)

# تفعيل التحسين الأقصى للمعالج لضمان استجابة الرادار في الوقت الحقيقي
target_compile_options(aegis-core PRIVATE -O3 -ffast-math -DOPERATION_MODE=1)
