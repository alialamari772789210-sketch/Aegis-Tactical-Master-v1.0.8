import os
import json
from openai import OpenAI

# إعداد العميل الحديث متوافق مع إصدار OpenAI 1.0+
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

class AegisMasterCore:
    def __init__(self):
        self.memory_path = "workspace/AI_MEMORY.json"
        self.log_path = "OPERATIONAL_FIX_REPORT.md"
        # دعم كافة لغات المنظومة التكتيكية بما في ذلك ملفات البناء والـ JNI
        self.supported_ext = ('.java', '.kt', '.py', '.cpp', '.h', '.gradle', '.xml', '.txt', 'CMakeLists.txt')
        self.memory = self.load_memory()

    def load_memory(self):
        if not os.path.exists("workspace"): os.makedirs("workspace")
        if os.path.exists(self.memory_path):
            try:
                with open(self.memory_path, 'r') as f: return json.load(f)
            except: pass
        return {"processed_files": [], "native_linked": False, "c2_protocol_active": True}

    def save_memory(self):
        with open(self.memory_path, 'w') as f:
            json.dump(self.memory, f, indent=4)

    def get_repo_files(self):
        """الإدراك: مسح شامل لكل ملفات المنظومة"""
        all_files = []
        for root, dirs, files in os.walk("."):
            if any(x in root for x in ['.git', 'build', '.github', 'workspace', '.idea']):
                continue
            for file in files:
                if file.endswith(self.supported_ext) or file == "CMakeLists.txt":
                    all_files.append(os.path.join(root, file))
        return all_files

    def transform_to_operational(self, file_path):
        """التنفيذ: تفعيل بروتوكول التحييد والقيادة المستقلة مع تصحيح الأخطاء الهيكلية"""
        with open(file_path, 'r', errors='ignore') as f:
            content = f.read()

        prompt = f"""
        المرحلة القتالية الحاسمة: تفعيل 'بروتوكول القيادة والسيطرة المستقل' (Autonomous C2 System) وتصحيح الأخطاء الهيكلية.
        
        المتطلبات العملياتية الصارمة للملف {file_path}:
        1. **تصحيح AndroidManifest**: أصلح فوراً أي تعارض في الصلاحيات أو تكرار في الوسوم، وتأكد من صحة إغلاق الـ Manifest لضمان بناء APK سليم.
        2. **مزامنة JNI (C++)**: في ملفات الـ CPP (مثل AegisCore.cpp)، قم بتصحيح روابط الـ JNI لضمان مطابقتها لاسم الحزمة الفعلي (com.jamesfirstok.aegis) لضمان الربط الصحيح بين العتاد والأندرويد.
        3. **صناعة القرار الذكي (C2)**: قم بحقن خوارزميات اتخاذ القرار الآلي؛ إذا رصد الرادار إشارة معادية، يجب تفعيل مضاد التشويش فوراً.
        4. **التنسيق العملياتي والتحييد**: اربط نتائج الرادار (FFT) بمحرك التحييد (Jamming Engine) واستخدم القفز الترددي (Frequency Hopping).
        5. **الاستقلالية**: استخدم مكتبات (Android Native APIs, SDR) حقيقية بدلاً من أي منطق محاكاة.
        
        أخرج الكود العملياتي النهائي المصلح والمطور فقط:
        {content}
        """

        try:
            response = client.chat.completions.create(
                model="gpt-4o",
                messages=[
                    {"role": "system", "content": "أنت نواة ذكاء اصطناعي سيادية خبيرة في تصحيح أخطاء الـ JNI والـ Android Manifest والأنظمة التكتيكية."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.1
            )
            
            # تصحيح الوصول للاستجابة
            new_code = response.choices[0].message.content.strip()
            if new_code.startswith("```"):
                lines = new_code.split("\n")
                new_code = "\n".join(lines[1:-1])

            if new_code and new_code != content:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(new_code)
                return True
        except Exception as e:
            print(f"❌ فشل في معالجة {file_path}: {e}")
        return False

    def run(self):
        print("--- 🛡️ بدء تفعيل بروتوكول القيادة المستقل والتصحيح الهيكلي لـ Aegis Master ---")
        files = self.get_repo_files()
        
        with open(self.log_path, "w", encoding="utf-8") as log:
            log.write("# ⚔️ سجل القيادة والتصحيح العملياتي الشامل\n\n")

        for file in files:
            print(f"⚙️ تفعيل وتصحيح: {file}")
            is_updated = self.transform_to_operational(file)
            if is_updated:
                self.memory["processed_files"].append(file)
                with open(self.log_path, "a", encoding="utf-8") as log:
                    log.write(f"- [🔥] تم تصحيح وتفعيل النظام في `{file}` بنجاح.\n")
        
        self.save_memory()
        print("--- ✅ تم التصحيح والتحول. النظام مستقر وعملياتي الآن ---")

if __name__ == "__main__":
    core = AegisMasterCore()
    core.run()
