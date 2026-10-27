package com.example.myapplication;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;
import java.util.Map;

public interface OptiSightApiService {

    // 1. User Registration
    @POST("auth/register")
    Call<Map<String, Object>> registerUser(@Body Map<String, String> registerData);

    // 2. User Login
    @POST("auth/login")
    Call<Map<String, Object>> loginUser(@Body Map<String, String> loginData);

    // 3. Forgot Password / OTP
    @POST("auth/forgot-password")
    Call<Map<String, Object>> forgotPassword(@Body Map<String, String> data);

    // 4. Reset Password
    @POST("auth/reset-password")
    Call<Map<String, Object>> resetPassword(@Body Map<String, String> data);

    // 5. Get Profile
    @GET("users/me")
    Call<Map<String, Object>> getUserProfile(@Header("Authorization") String token);

    // 6. Update Profile
    @PUT("users/me/profile")
    Call<Map<String, Object>> updateUserProfile(
            @Header("Authorization") String token,
            @Body Map<String, String> profileData
    );

    // 7. Eye Image Upload
    @Multipart
    @POST("analysis/upload")
    Call<Map<String, Object>> uploadEyeImage(
            @Header("Authorization") String token,
            @Part MultipartBody.Part file
    );

    // 8. AI Vision Analysis Execution
    @POST("analysis/analyze")
    Call<Map<String, Object>> analyzeEyeImage(
            @Header("Authorization") String token,
            @Body Map<String, String> requestBody
    );

    // 9. Fetch Analysis History
    @GET("analysis/history")
    Call<List<Map<String, Object>>> getAnalysisHistory(
            @Header("Authorization") String token
    );

    // 10. Fetch Single Analysis Detail
    @GET("analysis/{id}")
    Call<Map<String, Object>> getAnalysisDetail(
            @Header("Authorization") String token,
            @Path("id") int analysisId
    );
}
