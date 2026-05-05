import zipfile
import openai
import os

openai.api_key = os.getenv("OPENAI_API_KEY")

def ai_autonomous_engine(zip_path, goal):
    # 1. استخراج الكود من الملف المضغوط
    context = ""
    extract_path = "workspace/temp_files"
    with zipfile.ZipFile(zip_path, 'r') as z:
        z.extractall(extract_path)
        for root, dirs, files in os.walk(extract_path):
            for file in files:
                if file.endswith(('.py', '.js', '.html', '.css', '.php', '.c', '.cpp')):
                    p = os.path.join(root, file)
                    with open(p, 'r', errors='ignore') as f:
                        context += f"\n-- FILE: {file} --\n{f.read()}\n"

    # 2. طلب التطوير (نواة التفكير)
    prompt = f"المشروع البرمجي:\n{context}\n\nالهدف المطلوب: {goal}\nأعد كتابة الملفات المطورة بالكامل."
    
    response = openai.ChatCompletion.create(
        model="gpt-4o",
        messages=[{"role": "system", "content": "أنت مبرمج مستقل يقوم بتحديث المشاريع داخل ملفات ZIP."},
                  {"role": "user", "content": prompt}]
    )
    
    # 3. حفظ النتيجة وإعادة الضغط
    output_zip = "workspace/developed_project.zip"
    with open("workspace/ai_output.txt", "w") as f:
        f.write(response.choices[0].message.content)
    
    with zipfile.ZipFile(output_zip, 'w') as new_z:
        new_z.write("workspace/ai_output.txt")
        # يمكن إضافة منطق هنا لتقسيم الكود لملفات حقيقية داخل الـ ZIP
    
    return output_zip

if __name__ == "__main__":
    ai_autonomous_engine("workspace/project.zip", "قم بتحسين الأداء وإضافة تعليقات توضيحية لكل دالة")
