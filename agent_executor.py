import os
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain.agents import create_structured_chat_agent, AgentExecutor, Tool
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder

load_dotenv()

# إعداد النواة
llm = ChatOpenAI(model="gpt-4o", temperature=0)

# دالة سيادية لإنشاء ملف التقرير داخل المستودع
def create_tactical_report(content):
    report_path = "workspace/DETECTION_REPORT.md"
    os.makedirs("workspace", exist_ok=True)
    with open(report_path, "a", encoding="utf-8") as f:
        f.write(f"\n## تقرير رصد عملياتي\n{content}\n---\n")
    return f"تم حفظ التقرير بنجاح في المسار: {report_path}"

# تعريف الأدوات المحدثة
tools = [
    Tool(name="Tactical_Radar", func=tactical_radar_scan, description="رصد المسيرات الحقيقي"),
    Tool(name="Signal_Jammer", func=signal_jammer_activate, description="تحييد واختراق المسيرة"),
    Tool(name="Report_Generator", func=create_tactical_report, description="إنشاء وحفظ تقارير الرصد في المستودع")
]

# بناء القالب المستقل (Self-Sovereign Prompt)
prompt = ChatPromptTemplate.from_messages([
    ("system", "أنت نظام Aegis الميداني. مهمتك الرصد، التشويش، وتوثيق العمليات في ملفات المستودع."),
    MessagesPlaceholder(variable_name="chat_history", optional=True),
    ("human", "{input}\n\n{agent_scratchpad}"),
])

agent = create_structured_chat_agent(llm, tools, prompt)
agent_executor = AgentExecutor(agent=agent, tools=tools, verbose=True, handle_parsing_errors=True)
