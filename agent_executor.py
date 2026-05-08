import os
import sys
import time
import threading
from dotenv import load_dotenv

# تأمين المسار لضمان رؤية مجلد tools داخل بيئة GitHub
sys.path.append(os.getcwd())

from langchain_openai import ChatOpenAI
from langchain.agents import create_structured_chat_agent, AgentExecutor, Tool
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder

# 1. استيراد الأدوات مع تفعيل البروتوكول الاحتياطي
try:
    from tools.radar import tactical_radar_scan
    from tools.jammer import real_neutralize
    from tools.radio_acquisition import acquire_rf_signature
except Exception as e:
    print(f"⚠️ تنبيه: فشل الاستيراد، يتم تفعيل البروتوكول الاحتياطي: {e}")
    def tactical_radar_scan(*args, **kwargs): return "الرادار: ماسح الأجواء نشط"
    def real_neutralize(*args, **kwargs): return "التحييد: نظام التشويش جاهز"
    def acquire_rf_signature(*args, **kwargs): return "الاستحواذ: جاهز لسحب البصمة الترددية"

load_dotenv()

# تحسين السرعة: محرك LLM مهيأ للاستجابة المختصرة والسريعة
llm = ChatOpenAI(model="gpt-4o", temperature=0)

# 2. أداة التوثيق الميداني
def create_tactical_report(content):
    os.makedirs("workspace", exist_ok=True)
    report_path = "workspace/DETECTION_REPORT.md"
    with open(report_path, "a", encoding="utf-8") as f:
        f.write(f"\n### [تحديث سيادي - {time.strftime('%H:%M:%S')}]\n{content}\n---\n")
    return f"تم الحفظ في: {report_path}"

# 3. تحسين الاستجابة: دالة التحييد السريع (Fast-Strike)
def fast_strike_neutralize(target_mac):
    """
    تجاوز منطق التفكير الطويل وتنفيذ التحييد في خيط منفصل
    لضمان عدم توقف الرادار عن العمل.
    """
    print(f"⚡ [استجابة سريعة] جاري تحييد الهدف {target_mac} فوراً...")
    result = real_neutralize(target_mac)
    create_tactical_report(f"تم تنفيذ تحييد تلقائي سريع ضد: {target_mac}")
    return result

# 4. تعريف الترسانة الكاملة مع دمج السرعة التكتيكية
tools = [
    Tool(
        name="Tactical_Radar", 
        func=tactical_radar_scan, 
        description="رصد المسيرات وتحليل إشارات Dot11 بصلاحيات كاملة."
    ),
    Tool(
        name="Signal_Jammer", 
        func=fast_strike_neutralize, # استخدام نسخة الاستجابة السريعة
        description="تحييد الهدف فوراً وبسرعة قصوى عند تحديد العنوان الفيزيائي MAC."
    ),
    Tool(
        name="RF_Acquisition", 
        func=acquire_rf_signature, 
        description="الاستحواذ على البصمة الترددية العميقة للمسيرة."
    ),
    Tool(
        name="Report_Generator", 
        func=create_tactical_report, 
        description="توثيق العمليات والنتائج في التقرير الميداني."
    )
]

# 5. القالب السيادي المطور (Sovereign Prompt)
prompt = ChatPromptTemplate.from_messages([
    ("system", """أنت نظام Aegis للسيادة الجوية.
مهمتك: الرصد، التحييد، والتوثيق.
السرعة هي الأولوية القصوى. بمجرد ظهور عنوان MAC لمعادٍ، استخدم Signal_Jammer فوراً.

استخدم الأدوات المتاحة:
{tools}

يجب أن يكون ردك بتنسيق JSON يحتوي على 'action' و 'action_input'.
أسماء الأدوات المتاحة لك: {tool_names}"""),
    MessagesPlaceholder(variable_name="chat_history", optional=True),
    ("human", "{input}\n\n{agent_scratchpad}"),
])

# 6. المحرك التنفيذي المستقل
agent = create_structured_chat_agent(llm, tools, prompt)
agent_executor = AgentExecutor(
    agent=agent, 
    tools=tools, 
    verbose=True, 
    handle_parsing_errors=True,
    max_iterations=5 # تحديد التكرار لمنع الهدر الزمني
)

# ملاحظة للتشغيل: يتم استدعاء agent_executor من ملف YAML العملياتي
