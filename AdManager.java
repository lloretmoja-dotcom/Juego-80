package com.boug.runner.utils;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class AdManager {
    private static AdManager instance;
    private Context context;
    private AdView bannerAdView;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    
    // Replace with your AdMob IDs
    private static final String BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    private AdManager() {}

    public static AdManager getInstance() {
        if (instance == null) {
            instance = new AdManager();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // Ads initialized
            }
        });
    }

    public void loadBannerAd(ViewGroup adContainer) {
        if (PreferencesManager.getInstance().areAdsRemoved()) {
            adContainer.setVisibility(ViewGroup.GONE);
            return;
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView = new AdView(context);
        bannerAdView.setAdUnitId(BANNER_AD_UNIT_ID);
        bannerAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        adContainer.addView(bannerAdView);
        bannerAdView.loadAd(adRequest);
    }

    public void loadInterstitialAd() {
        if (PreferencesManager.getInstance().areAdsRemoved()) {
            return;
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, INTERSTITIAL_AD_UNIT_ID, adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(InterstitialAd ad) {
                    interstitialAd = ad;
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    interstitialAd = null;
                }
            });
    }

    public void showInterstitialAd(Activity activity) {
        if (PreferencesManager.getInstance().areAdsRemoved()) {
            return;
        }

        if (interstitialAd != null) {
            interstitialAd.show(activity);
            loadInterstitialAd(); // Preload next ad
        }
    }

    public void loadRewardedAd() {
        if (PreferencesManager.getInstance().areAdsRemoved()) {
            return;
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest,
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(RewardedAd ad) {
                    rewardedAd = ad;
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    rewardedAd = null;
                }
            });
    }

    public void showRewardedAd(Activity activity, RewardedAdCallback callback) {
        if (PreferencesManager.getInstance().areAdsRemoved()) {
            if (callback != null) {
                callback.onRewardEarned();
            }
            return;
        }

        if (rewardedAd != null) {
            rewardedAd.show(activity, rewardItem -> {
                if (callback != null) {
                    callback.onRewardEarned();
                }
                loadRewardedAd();
            });
        }
    }

    public void destroyBannerAd() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
            bannerAdView = null;
        }
    }

    public interface RewardedAdCallback {
        void onRewardEarned();
    }
}
