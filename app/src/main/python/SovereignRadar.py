"""
=============================================================================
SovereignRadio.py – Unified Sovereign RF Layer (The Absolute Version)
=============================================================================
النسخة السيادية الموحدة: AEGIS v7.2.6
المهندس المعماري: العقيد علي العماري
النظام: مراقبة وتحليل الطيف الترددي (RF Monitoring Engine)
-----------------------------------------------------------------------------
الإصلاحات البرمجية المثبتة:
- إصلاح Regex parsing لضمان دقة استخراج الـ SSID والـ RSSI.
- منع انهيار subprocess عبر استخدام timeouts و capture_output.
- دعم (Termux + Android + Linux + GitHub Actions) في ملف واحد.
- إدارة دورة حياة الخيوط (Thread Lifecycle) لمنع Deadlocks.
- نظام Callbacks لربط البيانات الحية بواجهات الـ HUD.
- منع تجمد الخدمة الخلفية ومعالجة أخطاء الـ JSON.
=============================================================================
"""

import os
import re
import json
import time
import threading
import subprocess

# =============================================================================
# كاشفات البيئة (Environment Detection)
# =============================================================================

def _is_termux():
    return "com.termux" in os.environ.get("PREFIX", "")

def _is_github_actions():
    return os.environ.get("GITHUB_ACTIONS", "false").lower() == "true"

def _is_android():
    return "ANDROID_ROOT" in os.environ or os.path.exists("/system/build.prop")

# =============================================================================
# المحرك الراديوي السيادي (Sovereign RF Engine)
# =============================================================================

