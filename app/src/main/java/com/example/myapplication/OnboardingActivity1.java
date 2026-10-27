package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity1 extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding1);

        // Initialize UI Elements
        Button btnNext = findViewById(R.id.btnNext);
        TextView tvSkipTop = findViewById(R.id.tvSkipTop);
        
        // Premium Animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        
        findViewById(R.id.ivOnboarding1).startAnimation(pulse);
        findViewById(R.id.tvTitle).startAnimation(fadeIn);
        findViewById(R.id.tvDesc).startAnimation(fadeIn);

        btnNext.setOnClickListener(v -> {
            // Requirement: Navigate to Onboarding Screen 2
            Intent intent = new Intent(OnboardingActivity1.this, OnboardingActivity2.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Skip option navigates directly to Login Screen
        tvSkipTop.setOnClickListener(v -> navigateToLogin());
    }

    private void navigateToLogin() {
        Intent intent = new Intent(OnboardingActivity1.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
