package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

/**
 * Premium Reset Password Activity for OptiSight.
 * Features: Password validation, strength indicator, glassmorphism UI, and success state.
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private TextInputLayout tilNewPassword, tilConfirmPassword;
    private MaterialButton btnReset, btnContinueLogin;
    private LinearLayout layoutInput, layoutSuccess;
    private View[] strengthBars = new View[4];
    private View vFloat1, vFloat2;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        initViews();
        setupPasswordListeners();
        applyAnimations();

        btnReset.setOnClickListener(v -> handleResetPassword());
        btnContinueLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void initViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnReset = findViewById(R.id.btnReset);
        btnContinueLogin = findViewById(R.id.btnContinueLogin);
        layoutInput = findViewById(R.id.layoutInput);
        layoutSuccess = findViewById(R.id.layoutSuccess);

        strengthBars[0] = findViewById(R.id.strength1);
        strengthBars[1] = findViewById(R.id.strength2);
        strengthBars[2] = findViewById(R.id.strength3);
        strengthBars[3] = findViewById(R.id.strength4);

        vFloat1 = findViewById(R.id.vFloat1);
        vFloat2 = findViewById(R.id.vFloat2);
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);

        findViewById(R.id.ivLogo).startAnimation(fadeIn);
        layoutInput.startAnimation(fadeIn);

        if (vFloat1 != null) vFloat1.startAnimation(pulse);
        if (vFloat2 != null) vFloat2.startAnimation(pulse);
    }

    private void setupPasswordListeners() {
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 6) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[@#$%^&+=].*")) strength++;

        int color;
        switch (strength) {
            case 1: color = android.R.color.holo_red_light; break;
            case 2: color = android.R.color.holo_orange_light; break;
            case 3: color = android.R.color.holo_blue_light; break;
            case 4: color = android.R.color.holo_green_light; break;
            default: color = -1;
        }

        for (int i = 0; i < 4; i++) {
            if (i < strength) {
                strengthBars[i].setBackgroundColor(ContextCompat.getColor(this, color));
            } else {
                strengthBars[i].setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            }
        }
    }

    private void handleResetPassword() {
        String password = Objects.requireNonNull(etNewPassword.getText()).toString();
        String confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString();

        if (TextUtils.isEmpty(password)) {
            tilNewPassword.setError("Password required");
            return;
        }
        if (password.length() < 6) {
            tilNewPassword.setError("Minimum 6 characters");
            return;
        }
        tilNewPassword.setError(null);

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            return;
        }
        tilConfirmPassword.setError(null);

        // Simulation of success
        showSuccessState();
    }

    private void showSuccessState() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);

        layoutInput.startAnimation(fadeOut);
        layoutInput.setVisibility(View.GONE);

        layoutSuccess.setVisibility(View.VISIBLE);
        layoutSuccess.startAnimation(fadeIn);

        Toast.makeText(this, getString(R.string.password_reset_success), Toast.LENGTH_SHORT).show();
    }
}
