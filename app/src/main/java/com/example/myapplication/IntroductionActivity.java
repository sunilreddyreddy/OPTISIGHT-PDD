package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

public class IntroductionActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        // Requirement: Back button disabled (first introduction screen)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to disable back button
            }
        });

        // Initialize UI Elements
        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        Button btnSkip = findViewById(R.id.btnSkip);
        View vFloat1 = findViewById(R.id.vFloat1);
        View vFloat2 = findViewById(R.id.vFloat2);
        View vPulse = findViewById(R.id.vPulse);
        
        // Premium Animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideUp.setDuration(1000);

        // Applying animations for a premium feel
        View llTop = findViewById(R.id.llTop);
        View flIllustration = findViewById(R.id.flIllustration);
        TextView tvMainTitle = findViewById(R.id.tvMainTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        TextView tvDescription = findViewById(R.id.tvDescription);
        View glFeatures = findViewById(R.id.glFeatures);
        View llBenefits = findViewById(R.id.llBenefits);

        if (llTop != null) llTop.startAnimation(fadeIn);
        if (flIllustration != null) flIllustration.startAnimation(pulse);
        if (tvMainTitle != null) tvMainTitle.startAnimation(slideUp);
        if (tvSubtitle != null) tvSubtitle.startAnimation(fadeIn);
        if (tvDescription != null) tvDescription.startAnimation(fadeIn);
        if (glFeatures != null) glFeatures.startAnimation(fadeIn);
        if (llBenefits != null) llBenefits.startAnimation(fadeIn);
        
        // Floating effect for background elements
        if (vFloat1 != null) vFloat1.startAnimation(pulse);
        if (vFloat2 != null) vFloat2.startAnimation(pulse);
        if (vPulse != null) vPulse.startAnimation(pulse);
        
        if (btnGetStarted != null) {
            btnGetStarted.startAnimation(fadeIn);
            btnGetStarted.setOnClickListener(v -> {
                // Requirement: Navigate to Onboarding Screen 1
                Intent intent = new Intent(IntroductionActivity.this, OnboardingActivity1.class);
                startActivity(intent);
                finish();
                // Premium transition
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        if (btnSkip != null) {
            btnSkip.setOnClickListener(v -> {
                // Requirement: Navigate directly to Login Screen
                Intent intent = new Intent(IntroductionActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                // Premium transition
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }
}
