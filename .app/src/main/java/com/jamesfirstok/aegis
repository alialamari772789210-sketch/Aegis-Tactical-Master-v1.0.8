package com.jamesfirstok.aegis;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigManager {
    private static final String PREFS_NAME = "AegisConfig";
    private static final String SATELLITE_LINK_KEY = "sat_link_active";
    private static final String RECON_FREQUENCY = "recon_freq_khz";

    private SharedPreferences prefs;

    public ConfigManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // تفعيل الربط مع الأقمار الصناعية
    public void setSatelliteLink(boolean active) {
        prefs.edit().putBoolean(SATELLITE_LINK_KEY, active).apply();
    }

    // تحديد تردد الرصد (بالكيلو هرتز)
    public void updateFrequency(int freq) {
        prefs.edit().putInt(RECON_FREQUENCY, freq).apply();
    }

    public int getStoredFrequency() {
        return prefs.getInt(RECON_FREQUENCY, 433000); // التردد الافتراضي للرصد
    }
}
