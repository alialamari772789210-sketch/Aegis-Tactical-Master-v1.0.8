import os
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain.agents import initialize_agent, Tool, AgentType
from tools.radar import tactical_radar_scan
from tools.jammer import signal_jammer_activate, stealth_mode_toggle

load_dotenv()

# إعداد النواة (العقل)
llm = ChatOpenAI(model="gpt-4-turbo", temperature=0)

# تعريف الأدوات العملياتية
tools = [
    Tool(name="Tactical_Radar", func=tactical_radar_scan, description="رصد المسيرات الحقيقي"),
    Tool(name="Signal_Jammer", func=signal_jammer_activate, description="تحييد واختراق المسيرة"),
    Tool(name="Stealth_System", func=stealth_mode_toggle, description="تفعيل التخفي الشبحي")
]

# المحرك التنفيذي (الجسم)
agent_executor = initialize_agent(
    tools, llm, agent=AgentType.STRUCTURED_CHAT_ZERO_SHOT_REACT_DESCRIPTION, verbose=True
)
