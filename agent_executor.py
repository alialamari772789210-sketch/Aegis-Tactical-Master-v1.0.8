import os
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain.agents import create_structured_chat_agent, AgentExecutor, Tool
from langchain import hub
from tools.radar import tactical_radar_scan
from tools.jammer import signal_jammer_activate, stealth_mode_toggle

load_dotenv()

# إعداد النواة
llm = ChatOpenAI(model="gpt-4o", temperature=0)

# تعريف الأدوات العملياتية
tools = [
    Tool(
        name="Tactical_Radar", 
        func=tactical_radar_scan, 
        description="رصد المسيرات الحقيقي وتحليل إشارات الرادار"
    ),
    Tool(
        name="Signal_Jammer", 
        func=signal_jammer_activate, 
        description="تحييد واختراق إشارات المسيرة المعادية"
    ),
    Tool(
        name="Stealth_System", 
        func=stealth_mode_toggle, 
        description="تفعيل نظام التخفي الشبحي للمنظومة"
    )
]

# المحرك التنفيذي الحديث
prompt = hub.pull("hwchase17/structured-chat-agent")
agent = create_structured_chat_agent(llm, tools, prompt)

agent_executor = AgentExecutor(
    agent=agent, 
    tools=tools, 
    verbose=True, 
    handle_parsing_errors=True
)
