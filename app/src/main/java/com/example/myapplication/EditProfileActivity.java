package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Premium Edit Profile Activity for OptiSight.
 * Features: Form validation, persistent storage, loading overlay, photo upload (Camera/Gallery).
 */
public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etMobile, etAge, etDOB, etAddress;
    private TextInputLayout tilFullName, tilEmail, tilMobile, tilAge, tilGender;
    private AutoCompleteTextView actvGender;
    private MaterialButton btnSaveChanges, btnCancel, btnReset;
    private SwitchMaterial switchNotifications, switchReminder;
    private View loadingOverlay;
    private ShapeableImageView ivProfileLarge;
    
    private Uri currentPhotoUri;
    private String savedPhotoUriString;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    launchCropActivity(currentPhotoUri);
                }
            });

    private final ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    launchCropActivity(uri);
                }
            });

    private final ActivityResultLauncher<Intent> cropImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String croppedUriString = result.getData().getStringExtra("croppedUri");
                    if (croppedUriString != null) {
                        currentPhotoUri = Uri.parse(croppedUriString);
                        ivProfileLarge.setImageURI(currentPhotoUri);
                        savedPhotoUriString = croppedUriString;
                    }
                }
            });

    private void launchCropActivity(Uri uri) {
        Intent intent = new Intent(this, CropImageActivity.class);
        intent.putExtra("imageUri", uri);
        cropImageLauncher.launch(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupGenderDropdown();
        setupDatePicker();
        applyAnimations();
        loadExistingData();

        btnSaveChanges.setOnClickListener(v -> handleSave());
        findViewById(R.id.btnSaveTop).setOnClickListener(v -> handleSave());
        
        btnCancel.setOnClickListener(v -> finish());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnEditImage).setOnClickListener(v -> showPhotoOptions());
        findViewById(R.id.btnChangePhoto).setOnClickListener(v -> showPhotoOptions());

        btnReset.setOnClickListener(v -> confirmReset());

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        etDOB = findViewById(R.id.etDOB);
        etAddress = findViewById(R.id.etAddress);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilMobile = findViewById(R.id.tilMobile);
        tilAge = findViewById(R.id.tilAge);
        tilGender = findViewById(R.id.tilGender);

        actvGender = findViewById(R.id.actvGender);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancel = findViewById(R.id.btnCancel);
        btnReset = findViewById(R.id.btnReset);

        switchNotifications = findViewById(R.id.switchNotifications);
        switchReminder = findViewById(R.id.switchReminder);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        ivProfileLarge = findViewById(R.id.ivProfileLarge);
    }

    private void loadExistingData() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        etFullName.setText(prefs.getString("fullName", "Sunil Kumar Yadav"));
        etEmail.setText(prefs.getString("email", "sunil.kumar@example.com"));
        etMobile.setText(prefs.getString("mobile", "+91 93925 71978"));
        etAge.setText(prefs.getString("age", "24"));
        etDOB.setText(prefs.getString("dob", "15 Aug 1999"));
        etAddress.setText(prefs.getString("address", "Hitech City, Hyderabad, India"));
        actvGender.setText(prefs.getString("gender", "Male"), false);
        switchNotifications.setChecked(prefs.getBoolean("notifications", true));
        switchReminder.setChecked(prefs.getBoolean("reminder", true));
        
        savedPhotoUriString = prefs.getString("profilePhotoUri", null);
        if (savedPhotoUriString != null) {
            ivProfileLarge.setImageURI(Uri.parse(savedPhotoUriString));
        }
    }

    private void setupGenderDropdown() {
        String[] genders = {getString(R.string.male), getString(R.string.female), getString(R.string.other)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        actvGender.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etDOB.setOnClickListener(v -> {
            final Calendar javaCalendar = Calendar.getInstance();
            int year = javaCalendar.get(Calendar.YEAR);
            int month = javaCalendar.get(Calendar.MONTH);
            int day = javaCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String date = dayOfMonth + " " + getMonthName(monthOfYear) + " " + year1;
                        etDOB.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private String getMonthName(int month) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month];
    }

    private void handleSave() {
        String fullName = Objects.requireNonNull(etFullName.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String mobile = Objects.requireNonNull(etMobile.getText()).toString().trim();
        String age = Objects.requireNonNull(etAge.getText()).toString().trim();
        String gender = actvGender.getText().toString();

        if (!validateInputs(fullName, email, mobile, age, gender)) return;

        showLoading(true);

        // Simulate Saving
        new Handler().postDelayed(() -> {
            saveDataToPrefs(fullName, email, mobile, age, gender);
            showLoading(false);
            showSuccessDialog();
        }, 2000);
    }

    private boolean validateInputs(String name, String email, String mobile, String age, String gender) {
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
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

        if (TextUtils.isEmpty(age)) {
            tilAge.setError(getString(R.string.error_age_required));
            isValid = false;
        } else {
            tilAge.setError(null);
        }

        if (TextUtils.isEmpty(gender)) {
            tilGender.setError(getString(R.string.error_gender_required));
            isValid = false;
        } else {
            tilGender.setError(null);
        }

        return isValid;
    }

    private void saveDataToPrefs(String name, String email, String mobile, String age, String gender) {
        SharedPreferences.Editor editor = getSharedPreferences("UserProfile", MODE_PRIVATE).edit();
        editor.putString("fullName", name);
        editor.putString("email", email);
        editor.putString("mobile", mobile);
        editor.putString("age", age);
        editor.putString("gender", gender);
        editor.putString("dob", Objects.requireNonNull(etDOB.getText()).toString());
        editor.putString("address", Objects.requireNonNull(etAddress.getText()).toString());
        editor.putBoolean("notifications", switchNotifications.isChecked());
        editor.putBoolean("reminder", switchReminder.isChecked());
        if (savedPhotoUriString != null) {
            editor.putString("profilePhotoUri", savedPhotoUriString);
        }
        editor.apply();
    }

    private void showLoading(boolean isLoading) {
        loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveChanges.setEnabled(!isLoading);
    }

    private void showSuccessDialog() {
        Intent intent = new Intent(this, ProfileSuccessActivity.class);
        startActivity(intent);
        finish();
    }

    private void showPhotoOptions() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_photo_bottom_sheet, findViewById(android.R.id.content), false);
        bottomSheetDialog.setContentView(view);

        LinearLayout llCamera = view.findViewById(R.id.llCamera);
        LinearLayout llGallery = view.findViewById(R.id.llGallery);
        LinearLayout llCancel = view.findViewById(R.id.llCancel);

        llCamera.setOnClickListener(v -> {
            checkCameraPermission();
            bottomSheetDialog.dismiss();
        });

        llGallery.setOnClickListener(v -> {
            selectImageLauncher.launch("image/*");
            bottomSheetDialog.dismiss();
        });

        llCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            currentPhotoUri = FileProvider.getUriForFile(this,
                    "com.example.myapplication.fileprovider",
                    photoFile);
            takePictureLauncher.launch(currentPhotoUri);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Information")
                .setMessage(getString(R.string.reset_confirmation))
                .setPositiveButton("Reset", (dialog, which) -> resetFields())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetFields() {
        etFullName.setText("");
        etEmail.setText("");
        etMobile.setText("");
        etAge.setText("");
        etDOB.setText("");
        etAddress.setText("");
        actvGender.setText("", false);
        switchNotifications.setChecked(true);
        switchReminder.setChecked(true);
        ivProfileLarge.setImageResource(R.drawable.ic_person);
        savedPhotoUriString = null;
        Toast.makeText(this, "All fields reset", Toast.LENGTH_SHORT).show();
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        findViewById(R.id.headerLayout).startAnimation(fadeIn);
        ivProfileLarge.startAnimation(fadeIn);
        
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        findViewById(R.id.btnEditImage).startAnimation(pulse);
    }
}
