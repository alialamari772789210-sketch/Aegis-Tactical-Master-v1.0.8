package com.jamesfirstok.aegis;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String ACCESS_CODE = "Ali2A2026";
    private TextToSpeech tts;
    private EditText accessCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accessCode = findViewById(R.id.access_code_entry);
        Button btnOk = findViewById(R.id.btn_ok);

        tts = new TextToSpeech(this, this);

        btnOk.setOnClickListener(v -> {
            String entered = accessCode.getText().toString().trim();
            if (ACCESS_CODE.equals(entered)) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(MainActivity.this, "رمز المرور غير صحيح", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale arabic = new Locale("ar");
            int result = tts.setLanguage(arabic);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.speak(
					"العقيد علي العماري مرحبا بك في المنظومة السيادية الشاملة",
					TextToSpeech.QUEUE_FLUSH,
					null,
					"WELCOME_UTTERANCE"
                );
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
