import asyncio
import json
import websockets
import numpy as np
from datetime import datetime
from dataclasses import dataclass, asdict

# --- 1. محرك تحويل الإحداثيات (Tactical Coordinate Engine) ---
class CoordConverter:
    """تحويل الإحداثيات الجغرافية إلى أمتار (Local Tangent Plane)"""
    def __init__(self, ref_lat, ref_lon):
        self.ref_lat = np.radians(ref_lat)
        self.ref_lon = np.radians(ref_lon)
        self.R = 6378137.0 # نصف قطر الأرض بالأمتار

    def to_local_ned(self, lat, lon):
        lat, lon = np.radians(lat), np.radians(lon)
        dlat = lat - self.ref_lat
        dlon = lon - self.ref_lon
        x = dlat * self.ref_lon
        y = dlon * self.R * np.cos(self.ref_lat)
        return x, y # العودة بالأمتار (الشمال، الشرق)

# --- 2. إدارة دورة حياة الأهداف (Track Lifecycle Manager) ---
@dataclass
class TacticalTrack:
    track_id: str
    pos_ned: tuple
    velocity: float
    confidence: float
    last_seen: float
    status: str = "TENTATIVE" # TENTATIVE, ACTIVE, LOST

class MultiTargetTracker:
    def __init__(self):
        self.tracks = {}
        self.timeout = 5.0 # ثوانٍ قبل اعتبار الهدف مفقوداً

    def update_track(self, track_id, pos, conf):
        now = datetime.now().timestamp()
        if track_id not in self.tracks:
            self.tracks[track_id] = TacticalTrack(track_id, pos, 0, conf, now)
        else:
            t = self.tracks[track_id]
            # حساب السرعة اللحظية (Distance / Time)
            dist = np.linalg.norm(np.array(pos) - np.array(t.pos_ned))
            dt = now - t.last_seen
            t.velocity = dist / dt if dt > 0 else 0
            t.pos_ned = pos
            t.last_seen = now
            t.status = "ACTIVE" if dt < self.timeout else "LOST"

    def purge_old_tracks(self):
        now = datetime.now().timestamp()
        self.tracks = {tid: t for tid, t in self.tracks.items() if now - t.last_seen < self.timeout * 2}

# --- 3. الجسر العملياتي (WebSocket & Android Bridge) ---
class AegisLiveRuntime:
    def __init__(self):
        self.tracker = MultiTargetTracker()
        self.converter = CoordConverter(13.7022, 44.7311) # مرجع الضالع كمثال
        self.clients = set()

    async def hud_broadcast_server(self, websocket, path):
        """بث البيانات لواجهة الـ HUD (Dashboard)"""
        self.clients.add(websocket)
        try:
            async for _ in websocket: await asyncio.sleep(10)
        finally:
            self.clients.remove(websocket)

    async def android_ingestion_handler(self, reader, writer):
        """استقبال البيانات من Kotlin Sensor Layer"""
        while True:
            data = await reader.read(2048)
            if not data: break
            try:
                payload = json.loads(data.decode())
                # 1. تحويل الموقع للـ Local Frame
                x, y = self.converter.to_local_ned(payload['gps']['lat'], payload['gps']['lon'])
                
                # 2. تحديث المسارات (Multi-Target Logic)
                target_id = payload.get('target_mac', 'UNKNOWN_ID')
                self.tracker.update_track(target_id, (x, y), payload.get('conf', 0.5))
                self.tracker.purge_old_tracks()
                
                # 3. البث الفوري للـ HUD عبر WebSocket
                await self.broadcast_to_hud()
            except Exception as e:
                print(f"B-ERR: {e}")

    async def broadcast_to_hud(self):
        if not self.clients: return
        data = json.dumps({"tracks": [asdict(t) for t in self.tracker.tracks.values()]})
        await asyncio.gather(*[client.send(data) for client in self.clients])

# --- 4. تشغيل المنظومة ---
async def main():
    runtime = AegisLiveRuntime()
    # تشغيل خادمين: واحد للأندرويد وواحد للـ HUD
    server_android = await asyncio.start_server(runtime.android_ingestion_handler, '0.0.0.0', 8888)
    server_hud = websockets.serve(runtime.hud_broadcast_server, "0.0.0.0", 9999)
    
    print("🚀 [V15_RUNTIME] DUAL BRIDGE ACTIVE | ANDROID: 8888 | HUD: 9999")
    await asyncio.gather(server_android.serve_forever(), server_hud)

if __name__ == "__main__":
    asyncio.run(main())
