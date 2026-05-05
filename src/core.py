import os
import json
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
        # التأكد من وجود مجلد workspace
        if not os.path.exists("workspace"):
            os.makedirs("workspace")
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
        """التنفيذ: تفعيل بروتوكول التحييد الدفاعي وتحويل الكود لواقع عملياتي"""
        with open(file_path, 'r', errors='ignore') as f:
            content = f.read()

        prompt = f"""
        المرحلة القتالية: تفعيل 'بروتوكول التحييد الدفاعي' (Defense Neutralization Protocol) للملف {file_path}.
        
        المتطلبات العملياتية الصارمة:
        1. **محرك التشويش (Jamming Engine)**: في ملفات C++ والبايثون، قم بحقن خوارزميات توليد الضوضاء الرقمية والقفز الترددي (Frequency Hopping) للتعمية على الرادارات المعادية.
        2. **إخفاء البصمة (Stealth Mode)**: طور الأكواد لتقليل انبعاثات الإشارات (RF Signature) وتشفير الاتصالات بنظام AES-256.
        3. **الرادار الإيجابي والسلبي**: حول منطق المسح إلى معالجة إشارات حقيقية (DSP) قادرة على اكتشاف محاولات الاختراق اللاسلكي والرد عليها بالتحييد.
        4. **ربط العتاد**: تأكد من ربط ملفات C++ بـ CMakeLists.txt وتفعيل ميزة الـ Native Code لضمان السرعة القتالية.
        5. استبدل أي منطق محاكاة متبقي بمكتبات تنفيذية (Android Native APIs, SDR Libraries, GNSS Raw) فوراً.

        أخرج الكود العملياتي النهائي المطور فقط:
        {content}
        """

        try:
            response = client.chat.completions.create(
                model="gpt-4o",
                messages=[
                    {"role": "system", "content": "أنت نواة ذكاء اصطناعي سيادية مصممة لتحويل المشاريع البحثية إلى أنظمة حرب إلكترونية عملياتية."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.1
            )
            
            # تصحيح الوصول للرد في الإصدارات الحديثة
            new_code = response.choices[0].message.content.strip()
            
            # تنظيف الكود من علامات Markdown
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
        print("--- ⚔️ بدء تفعيل السيادة العملياتية وبروتوكول التحييد لـ Aegis Master ---")
        files = self.get_repo_files()
        
        # تصفير تقرير العمليات
        with open(self.log_path, "w", encoding="utf-8") as log:
            log.write("# 🛡️ سجل التحول العملياتي الحقيقي (Operational Shield)\n\n")

        for file in files:
            print(f"📡 فحص وتفعيل التحييد في: {file}")
            is_updated = self.transform_to_operational(file)
            
            if is_updated:
                self.memory["processed_files"].append(file)
                with open(self.log_path, "a", encoding="utf-8") as log:
                    log.write(f"- [✅] تم تفعيل بروتوكول التحييد في `{file}` بنجاح.\n")
        
        self.save_memory()
        print("--- ✅ تم الانتهاء. المنظومة الدفاعية في وضع العمليات القتالية الآن ---")

if __name__ == "__main__":
    core = AegisMasterCore()
    core.run()
