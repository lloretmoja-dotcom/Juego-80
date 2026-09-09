package com.boug.runner.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.boug.runner.R;
import com.boug.runner.game.BougGame;
import com.boug.runner.game.GameRenderer;
import com.boug.runner.utils.AdManager;
import com.boug.runner.utils.FirebaseManager;
import com.boug.runner.utils.PreferencesManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class GameActivity extends AppCompatActivity {
    private static final String EXTRA_RESTART = "restart";
    
    private BougGame game;
    private GameRenderer renderer;
    private Handler gameHandler;
    private Runnable gameLoop;
    
    private TextView tvScore, tvCoins, tvDiamonds;
    private Button btnPause, btnResume;
    private AdView adView;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    
    private boolean isPaused = false;
    private boolean isGameOver = false;

    public static Intent getIntent(Context context) {
        return new Intent(context, GameActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initViews();
        setupAdMob();
        setupGame();
        setupGameLoop();
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvCoins = findViewById(R.id.tvCoins);
        tvDiamonds = findViewById(R.id.tvDiamonds);
        btnPause = findViewById(R.id.btnPause);
        btnResume = findViewById(R.id.btnResume);
        adView = findViewById(R.id.adView);
    }

    private void setupAdMob() {
        // Banner Ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // Interstitial Ad
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(InterstitialAd ad) {
                    interstitialAd = ad;
                }
            });

        // Rewarded Ad
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest,
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(RewardedAd ad) {
                    rewardedAd = ad;
                }
            });
    }

    private void setupGame() {
        game = new BougGame(this);
        renderer = new GameRenderer(this, game);
        renderer.setOnGameOverListener(() -> onGameOver());
        renderer.setOnScoreUpdateListener(score -> {
            runOnUiThread(() -> {
                tvScore.setText(String.valueOf(score));
                updateResources();
            });
        });
        
        // Set the renderer as content view
        setContentView(renderer);
    }

    private void setupGameLoop() {
        gameHandler = new Handler();
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!isPaused && !isGameOver) {
                    game.update();
                    renderer.invalidate();
                }
                gameHandler.postDelayed(this, 16); // ~60 FPS
            }
        };
        gameHandler.post(gameLoop);
    }

    private void updateResources() {
        PreferencesManager prefs = PreferencesManager.getInstance();
        tvCoins.setText(String.valueOf(prefs.getCoins()));
        tvDiamonds.setText(String.valueOf(prefs.getDiamonds()));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameOver) {
            return super.onTouchEvent(event);
        }

        float x = event.getX();
        float y = event.getY();
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        float screenHeight = getResources().getDisplayMetrics().heightPixels;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Left half = jump, right half = slide
                if (x < screenWidth / 2) {
                    game.jump();
                } else {
                    game.slide();
                }
                break;
            case MotionEvent.ACTION_UP:
                game.releaseSlide();
                break;
        }
        return true;
    }

    private void onGameOver() {
        isGameOver = true;
        runOnUiThread(() -> {
            // Show interstitial ad
            if (interstitialAd != null) {
                interstitialAd.show(GameActivity.this);
            }

            // Save score
            int score = game.getScore();
            int coins = game.getCoinsCollected();
            int diamonds = game.getDiamondsCollected();
            
            PreferencesManager prefs = PreferencesManager.getInstance();
            prefs.addCoins(coins);
            prefs.addDiamonds(diamonds);
            if (score > prefs.getBestScore()) {
                prefs.setBestScore(score);
            }

            // Show game over dialog
            showGameOverDialog(score, coins, diamonds);
        });
    }

    private void showGameOverDialog(int score, int coins, int diamonds) {
        // Implement game over dialog with restart and menu options
        Toast.makeText(this, 
            "Game Over! Score: " + score + " Coins: " + coins + " Diamonds: " + diamonds, 
            Toast.LENGTH_LONG).show();
    }

    public void restartGame() {
        isGameOver = false;
        game.reset();
        renderer.invalidate();
        gameHandler.post(gameLoop);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameHandler.removeCallbacks(gameLoop);
        if (adView != null) {
            adView.destroy();
        }
    }
                                    }
