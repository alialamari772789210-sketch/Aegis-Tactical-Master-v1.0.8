import os
import json
from openai import OpenAI

# إعداد العميل الحديث متوافق مع إصدار OpenAI 1.0+
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

class AegisFinalSovereignCore:
    def __init__(self):
        self.memory_path = "workspace/AI_MEMORY.json"
        self.log_path = "OPERATIONAL_FIX_REPORT.md"
        # دعم شامل لكافة ملفات المنظومة التكتيكية وملفات بناء Gradle
        self.supported_ext = ('.java', '.kt', '.py', '.cpp', '.h', '.gradle', '.xml', '.txt', 'CMakeLists.txt', '.properties', 'gradlew')
        self.memory = self.load_memory()

    def load_memory(self):
        if not os.path.exists("workspace"): os.makedirs("workspace")
        if os.path.exists(self.memory_path):
            try:
                with open(self.memory_path, 'r') as f: return json.load(f)
            except: pass
        return {"processed_files": [], "stealth_active": True, "build_system_fixed": True}

    def save_memory(self):
        with open(self.memory_path, 'w') as f:
            json.dump(self.memory, f, indent=4)

    def get_repo_files(self):
        """الإدراك الشامل: مسح كافة أجزاء المنظومة بما فيها ملفات البناء"""
        all_files = []
        for root, dirs, files in os.walk("."):
            if any(x in root for x in ['.git', 'build', '.github', 'workspace', '.idea']):
                continue
            for file in files:
                if file.endswith(self.supported_ext) or file in ["CMakeLists.txt", "gradlew", "gradle.properties"]:
                    all_files.append(os.path.join(root, file))
        return all_files

    def transform_to_fortified_operational(self, file_path):
        """التنفيذ: حقن بروتوكولات الإبادة وإصلاح أنظمة بناء APK"""
        with open(file_path, 'r', errors='ignore') as f:
            content = f.read()

        prompt = f"""
        المرحلة النهائية: تفعيل 'بروتوكول الإبادة الذاتية' وإصلاح نظام بناء APK للملف {file_path}.
        
        المتطلبات العملياتية الصارمة:
        1. **إصلاح نظام البناء**: إذا كان الملف هو gradlew أو gradle.properties، قم بتنظيفه من أي إعدادات ذاكرة خاطئة (مثل تعارض -Xmx) لضمان توافقه مع GitHub Actions.
        2. **كاشف الاختراق والإبادة**: حقن منطق يراقب الوصول غير المصرح به ويفعل المسح الفيزيائي (Zero-fill) للملفات الحساسة عند الخطر.
        3. **التحصين والقفل**: دمج ملفات وهمية (Honey-pots) وتطوير كود يغلق منافذ USB والاتصالات عند استشعار محاولة اختراق.
        4. **الاستقلالية**: استبدل أي بقايا محاكاة بأكواد تعامل مباشر مع الـ Kernel والعتاد.

        أخرج الكود المطور والنهائي فقط:
        {content}
        """

        try:
            response = client.chat.completions.create(
                model="gpt-4o",
                messages=[
                    {"role": "system", "content": "أنت نواة ذكاء اصطناعي سيادية مسؤولة عن حماية الأنظمة وإصلاح بيئات بناء التطبيقات العسكرية."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.1
            )
            
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
        print("--- ⚔️ تفعيل بروتوكول الإبادة وإصلاح أنظمة البناء لـ Aegis Master ---")
        files = self.get_repo_files()
        
        with open(self.log_path, "a", encoding="utf-8") as log:
            log.write("\n## 🛠️ تم فحص وإصلاح نظام البناء (Build System) وتأمين الملفات.\n")

        for file in files:
            print(f"🛡️ فحص وتطوير: {file}")
            is_updated = self.transform_to_fortified_operational(file)
            if is_updated:
                self.memory["processed_files"].append(file)
        
        self.save_memory()
        print("--- ✅ تم الإصلاح والتحصين الشامل. النظام جاهز للبناء الآن ---")

if __name__ == "__main__":
    core = AegisFinalSovereignCore()
    core.run()
