package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * Premium Lens Recommendation Activity for OptiSight.
 * Provides personalized lens suggestions based on AI vision analysis results.
 */
public class LensRecommendationActivity extends AppCompatActivity {

    private TextView tvCondition, tvConfidence, tvHealthScore;
    private TextView tvLensType, tvLensPower, tvLensCategory, tvSuitability;
    private TextView tvFeature1, tvFeature2, tvFeature3;
    private MaterialButton btnViewReport, btnSaveRec, btnReturnHome;
    private BottomNavigationView bottomNav;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lens_recommendation);

        initViews();
        handleIntentData();
        setupClickListeners();
        setupNavigation();
        applyAnimations();
    }

    private void initViews() {
        tvCondition = findViewById(R.id.tvCondition);
        tvConfidence = findViewById(R.id.tvConfidence);
        tvHealthScore = findViewById(R.id.tvHealthScore);
        
        tvLensType = findViewById(R.id.tvLensType);
        tvLensPower = findViewById(R.id.tvLensPower);
        tvLensCategory = findViewById(R.id.tvLensCategory);
        tvSuitability = findViewById(R.id.tvSuitability);
        
        tvFeature1 = findViewById(R.id.tvFeature1);
        tvFeature2 = findViewById(R.id.tvFeature2);
        tvFeature3 = findViewById(R.id.tvFeature3);
        
        btnViewReport = findViewById(R.id.btnViewReport);
        btnSaveRec = findViewById(R.id.btnSaveRec);
        btnReturnHome = findViewById(R.id.btnReturnHome);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void handleIntentData() {
        Intent intent = getIntent();
        String condition = intent.getStringExtra("condition");
        int confidence = intent.getIntExtra("confidence", 0);
        int healthScore = intent.getIntExtra("healthScore", 0);

        if (condition == null) {
            condition = "Normal Vision";
            confidence = 97;
            healthScore = 98;
        }

        tvCondition.setText(condition);
        tvConfidence.setText(String.format(Locale.getDefault(), "%d%%", confidence));
        tvHealthScore.setText(String.format(Locale.getDefault(), "%d%%", healthScore));

        applyDynamicLensRecommendation(condition);
    }

    private void applyDynamicLensRecommendation(String condition) {
        switch (condition) {
            case "Myopia":
                tvLensType.setText(R.string.concave_lens_text);
                tvLensPower.setText("-1.25");
                tvLensCategory.setText(R.string.category_single_vision);
                tvSuitability.setText(R.string.suitability_high);
                
                tvFeature1.setText(String.format("✓ %s", getString(R.string.feature_anti_glare)));
                tvFeature2.setText(String.format("✓ %s", getString(R.string.feature_uv_protection)));
                tvFeature3.setText(String.format("✓ %s", getString(R.string.feature_blue_light)));
                break;
                
            case "Hyperopia":
                tvLensType.setText(R.string.convex_lens_text);
                tvLensPower.setText("+1.50");
                tvLensCategory.setText(R.string.category_single_vision);
                tvSuitability.setText(R.string.suitability_high);
                
                tvFeature1.setText(String.format("✓ %s", getString(R.string.feature_reading_support)));
                tvFeature2.setText(String.format("✓ %s", getString(R.string.feature_uv_protection)));
                tvFeature3.setText(String.format("✓ %s", getString(R.string.feature_scratch_resistant)));
                break;
                
            case "Astigmatism":
                tvLensType.setText(R.string.cylindrical_lens_text);
                tvLensPower.setText(R.string.power_customized);
                tvLensCategory.setText(R.string.category_single_vision);
                tvSuitability.setText(R.string.suitability_high);
                
                tvFeature1.setText(String.format("✓ %s", getString(R.string.feature_precision)));
                tvFeature2.setText(String.format("✓ %s", getString(R.string.feature_anti_reflective)));
                tvFeature3.setText(String.format("✓ %s", getString(R.string.feature_uv_protection)));
                break;
                
            case "Normal Vision":
            default:
                tvLensType.setText(R.string.protective_lens_text);
                tvLensPower.setText(R.string.power_normal);
                tvLensCategory.setText(R.string.category_preventive);
                tvSuitability.setText(R.string.suitability_match);
                
                tvFeature1.setText(String.format("✓ %s", getString(R.string.feature_blue_light)));
                tvFeature2.setText(String.format("✓ %s", getString(R.string.feature_anti_glare)));
                tvFeature3.setText(String.format("✓ %s", getString(R.string.feature_uv_protection)));
                break;
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnViewReport.setOnClickListener(v -> 
            Toast.makeText(this, "Opening Detailed Vision Report...", Toast.LENGTH_SHORT).show());

        btnSaveRec.setOnClickListener(v -> 
            Toast.makeText(this, "Lens Recommendation Saved Locally", Toast.LENGTH_SHORT).show());

        btnReturnHome.setOnClickListener(v -> {
            // FIX: Navigate to HomeActivity instead of MainActivity (Login)
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void setupNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_analysis);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // FIX: Navigate to HomeActivity instead of MainActivity (Login)
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            }
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return itemId == R.id.nav_reports;
        });
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        findViewById(R.id.scrollView).startAnimation(fadeIn);
    }
}