class SovereignRadio:
    def __init__(self, frequency="144.0M"):
        self.frequency = frequency
        self.running = False
        self.last_scan = []
        self.worker_thread = None
        self.sdr_process = None
        self.event_callback = None

        # كشف البيئة وتحديد المسارات
        self.env_termux = _is_termux()
        self.env_android = _is_android()
        self.env_github = _is_github_actions()
        self.sdr_available = self._detect_sdr()

        self._print_sovereign_status()

    def _print_sovereign_status(self):
        env_name = ("Termux" if self.env_termux else
                    "GitHub Actions" if self.env_github else
                    "Android/Linux Standard")
        
        print(f"\n[AEGIS] تم تشخيص البيئة: {env_name}")
        print(f"[AEGIS] التردد العملياتي المقفل: {self.frequency}")
        print(f"[AEGIS] عتاد SDR: {'نشط (Active)' if self.sdr_available else 'غير مكتشف (الرصد السلبي مفعّل)'}")
        
        if self.env_github:
            print("[AEGIS] تنبيه أمني: وضع CI/CD نشط - يتم استخدام محاكاة للبيانات.")

    # -------------------------------------------------
    # تسجيل المستمعين (Callback System)
    # -------------------------------------------------
    def register_callback(self, callback):
        """يسمح لـ Kotlin أو JavaScript باستقبال مصفوفة الأهداف فوراً."""
        self.event_callback = callback

    # -------------------------------------------------
    # كشف العتاد (SDR Hardware Detection)
    # -------------------------------------------------
    def _detect_sdr(self):
        if self.env_github: return False

        detection_commands = []
        if self.env_termux:
            detection_commands.append(["termux-usb", "-l"])
        
        detection_commands.append(["sh", "-c", "lsusb"])
        detection_commands.append(["sh", "-c", "cat /sys/kernel/debug/usb/devices"])

        signatures = ["rtl", "hackrf", "rtl2838", "realtek"]

        for cmd in detection_commands:
            try:
                # إصلاح: منع الانهيار باستخدام timeout و capture
                result = subprocess.run(cmd, capture_output=True, text=True, timeout=5)
                output = (result.stdout.lower() + result.stderr.lower())
                if any(sig in output for sig in signatures):
                    return True
            except: continue
        return False

    # -------------------------------------------------
    # المسح التكتيكي (WiFi Scan - Multi-Environment)
    # -------------------------------------------------
    def passive_wifi_scan(self):
        if self.env_github:
            return self._mock_scan()

        scan_results = []

        # 1. بيئة Termux (إصلاح JSON parsing)
        if self.env_termux:
            try:
                result = subprocess.run(["termux-wifi-scaninfo"], capture_output=True, text=True, timeout=10)
                if result.returncode == 0:
                    data = json.loads(result.stdout)
                    for item in data[:20]:
                        scan_results.append({
                            "ssid": item.get("ssid", "UNKNOWN"),
                            "bssid": item.get("bssid", "00:00:00:00:00:00"),
                            "signal": item.get("level", -100),
                            "frequency": item.get("frequency", 0)
                        })
                    self.last_scan = scan_results
                    return scan_results
            except Exception as e:
                print(f"[AEGIS] Termux Scan Error: {e}")

        # 2. بيئة Android cmd wifi (إصلاح Regex Parsing الحقيقي)
        if self.env_android and not self.env_termux:
            try:
                result = subprocess.run(["cmd", "wifi", "list-scan-results"], capture_output=True, text=True, timeout=10)
                lines = result.stdout.splitlines()
                # Regex متطور لمعالجة الأسماء المعقدة والمسافات
                regex = re.compile(r"(.+?)\s+([0-9A-Fa-f:]{17})\s+(-?\d+)\s+(\d+)")
                
                for line in lines:
                    match = regex.search(line)
                    if match:
                        scan_results.append({
                            "ssid": match.group(1).strip(),
                            "bssid": match.group(2),
                            "signal": int(match.group(3)),
                            "frequency": int(match.group(4))
                        })
                self.last_scan = scan_results
                return scan_results
            except Exception as e:
                print(f"[AEGIS] Android Native Scan Error: {e}")

        # 3. بيئة Linux iw (إصلاح Deadlock ومنع التجميد)
        try:
            subprocess.run(["iw", "dev", "wlan0", "scan"], capture_output=True, timeout=5)
            result = subprocess.run(["iw", "dev", "wlan0", "scan", "dump"], capture_output=True, text=True, timeout=10)
            current = {}
            for line in result.stdout.splitlines():
                line = line.strip()
                if line.startswith("BSS "):
                    if current: scan_results.append(current)
                    current = {"ssid": "HIDDEN", "bssid": "", "signal": -100, "frequency": 0}
                    try: current["bssid"] = line.split()[1].split("(")[0]
                    except: pass
                elif "SSID:" in line:
                    current["ssid"] = line.split("SSID:")[-1].strip()
                elif "signal:" in line:
                    try: current["signal"] = float(line.split("signal:")[-1].strip().split()[0])
                    except: pass
                elif "freq:" in line:
                    try: current["frequency"] = int(line.split("freq:")[-1].strip())
                    except: pass
            if current: scan_results.append(current)
        except Exception as e:
            print(f"[AEGIS] Linux iw Error: {e}")

        self.last_scan = scan_results[:20]
        return self.last_scan

    def _mock_scan(self):
        """بيانات محاكاة لضمان استمرار الاختبار في GitHub Actions."""
        return [
            {"ssid": "SIMULATED_UAV_01", "bssid": "00:DE:AD:BE:EF:01", "signal": -45, "frequency": 2412},
            {"ssid": "SIMULATED_UAV_02", "bssid": "00:DE:AD:BE:EF:02", "signal": -62, "frequency": 5800}
        ]

    # -------------------------------------------------
    # الاستقبال النشط (SDR Stream Lifecycle)
    # -------------------------------------------------
    def listen_sdr_stream(self):
        if not self.sdr_available: return
        try:
            # إصلاح: الاحتفاظ بمرجع العملية لإغلاقها لاحقاً
            cmd = ["rtl_fm", "-f", self.frequency, "-s", "24000", "-g", "35"]
            self.sdr_process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            print(f"[AEGIS] SDR الاستقبال نشط على تردد: {self.frequency}")
        except Exception as e:
            print(f"[AEGIS] SDR Launch Error: {e}")

    # -------------------------------------------------
    # إدارة الخدمة الخلفية (Thread Management)
    # -------------------------------------------------
    def start(self):
        if self.running: return
        self.running = True
        
        if self.sdr_available:
            self.listen_sdr_stream()
        
        # حلقة الرصد السلبي (إصلاح Thread Life)
        if not self.worker_thread or not self.worker_thread.is_alive():
            self.worker_thread = threading.Thread(target=self._passive_loop, daemon=True)
            self.worker_thread.start()

    def _passive_loop(self):
        while self.running:
            try:
                data = self.passive_wifi_scan()
                
                # استدعاء الـ Callback إذا كان مسجلاً (إصلاح Inter-op)
                if self.event_callback:
                    try: self.event_callback(data)
                    except: pass
                
                # إصلاح: التحكم في استهلاك المعالج
                time.sleep(5)
            except Exception as e:
                # منع انهيار الحلقة عند الخطأ
                time.sleep(3)

    def stop(self):
        """إيقاف السيادة وتأمين الذاكرة."""
        self.running = False
        if self.sdr_process:
            try: 
                self.sdr_process.terminate()
                self.sdr_process.wait(timeout=2)
            except: pass
        print("[AEGIS] تم إغلاق القناة الراديوية وتأمين النظام.")

# =============================================================================
# بوابة التفعيل (Sovereign Entry Point)
# =============================================================================

def activate_radio_matrix(freq="144.0M"):
    """
    دالة الاستدعاء المباشر.
    تُستخدم للربط مع Kotlin أو للتشغيل الميداني الفوري.
    """
    print(f"\n[AEGIS] سيادة العقيد علي، تم فتح القناة الراديوية السيادية على {freq}")
    radio = SovereignRadio(freq)
    radio.start()
    return radio

# =============================================================================
# وضع التشغيل الميداني (CLI Execution)
# =============================================================================

if __name__ == "__main__":
    # تفعيل المصفوفة
    aegis_radio = activate_radio_matrix()
    try:
        while True:
            # الحفاظ على السكربت نشطاً لرؤية مخرجات الحلقة
            time.sleep(1)
    except KeyboardInterrupt:
        aegis_radio.stop()
