# تحديد الحد الأدنى لإصدار CMake المطلوبة لبناء النظام التكتيكي
cmake_minimum_required(VERSION 3.18.1)

# تعريف اسم المشروع البرمجي الأساسي
project("aegis-core")

# إضافة ملفات المصدر الحقيقية (Native C++) التي ستقوم بالعمليات القتالية
# يمكنك إضافة أي ملفات .cpp إضافية هنا ليتم دمجها في النواة
add_library(
        aegis-core
        SHARED
        AegisCore.cpp
        drone_fusion_bridge.cpp # جسر الربط مع أنظمة المسيرات
)

# البحث عن المكتبات الأساسية في نظام أندرويد لربطها بالرادار
# مكتبة log: لتسجيل العمليات التكتيكية في السجل
find_library(
        log-lib
        log
)

# ربط المكتبة التي أنشأناها بمكتبات النظام
# هذا يسمح لـ AegisCore بالوصول إلى موارد النظام العميقة
target_link_libraries(
        aegis-core
        ${log-lib}
)

# تحسين أداء الكود البرمجي (Optimization)
# نستخدم -O3 لضمان أعلى سرعة معالجة ممكنة لإشارات الرادار والترددات
set_target_properties(aegis-core PROPERTIES 
                     CXX_STANDARD 17 
                     CXX_STANDARD_REQUIRED ON)

target_compile_options(aegis-core PRIVATE -O3 -ffast-math)
