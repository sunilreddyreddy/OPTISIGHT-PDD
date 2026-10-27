package com.example.myapplication;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Persistence: Load user preference for theme
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        
        // Apply theme automatically on app startup
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Apply language automatically on app startup
        String lang = prefs.getString("Locale.Helper.Selected.Language", "en");
        LocaleHelper.setLocale(this, lang);
    }
}
