import os
import sys
from dotenv import load_dotenv

# تأمين مسارات النظام لرؤية المجلدات الفرعية
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from langchain_openai import ChatOpenAI
from langchain.agents import create_structured_chat_agent, AgentExecutor, Tool
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder

# استيراد الأدوات المدمجة من الملف الجديد
try:
    from tools.radar import tactical_radar_scan, real_neutralize
except ImportError as e:
    print(f"⚠️ فشل الاستيراد التكتيكي: {e}")
    # تعريف دوال طوارئ لمنع انهيار النظام أثناء البناء
    def tactical_radar_scan(*args, **kwargs): return "الرادار: في وضع الاستعداد"
    def real_neutralize(*args, **kwargs): return "نظام التحييد: جاهز"

load_dotenv()

# إعداد النواة
llm = ChatOpenAI(model="gpt-4o", temperature=0)

# تعريف وظيفة حفظ التقارير محلياً (بدلاً من الإيميل)
def create_tactical_report(content):
    report_path = "workspace/DETECTION_REPORT.md"
    os.makedirs("workspace", exist_ok=True)
    with open(report_path, "a", encoding="utf-8") as f:
        f.write(f"\n## تقرير عملياتي حديث\n{content}\n---\n")
    return f"تم توثيق العملية بنجاح في {report_path}"

# ربط الأدوات بالعميل
tools = [
    Tool(name="Tactical_Radar", func=tactical_radar_scan, description="رصد المسيرات وتحليل بصمات Dot11"),
    Tool(name="Signal_Jammer", func=real_neutralize, description="تحييد المسيرة عبر قطع الاتصال القسري Deauth"),
    Tool(name="Report_Generator", func=create_tactical_report, description="توثيق النتائج في تقرير المجلد العملياتي")
]

# بناء محرك القرار
prompt = ChatPromptTemplate.from_messages([
    ("system", "أنت نظام Aegis. افحص الرادار أولاً، وإذا وجد أهدافاً استخدم التحييد، ثم وثق كل شيء."),
    MessagesPlaceholder(variable_name="chat_history", optional=True),
    ("human", "{input}\n\n{agent_scratchpad}"),
])

agent = create_structured_chat_agent(llm, tools, prompt)
agent_executor = AgentExecutor(agent=agent, tools=tools, verbose=True, handle_parsing_errors=True)
