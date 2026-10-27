package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * Premium Vision Analysis Result Activity for OptiSight.
 * Displays randomized AI findings, health scores, and recommended actions.
 */
public class VisionAnalysisResultActivity extends AppCompatActivity {

    private ImageView ivEyeResult;
    private TextView tvConditionValue, tvSeverityValue, tvConfidenceValue, tvHealthScoreValue;
    private TextView tvStatusTitle, tvStatusContent, tvRecommendedLensValue;
    private TextView tvRec1, tvRec2, tvRec3, tvAnalysisDateTime;
    private ProgressBar pbConfidence, pbHealthScore;
    private View viewStatusDivider;
    private MaterialButton btnViewLensRec, btnViewReport, btnAnalyzeAnother;
    private BottomNavigationView bottomNav;
    private String analysisDate, analysisTime;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision_analysis_result);

        initViews();
        handleIntentData();
        displayRandomResult();
        setupClickListeners();
        setupNavigation();
        applyAnimations();
    }

    private void initViews() {
        ivEyeResult = findViewById(R.id.ivEyeResult);
        tvConditionValue = findViewById(R.id.tvConditionValue);
        tvSeverityValue = findViewById(R.id.tvSeverityValue);
        tvConfidenceValue = findViewById(R.id.tvConfidenceValue);
        tvHealthScoreValue = findViewById(R.id.tvHealthScoreValue);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusContent = findViewById(R.id.tvStatusContent);
        tvRecommendedLensValue = findViewById(R.id.tvRecommendedLensValue);
        tvRec1 = findViewById(R.id.tvRec1);
        tvRec2 = findViewById(R.id.tvRec2);
        tvRec3 = findViewById(R.id.tvRec3);
        tvAnalysisDateTime = findViewById(R.id.tvAnalysisDateTime);
        pbConfidence = findViewById(R.id.pbConfidence);
        pbHealthScore = findViewById(R.id.pbHealthScore);
        viewStatusDivider = findViewById(R.id.viewStatusDivider);
        
        btnViewLensRec = findViewById(R.id.btnViewLensRec);
        btnViewReport = findViewById(R.id.btnViewReport);
        btnAnalyzeAnother = findViewById(R.id.btnAnalyzeAnother);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void handleIntentData() {
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            ivEyeResult.setImageURI(Uri.parse(imageUriString));
            ivEyeResult.setColorFilter(null);
        }
    }

    private void displayRandomResult() {
        Intent intent = getIntent();
        String condition = intent.getStringExtra("condition");
        int confidence = intent.getIntExtra("confidence", 0);
        int healthScore = intent.getIntExtra("healthScore", 0);
        String lens = intent.getStringExtra("lens");
        analysisDate = intent.getStringExtra("analysisDate");
        analysisTime = intent.getStringExtra("analysisTime");

        if (condition == null) return;

        tvConditionValue.setText(condition);
        pbConfidence.setProgress(confidence);
        tvConfidenceValue.setText(String.format(Locale.getDefault(), "%d%%", confidence));
        pbHealthScore.setProgress(healthScore);
        tvHealthScoreValue.setText(String.format(Locale.getDefault(), "%d%%", healthScore));
        tvRecommendedLensValue.setText(lens);

        if (analysisDate != null && analysisTime != null) {
            String dateTimeText = "Date: " + analysisDate + "\nTime: " + analysisTime;
            tvAnalysisDateTime.setText(dateTimeText);
            tvAnalysisDateTime.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }

        updateDetailsByCondition(condition);
    }

    private void updateDetailsByCondition(String condition) {
        switch (condition) {
            case "Myopia":
                tvSeverityValue.setText(R.string.severity_moderate);
                tvSeverityValue.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                tvStatusTitle.setText(R.string.symptoms_label);
                tvStatusContent.setText(R.string.symptom_blurred);
                viewStatusDivider.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                tvRec1.setText(R.string.recommendation_wear);
                tvRec2.setText(R.string.recommendation_screen);
                tvRec3.setText(R.string.recommendation_guidelines);
                break;
            case "Hyperopia":
                tvSeverityValue.setText(R.string.severity_moderate);
                tvSeverityValue.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                tvStatusTitle.setText(R.string.symptoms_label);
                tvStatusContent.setText(R.string.symptom_focus);
                viewStatusDivider.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                tvRec1.setText(R.string.recommendation_wear);
                tvRec2.setText(R.string.recommendation_checkups);
                tvRec3.setText(R.string.recommendation_guidelines);
                break;
            case "Astigmatism":
                tvSeverityValue.setText(R.string.severity_mild);
                tvSeverityValue.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                tvStatusTitle.setText(R.string.symptoms_label);
                tvStatusContent.setText(R.string.symptom_blurred);
                viewStatusDivider.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                tvRec1.setText(R.string.recommendation_wear);
                tvRec2.setText(R.string.recommendation_screen);
                tvRec3.setText(R.string.recommendation_guidelines);
                break;
            case "Eye Disease":
                tvSeverityValue.setText(R.string.severity_high);
                tvSeverityValue.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                tvStatusTitle.setText(R.string.symptoms_label);
                tvStatusContent.setText("Corneal or lens opacity detected. Professional ophthalmologist examination advised.");
                viewStatusDivider.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                tvRec1.setText("Consult an ophthalmologist for detailed slit-lamp evaluation.");
                tvRec2.setText(R.string.recommendation_wear);
                tvRec3.setText(R.string.recommendation_checkups);
                break;
            case "Eye Strain / Fatigue":
                tvSeverityValue.setText(R.string.severity_mild);
                tvSeverityValue.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                tvStatusTitle.setText(R.string.symptoms_label);
                tvStatusContent.setText("Scleral vascular congestion and digital eye strain detected.");
                viewStatusDivider.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                tvRec1.setText(R.string.recommendation_20_20_20);
                tvRec2.setText("Use lubricating eye drops and blue-light filtration.");
                tvRec3.setText(R.string.recommendation_screen);
                break;
            case "Normal Vision":
            default:
                tvSeverityValue.setText(R.string.severity_none);
                tvSeverityValue.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                tvStatusTitle.setText(R.string.vision_status_label);
                tvStatusContent.setText(R.string.healthy_vision_status);
                viewStatusDivider.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                tvRec1.setText(R.string.recommendation_habits);
                tvRec2.setText(R.string.recommendation_20_20_20);
                tvRec3.setText(R.string.recommendation_checkups);
                break;
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnViewLensRec.setOnClickListener(v -> {
            Intent intent = new Intent(this, LensRecommendationActivity.class);
            intent.putExtra("condition", tvConditionValue.getText().toString());
            intent.putExtra("confidence", pbConfidence.getProgress());
            intent.putExtra("healthScore", pbHealthScore.getProgress());
            startActivity(intent);
        });

        btnViewReport.setOnClickListener(v -> {
            Intent intent = new Intent(this, VisionReportActivity.class);
            intent.putExtra("condition", tvConditionValue.getText().toString());
            intent.putExtra("confidence", pbConfidence.getProgress());
            intent.putExtra("healthScore", pbHealthScore.getProgress());
            intent.putExtra("analysisDate", analysisDate);
            intent.putExtra("analysisTime", analysisTime);
            startActivity(intent);
        });

        btnAnalyzeAnother.setOnClickListener(v -> {
            Intent intent = new Intent(this, UploadEyeImageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
            return false;
        });
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        findViewById(R.id.scrollView).startAnimation(fadeIn);
    }
}
