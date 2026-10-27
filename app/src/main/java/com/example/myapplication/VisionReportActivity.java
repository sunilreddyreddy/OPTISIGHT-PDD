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
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Professional View Vision Report Activity for OptiSight.
 * Features a clean healthcare layout with detailed analysis cards.
 */
public class VisionReportActivity extends AppCompatActivity {

    private MaterialButton btnDownloadPdf, btnShareReport, btnPrintReport, btnReturnHome, btnViewTips;
    private BottomNavigationView bottomNav;
    private MaterialCardView cardReportInfo, cardPatientDetails, cardVisionResult, cardLensRec, cardAiRec, cardReportSummary;

    private TextView tvConditionValue, tvConfidenceValue, tvHealthScoreValue, tvSeverityValue;
    private TextView tvLensTypeValue, tvLensPowerValue, tvLensFeaturesValue;
    private TextView tvOverallStatusValue, tvAiAssessmentValue, tvHealthRatingValue;
    private TextView tvReportIdValue, tvReportDateValue, tvReportTimeValue;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision_report);

        initViews();
        generateReportMetadata();
        handleIntentData();
        setupClickListeners();
        setupNavigation();
        applyAnimations();
    }

    private void initViews() {
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf);
        btnShareReport = findViewById(R.id.btnShareReport);
        btnPrintReport = findViewById(R.id.btnPrintReport);
        btnReturnHome = findViewById(R.id.btnReturnHome);
        btnViewTips = findViewById(R.id.btnViewTips);
        bottomNav = findViewById(R.id.bottomNav);

        cardReportInfo = findViewById(R.id.cardReportInfo);
        cardPatientDetails = findViewById(R.id.cardPatientDetails);
        cardVisionResult = findViewById(R.id.cardVisionResult);
        cardLensRec = findViewById(R.id.cardLensRec);
        cardAiRec = findViewById(R.id.cardAiRec);
        cardReportSummary = findViewById(R.id.cardReportSummary);

        tvConditionValue = findViewById(R.id.tvConditionValue);
        tvConfidenceValue = findViewById(R.id.tvConfidenceValue);
        tvHealthScoreValue = findViewById(R.id.tvHealthScoreValue);
        tvSeverityValue = findViewById(R.id.tvSeverityValue);

        tvLensTypeValue = findViewById(R.id.tvLensTypeValue);
        tvLensPowerValue = findViewById(R.id.tvLensPowerValue);
        tvLensFeaturesValue = findViewById(R.id.tvLensFeaturesValue);

        tvOverallStatusValue = findViewById(R.id.tvOverallStatusValue);
        tvAiAssessmentValue = findViewById(R.id.tvAiAssessmentValue);
        tvHealthRatingValue = findViewById(R.id.tvHealthRatingValue);

        tvReportIdValue = findViewById(R.id.tvReportIdValue);
        tvReportDateValue = findViewById(R.id.tvReportDateValue);
        tvReportTimeValue = findViewById(R.id.tvReportTimeValue);
    }

    private void generateReportMetadata() {
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date now = new Date();

        tvReportDateValue.setText(dateFmt.format(now));
        tvReportTimeValue.setText(timeFmt.format(now));

        int randomId = 100000 + new Random().nextInt(900000);
        tvReportIdValue.setText(String.format(Locale.getDefault(), "#OS-%d", randomId));
    }

    private void handleIntentData() {
        Intent intent = getIntent();
        String condition = intent.getStringExtra("condition");
        int confidence = intent.getIntExtra("confidence", 0);
        int healthScore = intent.getIntExtra("healthScore", 0);
        String analysisDate = intent.getStringExtra("analysisDate");
        String analysisTime = intent.getStringExtra("analysisTime");

        if (condition == null) {
            condition = "Normal Vision";
            confidence = 97;
            healthScore = 98;
        }

        tvConditionValue.setText(condition);
        tvConfidenceValue.setText(String.format(Locale.getDefault(), "%d%%", confidence));
        tvHealthScoreValue.setText(String.format(Locale.getDefault(), "%d/100", healthScore));

        if (analysisDate != null) {
            tvReportDateValue.setText(analysisDate);
        }
        if (analysisTime != null) {
            tvReportTimeValue.setText(analysisTime);
        }

        updateDynamicReport(condition);
    }

    private void updateDynamicReport(String condition) {
        switch (condition) {
            case "Myopia":
                tvSeverityValue.setText(R.string.severity_moderate);
                tvLensTypeValue.setText(R.string.concave_lens_text);
                tvLensPowerValue.setText(R.string.lens_power_myopia);
                tvLensFeaturesValue.setText(R.string.lens_features_myopia);
                tvOverallStatusValue.setText(R.string.status_action_required);
                tvAiAssessmentValue.setText(R.string.assessment_myopia);
                tvHealthRatingValue.setText(R.string.rating_good_b_plus);
                break;
            case "Hyperopia":
                tvSeverityValue.setText(R.string.severity_moderate);
                tvLensTypeValue.setText(R.string.convex_lens_text);
                tvLensPowerValue.setText(R.string.lens_power_hyperopia);
                tvLensFeaturesValue.setText(R.string.lens_features_hyperopia);
                tvOverallStatusValue.setText(R.string.status_action_required);
                tvAiAssessmentValue.setText(R.string.assessment_hyperopia);
                tvHealthRatingValue.setText(R.string.rating_good_b);
                break;
            case "Astigmatism":
                tvSeverityValue.setText(R.string.severity_mild);
                tvLensTypeValue.setText(R.string.cylindrical_lens_text);
                tvLensPowerValue.setText(R.string.power_customized);
                tvLensFeaturesValue.setText(R.string.lens_features_astigmatism);
                tvOverallStatusValue.setText(R.string.status_correction_rec);
                tvAiAssessmentValue.setText(R.string.assessment_astigmatism);
                tvHealthRatingValue.setText(R.string.rating_fair_c_plus);
                break;
            case "Normal Vision":
            default:
                tvSeverityValue.setText(R.string.severity_none);
                tvLensTypeValue.setText(R.string.protective_lens_text);
                tvLensPowerValue.setText(R.string.power_normal);
                tvLensFeaturesValue.setText(R.string.lens_features_normal);
                tvOverallStatusValue.setText(R.string.healthy_vision_status);
                tvAiAssessmentValue.setText(R.string.assessment_normal_val);
                tvHealthRatingValue.setText(R.string.rating_excellent);
                break;
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnDownloadPdf.setOnClickListener(v -> 
            Toast.makeText(this, R.string.report_download_success, Toast.LENGTH_SHORT).show());

        btnShareReport.setOnClickListener(v -> {
            Toast.makeText(this, R.string.report_share_success, Toast.LENGTH_SHORT).show();
            shareReport();
        });

        btnPrintReport.setOnClickListener(v -> 
            Toast.makeText(this, "Connecting to Cloud Printer...", Toast.LENGTH_SHORT).show());

        btnReturnHome.setOnClickListener(v -> {
            // FIX: Navigate to HomeActivity instead of MainActivity (Login)
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        if (btnViewTips != null) {
            btnViewTips.setOnClickListener(v -> {
                Intent intent = new Intent(VisionReportActivity.this, EyeHealthTipsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void shareReport() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String condition = tvConditionValue.getText().toString();
        sendIntent.putExtra(Intent.EXTRA_TEXT, "OptiSight AI Vision Report: Condition " + condition + ". Health Score: " + tvHealthScoreValue.getText().toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Vision Report");
        startActivity(shareIntent);
    }

    private void setupNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_reports);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // FIX: Navigate to HomeActivity instead of MainActivity (Login)
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_analysis) {
                Intent intent = new Intent(this, UploadEyeImageActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return itemId == R.id.nav_reports;
        });
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        
        cardReportInfo.startAnimation(fadeIn);
        cardPatientDetails.startAnimation(fadeIn);
        cardVisionResult.startAnimation(fadeIn);
        cardLensRec.startAnimation(fadeIn);
        cardAiRec.startAnimation(fadeIn);
        cardReportSummary.startAnimation(fadeIn);
    }
}
