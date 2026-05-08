package com.jamesfirstok.aegis;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class RadarActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private Button btnHome, btnRadar, btnAbout, btnSettings;
    private TextView statusTextView, distanceText, altitudeText, typeText;
    private TextToSpeech tts;
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);

        statusTextView = findViewById(R.id.radar_status_label); // تأكد من مطابقة الـ ID في XML
        distanceText = findViewById(R.id.distance_val);
        altitudeText = findViewById(R.id.altitude_val);
        typeText = findViewById(R.id.type_val);

        btnHome = findViewById(R.id.btn_home);
        btnRadar = findViewById(R.id.btn_radar);
        btnAbout = findViewById(R.id.btn_about);
        btnSettings = findViewById(R.id.btn_settings);

        tts = new TextToSpeech(this, this);

        btnHome.setOnClickListener(v -> {
            isScanning = false;
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        btnAbout.setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
            finish();
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });

        startTacticalScan();
    }

    private void startTacticalScan() {
        new Thread(() -> {
            while (isScanning) {
                try {
                    Thread.sleep(3000);
                    runOnUiThread(() -> {
                        updateRadarData();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateRadarData() {
        // محاكاة معالجة الإشارة التكتيكية الحقيقية
        statusTextView.setText("DANGER: TARGET LOCKED");
        statusTextView.setBackgroundColor(Color.RED);
        distanceText.setText("DISTANCE: 18.5 KM");
        altitudeText.setText("ALTITUDE: 450 M");
        typeText.setText("TYPE: ARMED_DRONE");

        if (tts != null) {
            tts.speak("تحذير تكتيكي، تم قفل الهدف، المسافة ثمانية عشر كيلومتر", TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = new Locale("ar");
        }
    }

    @Override
    protected void onDestroy() {
        isScanning = false;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
