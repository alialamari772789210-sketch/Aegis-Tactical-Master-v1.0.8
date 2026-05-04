cmake_minimum_required(VERSION 3.10.2)

# مشروع الحماية السيادية - AEGIS TACTICAL
project("Aegis_Sovereign_Security")

# إعدادات المعالجة التكتيكية (C++17) لضمان أقصى سرعة في تحليل الإشارات
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# بناء المكتبة الأمنية المحصنة (النواة الصلبة)
# تم إدراج جميع الملفات المصدرية (native-lib و aegis-security-lib) لضمان اكتمال الربط البرمجي
add_library( 
             aegis-security-lib
             SHARED
             native-lib.cpp
             aegis-security-lib.cpp 
           )

# استدعاء مكتبات النظام الأساسية للتعامل مع السجلات والعتاد
find_library( log-lib log )
find_library( android-lib android )
find_library( jnigraphics-lib jnigraphics ) # لمعالجة صور الرادار بذكاء

# الربط التكاملي بين النواة والعتاد لضمان تدفق البيانات
target_link_libraries( 
                       aegis-security-lib
                       ${log-lib}
                       ${android-lib}
                       ${jnigraphics-lib}
                     )

# بروتوكول التخفي الرقمي (Anti-Reverse Engineering)
# إخفاء الرموز البرمجية لمنع الهندسة العكسية من قبل أي طرف خارجي
set_target_properties(aegis-security-lib PROPERTIES 
                      CXX_VISIBILITY_PRESET hidden
                      VISIBILITY_INLINES_HIDDEN ON
                      POSITION_INDEPENDENT_CODE ON)

# إعدادات تحسين الأداء الميداني (Optimization for Release)
if(NOT CMAKE_BUILD_TYPE OR CMAKE_BUILD_TYPE STREQUAL "Release")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -fomit-frame-pointer -fdata-sections -ffunction-sections")
endif()
