cmake_minimum_required(VERSION 3.22.1)
project("aegis-core")

# ملفات المصدر
add_library(aegis-core SHARED
    AegisCore.cpp
    drone_fusion_bridge.cpp
    tactical_radar_core.cpp
    aegis_jni_bridge.cpp
    demodulate_signal.cpp
    mavlink_hijack.cpp
)

# مكتبة السجلات
find_library(log-lib log)

# ربط المكتبات
target_link_libraries(aegis-core ${log-lib})

# معيار C++17 وتحسينات
set_target_properties(aegis-core PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED ON
)

target_compile_options(aegis-core PRIVATE -O3 -ffast-math -DOPERATION_MODE=1)

# إذا وُجدت مكتبة liquid-dsp
if(EXISTS "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libliquid.a")
    add_library(liquid STATIC IMPORTED)
    set_target_properties(liquid PROPERTIES IMPORTED_LOCATION
        "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libliquid.a")
    target_link_libraries(aegis-core liquid)
endif()
