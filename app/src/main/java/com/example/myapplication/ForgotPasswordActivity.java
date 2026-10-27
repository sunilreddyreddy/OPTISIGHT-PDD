package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

/**
 * Premium Forgot Password Activity for OptiSight.
 * Features: Email validation, premium animations, and navigation to OTP verification.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputLayout tilEmail;
    private MaterialButton btnSendOtp;
    private TextView tvBackToLogin;
    private View vFloat1, vFloat2;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        applyAnimations();

        btnSendOtp.setOnClickListener(v -> handleSendOtp());

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        tilEmail = findViewById(R.id.tilEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        vFloat1 = findViewById(R.id.vFloat1);
        vFloat2 = findViewById(R.id.vFloat2);
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);

        findViewById(R.id.ivLogo).startAnimation(fadeIn);
        findViewById(R.id.tvTitle).startAnimation(fadeIn);
        findViewById(R.id.tvDescription).startAnimation(fadeIn);
        findViewById(R.id.cardForgot).startAnimation(fadeIn);

        if (vFloat1 != null) vFloat1.startAnimation(pulse);
        if (vFloat2 != null) vFloat2.startAnimation(pulse);
    }

    private void handleSendOtp() {
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            return;
        }

        tilEmail.setError(null);

        // Simulation of sending OTP
        Toast.makeText(this, "Verification code sent to " + email, Toast.LENGTH_SHORT).show();

        // Navigate to OTP Verification Screen
        Intent intent = new Intent(ForgotPasswordActivity.this, OTPVerificationActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }
}
