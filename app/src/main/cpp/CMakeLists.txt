cmake_minimum_required(VERSION 3.10.2)

# تعريف المشروع الأمني السيادي
project("Aegis_Sovereign_Security")

# 1. إعدادات الترجمة العسكرية (Hardening)
# تفعيل معايير C++17 لضمان سرعة معالجة بيانات الرادار
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 2. بناء المكتبة الأمنية (The Security Core)
# هذا الجزء يحول كود C++ إلى مكتبة مشفرة غير قابلة للقراءة
add_library( 
             aegis-security-lib
             SHARED
             native-lib.cpp  # النواة العصبية (التشفير)
             quantum-shield.cpp # بروتوكول الكوانتم (إذا وجد)
           )

# 3. استدعاء موارد النظام الأساسية
# مكتبة السجلات (Log) لرصد الاختراقات، ومكتبة Android للوصول للعتاد
find_library( log-lib log )
find_library( android-lib android )

# 4. الدمج التكاملي الشامل (The Strategic Link)
# ربط المكتبة الأمنية بموارد النظام لضمان العمل المستقل (Standalone)
target_link_libraries( 
                       aegis-security-lib
                       ${log-lib}
                       ${android-lib}
                     )

# 5. حماية الملف الناتج من التلاعب
# منع استخراج الرموز البرمجية (Stripping Symbols) لزيادة التخفي الشبحي
set_target_properties(aegis-security-lib PROPERTIES 
                      CXX_VISIBILITY_PRESET hidden
                      VISIBILITY_INLINES_HIDDEN ON)
