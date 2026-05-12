package com.jamesfirstok.aegis;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private Button btnHome;
    private Button btnRadar;
    private Button btnAbout;
    private Button btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        btnHome = findViewById(R.id.btn_home);
        btnRadar = findViewById(R.id.btn_radar);
        btnAbout = findViewById(R.id.btn_about);
        btnSettings = findViewById(R.id.btn_settings);

        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        btnRadar.setOnClickListener(v -> {
            startActivity(new Intent(this, RadarActivity.class));
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
    }
}
