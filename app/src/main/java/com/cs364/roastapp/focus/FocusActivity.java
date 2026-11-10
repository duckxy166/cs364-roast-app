package com.cs364.roastapp.focus;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cs364.roastapp.R;

public class FocusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus_activity);

        Button backBtn = findViewById(R.id.btn_focus_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }
}
