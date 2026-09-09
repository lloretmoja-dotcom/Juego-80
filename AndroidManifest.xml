package com.boug.runner;

import android.app.Application;
import android.content.Context;

import com.boug.runner.utils.FirebaseManager;
import com.boug.runner.utils.PreferencesManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class BougApplication extends Application {
    private static BougApplication instance;
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        appContext = getApplicationContext();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Initialize Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        
        // Initialize Preferences
        PreferencesManager.getInstance().init(this);
        
        // Initialize Firebase Manager
        FirebaseManager.getInstance().init(this);
    }

    public static BougApplication getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }
}
