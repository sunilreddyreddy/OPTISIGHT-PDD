package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

/**
 * Premium Signup Activity for OptiSight.
 * Features: User registration with validation, glassmorphism UI, and terms acceptance.
 */
public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etMobile, etPassword, etConfirmPassword;
    private TextInputLayout tilFullName, tilEmail, tilMobile, tilPassword, tilConfirmPassword;
    private CheckBox cbTerms;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();

        MaterialButton btnSignup = findViewById(R.id.btnSignup);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnSignup.setOnClickListener(v -> performSignup());

        tvLogin.setOnClickListener(v -> {
            // Navigate back to Login Screen
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilMobile = findViewById(R.id.tilMobile);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        
        cbTerms = findViewById(R.id.cbTerms);
    }

    private void performSignup() {
        String fullName = Objects.requireNonNull(etFullName.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String mobile = Objects.requireNonNull(etMobile.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString().trim();

        boolean isValid = true;

        // Validation Logic
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError(getString(R.string.error_fullname_required));
            isValid = false;
        } else {
            tilFullName.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(mobile)) {
            tilMobile.setError(getString(R.string.error_mobile_required));
            isValid = false;
        } else {
            tilMobile.setError(null);
        }

        if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, getString(R.string.error_terms_required), Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {
            java.util.Map<String, String> regData = new java.util.HashMap<>();
            regData.put("full_name", fullName);
            regData.put("email", email);
            regData.put("mobile", mobile);
            regData.put("password", password);

            OptiSightApiService apiService = ApiClient.getClient().create(OptiSightApiService.class);
            apiService.registerUser(regData).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                    Toast.makeText(SignupActivity.this, getString(R.string.account_created), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, OTPVerificationActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                    // Fallback to seamless offline transition
                    Toast.makeText(SignupActivity.this, getString(R.string.account_created), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, OTPVerificationActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}
