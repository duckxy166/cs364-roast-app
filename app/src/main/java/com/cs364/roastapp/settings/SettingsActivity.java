package com.cs364.roastapp.settings;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cs364.roastapp.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Button backBtn = findViewById(R.id.btn_settings_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }
}
