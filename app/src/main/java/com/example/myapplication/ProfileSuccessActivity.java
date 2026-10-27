package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

/**
 * Premium Success Screen for OptiSight.
 * Displayed after profile updates with animations and summary.
 */
public class ProfileSuccessActivity extends AppCompatActivity {

    private ImageView ivSuccessCheck;
    private TextView tvUserNameSummary, tvUserEmailSummary;
    private MaterialButton btnContinueHome, btnBackToProfile;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_success);

        initViews();
        loadUpdatedData();
        applyAnimations();

        btnContinueHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnBackToProfile.setOnClickListener(v -> finish());
    }

    private void initViews() {
        ivSuccessCheck = findViewById(R.id.ivSuccessCheck);
        tvUserNameSummary = findViewById(R.id.tvUserNameSummary);
        tvUserEmailSummary = findViewById(R.id.tvUserEmailSummary);
        btnContinueHome = findViewById(R.id.btnContinueHome);
        btnBackToProfile = findViewById(R.id.btnBackToProfile);
    }

    private void loadUpdatedData() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String name = prefs.getString("fullName", "User Name");
        String email = prefs.getString("email", "user@example.com");
        String photoUri = prefs.getString("profilePhotoUri", null);

        tvUserNameSummary.setText(name);
        tvUserEmailSummary.setText(email);
        
        if (photoUri != null) {
            ImageView ivProfilePreview = findViewById(R.id.ivProfilePreview);
            ivProfilePreview.setImageURI(android.net.Uri.parse(photoUri));
        }
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);

        findViewById(R.id.layoutContent).startAnimation(fadeIn);
        ivSuccessCheck.startAnimation(pulse);

        // Animate floating background elements
        findViewById(R.id.vFloat1).startAnimation(pulse);
        findViewById(R.id.vFloat2).startAnimation(pulse);
    }
}
