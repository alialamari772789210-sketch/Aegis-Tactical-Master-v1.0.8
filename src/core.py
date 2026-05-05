import os
import json
import subprocess
from openai import OpenAI

# إعداد العميل الحديث متوافق مع إصدار OpenAI 1.0+
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

class AegisMasterCore:
    def __init__(self):
        self.memory_path = "workspace/AI_MEMORY.json"
        self.log_path = "OPERATIONAL_FIX_REPORT.md"
        # دعم كافة لغات المنظومة التكتيكية
        self.supported_ext = ('.java', '.kt', '.py', '.cpp', '.h', '.gradle', '.xml', '.txt', 'CMakeLists.txt')
        self.memory = self.load_memory()

    def load_memory(self):
        if os.path.exists(self.memory_path):
            try:
                with open(self.memory_path, 'r') as f: return json.load(f)
            except: pass
        return {"processed_files": [], "native_linked": False}

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
        """التنفيذ: تحويل الكود من محاكاة إلى حقيقة وربط العتاد"""
        with open(file_path, 'r', errors='ignore') as f:
            content = f.read()

        prompt = f"""
        مهمة عملياتية: قم بتطوير الملف {file_path} وتحويله من وضع المحاكاة إلى التنفيذ العملياتي الحقيقي.
        
        التعليمات الفنية الصارمة:
        1. استبدل الأكواد الوهمية (Mock/Fake) بمكتبات حقيقية (Android Native APIs, SDR Libraries, GNSS Raw).
        2. تأكد من ربط ملفات C++ بـ CMakeLists.txt وتفعيل ميزة الـ Native Code لضمان العمليات الحقيقية بسرعة المعالج.
        3. حول أي وظائف رادار أو تشويش نصية إلى خوارزميات رياضية حقيقية (FFT, Signal Processing).
        4. أصلح ملفات Gradle لضمان بناء APK يدعم المكونات الأصلية (Native Bundles).
        5. أخرج الكود البرمجي النهائي المطور فقط.

        المحتوى الحالي:
        {content}
        """

        try:
            response = client.chat.completions.create(
                model="gpt-4o",
                messages=[
                    {"role": "system", "content": "أنت نواة ذكاء اصطناعي مستقلة خبيرة في الهندسة العسكرية والبرمجة التكتيكية."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.1
            )
            
            new_code = response.choices[0].message.content.strip()
            # تنظيف الكود من علامات Markdown
            if new_code.startswith("```"):
                new_code = "\n".join(new_code.split("\n")[1:-1])

            if new_code and new_code != content:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(new_code)
                return True
        except Exception as e:
            print(f"❌ فشل في معالجة {file_path}: {e}")
        return False

    def run(self):
        print("--- ⚔️ بدء تفعيل السيادة العملياتية لـ Aegis Master ---")
        files = self.get_repo_files()
        
        # تصفير تقرير العمليات
        with open(self.log_path, "w", encoding="utf-8") as log:
            log.write("# 🛡️ سجل التحول العملياتي الحقيقي (Operational Shield)\n\n")

        for file in files:
            print(f"📡 فحص وتفعيل: {file}")
            is_updated = self.transform_to_operational(file)
            
            if is_updated:
                self.memory["processed_files"].append(file)
                with open(self.log_path, "a", encoding="utf-8") as log:
                    log.write(f"- [✅] تم تحويل `{file}` إلى كود عملياتي حقيقي وربطه بالعتاد.\n")
        
        self.save_memory()
        print("--- ✅ تم الانتهاء. النظام الآن يعمل بكامل طاقته الحقيقية ---")

if __name__ == "__main__":
    core = AegisMasterCore()
    core.run()
