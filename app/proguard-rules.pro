# =============================================================================
# AEGIS SOVEREIGN CORE - PROGUARD SECURITY RULES v8.5.2
# درع حماية النواة ومنع الهندسة العكسية وفك التشفير التكتيكي
# المصمم: العقيد المهندس علي العماري
# =============================================================================

# 1. تحسين وضغط الكود لأقصى درجات الأداء العسكري
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# 2. حماية وتعمية مسميات المتغيرات والدوال (Code Obfuscation)
-repackageclasses 'com.jamesfirstok.aegis.internal'
-allowspeculativepacketreordering

# 3. [تأمين حاسم]: منع كسر روابط مكتبة الـ C++ Native (JNI Layer)
# ترك هذه الدوال دون حماية من الحذف لضمان بقاء اتصالات الـ SDR والـ I/Q فعالة
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# 4. [تأمين حاسم]: حماية روابط محرك بايثون (Chaquopy Interface)
# يمنع تعمية كلاسات بايثون لتفادي خطأ تحطم الجسر البرمجي أثناء الاشتباك
-keep class com.chaquo.python.** { *; }
-dontwarn com.chaquo.python.**

# 5. حماية محرك الذكاء الاصطناعي المستقل (TensorFlow Lite Engine)
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

# 6. حماية مكتبة الحسابات الطيفية ومعالجة الإشارات (JTransforms)
-keep class pl.edu.icm.jtransforms.** { *; }
-dontwarn pl.edu.icm.jtransforms.**

# 7. حماية مستودع البيانات التكتيكية وقواعد البيانات (Room Database)
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomOpenHelper
-dontwarn androidx.room.**

# 8. حماية كلاسات الأمان والتشفير العتادي والإنتروبيا (Crypto SharedPreferences)
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# 9. منع حذف أو تعمية جسور الـ JavaScript (WebView WebViewBridge Interface)
# يضمن استمرار تدفق بيانات الـ JSON الحية إلى شاشة الـ HUD الرادارية دون انقطاع
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# 10. الحفاظ على المسميات الاستراتيجية الحاكمة للمنظومة لربط الكود
-keep class com.jamesfirstok.aegis.core.** { *; }
-keep class com.jamesfirstok.aegis.ai.** { *; }
-keep class com.jamesfirstok.aegis.radar.** { *; }
-keep class com.jamesfirstok.aegis.security.** { *; }

# 11. حذف سجلات التطوير العادية وصور الأخطاء عند البناء النهائي لسرية العمليات
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
