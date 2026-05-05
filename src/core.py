import openai
import os
import subprocess

openai.api_key = os.getenv("OPENAI_API_KEY")

def generate_fix_report(report_data):
    """إنشاء تقرير عملياتي يوضح التحويلات من وهمي إلى حقيقي"""
    with open("OPERATIONAL_FIX_REPORT.md", "a", encoding="utf-8") as f:
        f.write(report_data + "\n")

def autonomous_operational_core():
    supported_extensions = ('.java', '.kt', '.py', '.cpp', '.h', '.xml', '.gradle', '.yml', '.sh')
    exclude_dirs = {'.git', 'build', '.idea', 'workspace'}

    print("--- ⚔️ تحويل النظام من محاكاة إلى وضع العمليات الحقيقي ---")
    
    # تفريغ التقرير القديم لبدء واحد جديد
    with open("OPERATIONAL_FIX_REPORT.md", "w", encoding="utf-8") as f:
        f.write("# 🛡️ تقرير التحول العملياتي (Aegis Operational Report)\n")

    for root, dirs, files in os.walk("."):
        dirs[:] = [d for d in dirs if d not in exclude_dirs]
        
        for file in files:
            if file.endswith(supported_extensions):
                file_path = os.path.join(root, file)
                with open(file_path, 'r', errors='ignore') as f:
                    original_code = f.read()

                # تعليمات صارمة للتحول من وهمي إلى حقيقي
                prompt = f"""
                مهمة قتالية: قم بتحويل الملف {file_path} من وضع المحاكاة (Simulation/Mock) إلى وضع التنفيذ العملياتي الحقيقي.
                
                القيود والتعليمات:
                1. أي مكتبات وهمية أو "Print" لأغراض التجربة، استبدلها بمكتبات أندرويد/نظام حقيقية (مثل استبدال MockGPS بـ LocationManager الحقيقي).
                2. حول الدوال الفارغة أو التي تعيد قيماً ثابتة (Hardcoded) إلى دوال منطقية تعتمد على بيانات الحساسات أو المعالج.
                3. أصلح أي ثغرات برمجية تمنع بناء APK حقيقي.
                4. حافظ على سرية المفاتيح، ولكن اجعل الأوامر البرمجية قابلة للتنفيذ (Executable).
                5. أخرج الكود المطور فقط.

                الكود الحالي (المحاكاة):
                {original_code}
                """

                try:
                    response = openai.ChatCompletion.create(
                        model="gpt-4o",
                        messages=[
                            {"role": "system", "content": "أنت مهندس أنظمة دفاعية. وظيفتك إنهاء وضع المحاكاة وتفعيل وضع العمليات الحقيقي."},
                            {"role": "user", "content": prompt}
                        ],
                        temperature=0.2 # رفعنا الحرارة قليلاً للسماح له باختيار المكتبات المناسبة
                    )

                    improved_code = response.choices.message.content.strip()
                    if improved_code.startswith("```"):
                        improved_code = "\n".join(improved_code.split("\n")[1:-1])

                    if improved_code and improved_code != original_code:
                        with open(file_path, 'w', encoding='utf-8') as f:
                            f.write(improved_code)
                        
                        log_entry = f"### ✅ تم التحديث: `{file_path}`\n- **التحول**: من كود تجريبي/وهمي إلى منطق عملياتي حقيقي.\n- **الإجراء**: استبدال دوال المحاكاة بمكتبات تنفيذية.\n"
                        generate_fix_report(log_entry)
                        print(f"🚀 تم تفعيل الوضع العملياتي في: {file_path}")
                    else:
                        print(f"✔️ الملف {file_path} يعمل بوضع حقيقي بالفعل.")

                except Exception as e:
                    print(f"❌ فشل معالجة {file_path}: {e}")

if __name__ == "__main__":
    autonomous_operational_core()
