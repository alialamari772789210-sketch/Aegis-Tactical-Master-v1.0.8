import os
import sys
from dotenv import load_dotenv

# تأمين المسار لضمان رؤية مجلد tools داخل بيئة GitHub
sys.path.append(os.getcwd())

from langchain_openai import ChatOpenAI
from langchain.agents import create_structured_chat_agent, AgentExecutor, Tool
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder

# استيراد الأدوات بناءً على أسماء الملفات في صورتك
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
llm = ChatOpenAI(model="gpt-4o", temperature=0)

# أداة التوثيق في المجلد العملياتي
def create_tactical_report(content):
    os.makedirs("workspace", exist_ok=True)
    report_path = "workspace/DETECTION_REPORT.md"
    with open(report_path, "a", encoding="utf-8") as f:
        f.write(f"\n### تحديث ميداني سيادي\n{content}\n---\n")
    return f"تم الحفظ في: {report_path}"

# تعريف الترسانة الكاملة
tools = [
    Tool(name="Tactical_Radar", func=tactical_radar_scan, description="رصد المسيرات وتحليل إشارات Dot11"),
    Tool(name="Signal_Jammer", func=real_neutralize, description="تحييد الهدف عبر قطع الاتصال القسري Deauth"),
    Tool(name="RF_Acquisition", func=acquire_rf_signature, description="الاستحواذ على البصمة الترددية للمسيرة"),
    Tool(name="Report_Generator", func=create_tactical_report, description="توثيق العمليات في تقرير المجلد العملياتي")
]

# القالب النهائي (يحتوي على المتغيرات الإلزامية لمنع خطأ ValueError)
prompt = ChatPromptTemplate.from_messages([
    ("system", """أنت نظام Aegis للسيادة الجوية. 
استخدم الأدوات المتاحة للتعامل مع التهديدات:
{tools}

يجب أن يكون ردك بتنسيق JSON يحتوي على 'action' و 'action_input'.
أسماء الأدوات المتاحة لك: {tool_names}"""),
    MessagesPlaceholder(variable_name="chat_history", optional=True),
    ("human", "{input}\n\n{agent_scratchpad}"),
])

# إنشاء المحرك التنفيذي
agent = create_structured_chat_agent(llm, tools, prompt)
agent_executor = AgentExecutor(agent=agent, tools=tools, verbose=True, handle_parsing_errors=True)
