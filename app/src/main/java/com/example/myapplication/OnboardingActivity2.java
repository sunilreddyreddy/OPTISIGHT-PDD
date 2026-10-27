package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity2 extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding2);

        Button btnNext = findViewById(R.id.btnNext);
        Button btnBack = findViewById(R.id.btnBack);
        
        btnNext.setOnClickListener(v -> {
            // Requirement: Navigate to Login (MainActivity) as Step 3 is not yet requested
            Intent intent = new Intent(OnboardingActivity2.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
        
        findViewById(R.id.tvSkipTop).setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity2.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
