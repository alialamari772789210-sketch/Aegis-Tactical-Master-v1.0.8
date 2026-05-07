import os
import json
import subprocess
import stat
from openai import OpenAI

# إعداد العميل الحديث - يتطلب وجود OPENAI_API_KEY في أسرار المستودع
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

class AegisFinalSovereignCore:
    def __init__(self):
        self.memory_path = "workspace/AI_MEMORY.json"
        self.log_path = "OPERATIONAL_FIX_REPORT.md"
        # دعم شامل لكافة ملفات المنظومة والبيئة البرمجية
        self.supported_ext = ('.java', '.kt', '.py', '.cpp', '.h', '.gradle', '.xml', '.txt', 'CMakeLists.txt', '.properties')
        self.memory = self.load_memory()
        
        # تعريف الخريطة التكتيكية للمسارات المطلوبة (المطابقة لـ agent_executor)
        self.tactical_map = {
            "tools": {
                "__init__.py": "# Aegis Tactical Package",
                "radar.py": "def tactical_radar_scan(): pass",
                "jammer.py": "def real_neutralize(): pass",
                "radio_acquisition.py": "def acquire_rf_signature(): pass"
            }
        }

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

    def fortify_tactical_paths(self):
        """تأمين المسارات: التأكد من مطابقة أسماء الملفات والمجلدات لمتطلبات الاستدعاء"""
        print("🔍 جاري تأمين خطوط الإمداد البرمجية (Path Fortification)...")
        for folder, files in self.tactical_map.items():
            if not os.path.exists(folder):
                os.makedirs(folder)
                print(f"✅ تم إنشاء مجلد العمليات المفقود: {folder}")
            
            for file_name, placeholder in files.items():
                file_path = os.path.join(folder, file_name)
                if not os.path.exists(file_path):
                    with open(file_path, "w", encoding="utf-8") as f:
                        f.write(placeholder)
                    print(f"⚠️ تم تعويض النقص التكتيكي: {file_path}")
        print("🛡️ جميع المسارات الآن مطابقة لبروتوكول الاستدعاء.")

    def secure_erase(self, file_path):
        """بروتوكول الإبادة الفيزيائية: المسح بالأصفار لمنع الاستعادة"""
        try:
            size = os.path.getsize(file_path)
            with open(file_path, "ba+", buffering=0) as f:
                f.write(b"\x00" * size)
            os.remove(file_path)
            return True
        except: return False

    def set_operational_permissions(self, file_path):
        """منح صلاحيات التنفيذ للملفات الحيوية"""
        if file_path.endswith('.sh') or 'gradlew' in file_path:
            st = os.stat(file_path)
            os.chmod(file_path, st.st_mode | stat.S_IEXEC)

    def get_repo_files(self):
        all_files = []
        for root, dirs, files in os.walk("."):
            if any(x in root for x in ['.git', 'build', '.github', 'workspace', '.idea', 'venv']):
                continue
            for file in files:
                if file.endswith(self.supported_ext) or file in ["gradlew", "gradle.properties", "settings.gradle"]:
                    all_files.append(os.path.join(root, file))
        return all_files

    def transform_to_fortified_operational(self, file_path):
        """الحقن العملياتي: استخدام GPT-4o لتحويل الكود من محاكاة إلى حقيقة"""
        with open(file_path, 'r', errors='ignore') as f:
            content = f.read()

        prompt = f"""
        الملف: {file_path}
        المهمة: تحويل الكود إلى صيغة 'عملياتية حقيقية' (Operational).
        1. إذا كان ملف بناء (Gradle): أصلح تعارضات الذاكرة وتأكد من الربط الصحيح لـ JNI و Python.
        2. إذا كان ملف برمجياً (C++/Kotlin/Python): استبدل أي محاكاة بأوامر Kernel أو التعامل المباشر مع الـ Hardware.
        3. حقن 'كاشف العبث': إذا تم تعديل الكود من جهة غير مصرحة، يجب أن يقوم بتعطيل الوظائف الحساسة.
        4. الإبادة: تأكد من وجود دوال تمسح البيانات الحساسة من الـ RAM فور انتهاء المهمة.
        
        أخرج الكود المطور فقط:
        """

        try:
            response = client.chat.completions.create(
                model="gpt-4o",
                messages=[
                    {"role": "system", "content": "أنت نواة السيادة الرقمية لـ Aegis Master. مهمتك التحصين وتفعيل الأنظمة الميدانية."},
                    {"role": "user", "content": prompt + "\n" + content}
                ],
                temperature=0.1
            )
            new_code = response.choices[0].message.content.strip().strip("```python").strip("```")
            
            if new_code and len(new_code) > 10:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(new_code)
                self.set_operational_permissions(file_path)
                return True
        except Exception as e:
            print(f"⚠️ خطأ في معالجة {file_path}: {e}")
        return False

    def run(self):
        print("--- ⚔️ بدء تفعيل بروتوكول السيادة والإصلاح الشامل لـ Aegis ---")
        
        # خطوة استباقية: تأمين المسارات قبل البدء
        self.fortify_tactical_paths()
        
        files = self.get_repo_files()
        
        for file in files:
            print(f"🛡️ تحصين وتنفيذ: {file}")
            if self.transform_to_fortified_operational(file):
                self.memory["processed_files"].append(file)
        
        self.save_memory()
        print("--- ✅ النظام محصن، ملفات البناء جاهزة، والبروتوكولات مفعلة ---")

if __name__ == "__main__":
    core = AegisFinalSovereignCore()
    core.run()
