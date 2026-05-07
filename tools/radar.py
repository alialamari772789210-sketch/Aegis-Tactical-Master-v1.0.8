from scapy.all import *

def tactical_radar_scan(interface="wlan0mon"):
    """رصد حقيقي للمسيرات عبر تحليل بصمات MAC OUI"""
    drone_vendors = ["60:60:1f", "00:26:7e", "90:0e:b3"] # DJI, Parrot, Autel
    detected = []

    def packet_handler(pkt):
        if pkt.haslayer(Dot11Beacon):
            addr = pkt.addr2.lower()
            if any(addr.startswith(v) for v in drone_vendors):
                detected.append(addr)

    # مسح لمدة 5 ثوانٍ
    sniff(iface=interface, prn=packet_handler, timeout=5)
    found = list(set(detected))
    return f"Radar Report: Detected {len(found)} targets: {found}"
