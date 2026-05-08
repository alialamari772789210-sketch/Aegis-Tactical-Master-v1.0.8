
	package com.jamesfirstok.aegis;

	import android.content.Intent;
	import android.os.Bundle;
	import android.widget.Button;

	import androidx.appcompat.app.AppCompatActivity;

	public class HomeActivity extends AppCompatActivity {

		private Button btnHome;
		private Button btnRadar;
		private Button btnAbout;
		private Button btnSettings;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_home);

			btnHome = findViewById(R.id.btn_home);
			btnRadar = findViewById(R.id.btn_radar);
			btnAbout = findViewById(R.id.btn_about);
			btnSettings = findViewById(R.id.btn_settings);

			openHome();

			btnHome.setOnClickListener(v -> openHome());
			btnRadar.setOnClickListener(v -> openRadar());
			btnAbout.setOnClickListener(v -> openAbout());
			btnSettings.setOnClickListener(v -> openSettings());
		}

		private void openHome() {
			Intent i = new Intent(this, HomeActivity.class);
			startActivity(i);
			finish();
			overridePendingTransition(0, 0);
		}

		private void openRadar() {
			Intent i = new Intent(this, RadarActivity.class);
			startActivity(i);
			finish();
		}

		private void openAbout() {
			Intent i = new Intent(this, AboutActivity.class);
			startActivity(i);
			finish();
		}

		private void openSettings() {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			finish();
		}
	}
