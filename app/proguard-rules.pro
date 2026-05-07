# 1. حماية مكتبات TensorFlow من التشفير لضمان عمل الذكاء الاصطناعي
-keep class org.tensorflow.** { *; }
-keep interface org.tensorflow.** { *; }

# 2. حماية دوال JNI (الربط بين Java و C++)
# هذا يمنع تغيير أسماء الدوال التي تنتهي بـ 'native'
-keepclasseswithmembernames class * {
    native <methods>;
}

# 3. حماية كلاسات الـ Model (لأن Room و Gson يحتاجان الأسماء الحقيقية)
-keep class com.jamesfirstok.aegis.model.** { *; }

# 4. حماية مكتبة RootBeer الأمنية لضمان استمرار فحص النظام
-keep class com.scottyab.rootbeer.** { *; }
