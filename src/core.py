import os
import json
from openai import OpenAI

# إعداد العميل الحديث متوافق مع إصدار OpenAI 1.0+
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

class AegisFinalSovereignCore:
    def __init__(self):
        self.memory_path = "workspace/AI_MEMORY.json"
        self.log_path = "OPERATIONAL_FIX_REPORT.md"
        # دعم شامل لكافة ملفات المنظومة التكتيكية
        self.supported_ext = ('.java', '.kt', '.py', '.cpp', '.h', '.gradle', '.xml', '.txt', 'CMakeLists.txt')
        self.memory = self.load_memory()

    def load_memory(self):
        if not os.path.exists("workspace"): os.makedirs("workspace")
        if os.path.exists(self.memory_path):
            try:
                with open(self.memory_path, 'r') as f: return json.load(f)
            except: pass
        return {"processed_files": [], "stealth_active": True, "anti_forensics_armed": True}

    def save_memory(self):
        with open(self.memory_path, 'w') as f:
            json.dump(self.memory, f, indent=4)

    def get_repo_files(self):
        """الإدراك الشامل: مسح كافة أجزاء المنظومة"""
        all_files = []
        for root, dirs, files in os.walk("."):
            if any(x in root for x in ['.git', 'build', '.github', 'workspace', '.idea']):
                continue
            for file in files:
                if file.endswith(self.supported_ext) or file == "CMakeLists.txt":
                    all_files.append(os.path.join(root, file))
        return all_files

    def transform_to_fortified_operational(self, file_path):
        """التنفيذ: حقن بروتوكولات الإبادة الذاتية والتحصين الشبحي"""
        with open(file_path, 'r', errors='ignore') as f:
            content = f.read()

        prompt = f"""
        المرحلة النهائية: تفعيل 'بروتوكول الإبادة الذاتية والتحصين' (Anti-Forensics & Self-Destruct) للملف {file_path}.
        
        المتطلبات العملياتية الصارمة (أكواد حقيقية تنفيذية):
        1. **كاشف الاختراق (Intrusion Detector)**: حقن منطق برمي يراقب محاولات الوصول غير المصرح بها أو محاولات فك التشفير (Brute-force) والرد عليها فوراً.
        2. **الإبادة الرقمية (Digital Wipe)**: في حال تأكد الاختراق المادي أو البرمجي، فعل وظيفة مسح فيزيائي (Zero-fill) للملفات الحساسة لجعل استعادتها مستحيلة.
        3. **التحصين الشبحي وHoney-pots**: دمج تقنية الملفات الوهمية لتضليل المخترق وحماية النواة الحقيقية للنظام.
        4. **قفل الطوارئ السيادي**: تطوير كود يغلق كافة منافذ الـ USB والاتصالات اللاسلكية (Kill Switch) عند استشعار خطر محدق.
        5. **الاستقلالية التكتيكية**: استبدل أي بقايا لمحاكاة بأكواد تعامل مباشر مع الـ Kernel والعتاد.

        أخرج الكود العملياتي المطور والنهائي فقط:
        {content}
        """

        try:
            response = client.chat.completions.create(
                model="gpt-4o",
                messages=[
                    {"role": "system", "content": "أنت نواة ذكاء اصطناعي سيادية مسؤولة عن حماية وتحصين الأنظمة الدفاعية ضد الاختراق والتحقيق الجنائي الرقمي."},
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
            print(f"❌ فشل في تحصين {file_path}: {e}")
        return False

    def run(self):
        print("--- ⚔️ تفعيل بروتوكول الأرض المحروقة والتحصين النهائي لـ Aegis Master ---")
        files = self.get_repo_files()
        
        with open(self.log_path, "a", encoding="utf-8") as log:
            log.write("\n## 🔒 بروتوكول الإبادة الذاتية والتحصين (Self-Destruct) مسلح الآن.\n")

        for file in files:
            print(f"🛡️ تحصين وإغلاق: {file}")
            is_updated = self.transform_to_fortified_operational(file)
            if is_updated:
                self.memory["processed_files"].append(file)
        
        self.save_memory()
        print("--- ✅ تمت السيادة الكاملة. النظام الآن محصن، شبحي، ومستقل تماماً ---")

if __name__ == "__main__":
    core = AegisFinalSovereignCore()
    core.run()
