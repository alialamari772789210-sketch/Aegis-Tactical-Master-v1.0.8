import openai
import os

openai.api_key = os.getenv("OPENAI_API_KEY")

def autonomous_refactor():
    # المجلدات والملفات التي يجب عدم المساس بها
    exclude_dirs = {'.git', '.github', 'workspace', 'gradle', '.idea', 'build', 'node_modules'}
    supported_extensions = ('.java', '.kt', '.xml', '.gradle', '.py')

    print("--- 🔍 بدء فحص المستودع لتصحيح الأخطاء ---")

    for root, dirs, files in os.walk("."):
        dirs[:] = [d for d in dirs if d not in exclude_dirs]
        
        for file in files:
            if file.endswith(supported_extensions):
                file_path = os.path.join(root, file)
                
                with open(file_path, 'r', errors='ignore') as f:
                    original_code = f.read()

                # طلب التصحيح من الذكاء الاصطناعي
                prompt = f"""
                أنت خبير تصحيح أخطاء (Debugger). قم بمراجعة الكود التالي للملف: {file_path}
                
                المطلوب:
                1. أصلح أي أخطاء منطقية أو برمجية (Syntax/Logic Errors).
                2. حافظ على البنية الأساسية للكود (Architecture) ولا تغير أسماء الدوال أو المتغيرات الأساسية.
                3. إذا كان الكود سليماً، أعده كما هو دون تغيير.
                4. أخرج الكود فقط بدون أي شرح أو كلام جانبي.

                الكود الأصلي:
                {original_code}
                """

                try:
                    response = openai.ChatCompletion.create(
                        model="gpt-4o",
                        messages=[
                            {"role": "system", "content": "أنت محرك تصحيح أخطاء دقيق جداً."},
                            {"role": "user", "content": prompt}
                        ],
                        temperature=0 # لضمان الدقة وعدم الابتكار الزائد
                    )

                    improved_code = response.choices.message.content.strip()

                    # إزالة علامات الاقتباس البرمجية إذا وجدت في الرد
                    if improved_code.startswith("```"):
                        improved_code = "\n".join(improved_code.split("\n")[1:-1])

                    # 3. تحديث الملف فقط إذا كان هناك تغيير حقيقي
                    if improved_code and improved_code != original_code:
                        with open(file_path, 'w', encoding='utf-8') as f:
                            f.write(improved_code)
                        print(f"✅ تم تصحيح وتحديث: {file_path}")
                    else:
                        print(f"✔️ ملف سليم: {file_path}")

                except Exception as e:
                    print(f"❌ خطأ أثناء معالجة {file_path}: {e}")

if __name__ == "__main__":
    autonomous_refactor()
