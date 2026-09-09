package com.boug.runner;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.boug.runner.ui.GameActivity;
import com.boug.runner.ui.ProfileActivity;
import com.boug.runner.ui.ShopActivity;
import com.boug.runner.utils.FirebaseManager;
import com.boug.runner.utils.PreferencesManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private AdView adView;
    private TextView tvCoins, tvDiamonds, tvBestScore;
    private Button btnPlay, btnShop, btnProfile, btnAchievements, btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupAdMob();
        setupClickListeners();
        updateUI();
        
        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseManager.getInstance().signInAnonymously();
        }
    }

    private void initViews() {
        tvCoins = findViewById(R.id.tvCoins);
        tvDiamonds = findViewById(R.id.tvDiamonds);
        tvBestScore = findViewById(R.id.tvBestScore);
        btnPlay = findViewById(R.id.btnPlay);
        btnShop = findViewById(R.id.btnShop);
        btnProfile = findViewById(R.id.btnProfile);
        btnAchievements = findViewById(R.id.btnAchievements);
        btnSettings = findViewById(R.id.btnSettings);
        adView = findViewById(R.id.adView);
    }

    private void setupAdMob() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void setupClickListeners() {
        btnPlay.setOnClickListener(v -> {
            startActivity(GameActivity.getIntent(MainActivity.this));
        });

        btnShop.setOnClickListener(v -> {
            startActivity(ShopActivity.getIntent(MainActivity.this));
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(ProfileActivity.getIntent(MainActivity.this));
        });

        btnAchievements.setOnClickListener(v -> {
            startActivity(AchievementsActivity.getIntent(MainActivity.this));
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(SettingsActivity.getIntent(MainActivity.this));
        });
    }

    private void updateUI() {
        PreferencesManager prefs = PreferencesManager.getInstance();
        tvCoins.setText(String.valueOf(prefs.getCoins()));
        tvDiamonds.setText(String.valueOf(prefs.getDiamonds()));
        tvBestScore.setText(String.valueOf(prefs.getBestScore()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        
        // Resume AdView
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }
}
