package com.boug.runner.utils;

import android.app.Activity;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class PurchaseManager implements PurchasesUpdatedListener {
    private static PurchaseManager instance;
    private BillingClient billingClient;
    private Activity activity;
    private PurchaseCallback callback;

    // Replace with your actual product IDs
    private static final String SKU_REMOVE_ADS = "remove_ads";
    private static final String SKU_100_COINS = "100_coins";
    private static final String SKU_500_COINS = "500_coins";
    private static final String SKU_1000_COINS = "1000_coins";
    private static final String SKU_50_DIAMONDS = "50_diamonds";
    private static final String SKU_100_DIAMONDS = "100_diamonds";

    public interface PurchaseCallback {
        void onPurchaseSuccess(String sku);
        void onPurchaseFailed(String sku);
        void onProductsLoaded(List<SkuDetails> products);
    }

    private PurchaseManager() {}

    public static PurchaseManager getInstance() {
        if (instance == null) {
            instance = new PurchaseManager();
        }
        return instance;
    }

    public void init(Activity activity) {
        this.activity = activity;
        billingClient = BillingClient.newBuilder(activity)
            .setListener(this)
            .enablePendingPurchases()
            .build();
            
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d("PurchaseManager", "Billing connection successful");
                    // Query purchases
                    queryPurchases();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to reconnect
            }
        });
    }

    public void queryProducts(List<String> productIds) {
        SkuDetailsParams params = SkuDetailsParams.newBuilder()
            .setSkusList(productIds)
            .setType(BillingClient.SkuType.INAPP)
            .build();

        billingClient.querySkuDetailsAsync(params, (billingResult, skuDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                if (callback != null && skuDetailsList != null) {
                    callback.onProductsLoaded(skuDetailsList);
                }
            }
        });
    }

    public void purchaseProduct(String sku) {
        SkuDetailsParams params = SkuDetailsParams.newBuilder()
            .setSkusList(List.of(sku))
            .setType(BillingClient.SkuType.INAPP)
            .build();

        billingClient.querySkuDetailsAsync(params, (billingResult, skuDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK 
                && skuDetailsList != null && !skuDetailsList.isEmpty()) {
                
                SkuDetails skuDetails = skuDetailsList.get(0);
                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
                    
                billingClient.launchBillingFlow(activity, flowParams);
            }
        });
    }

    public void queryPurchases() {
        Purchase.PurchasesResult result = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        List<Purchase> purchases = result.getPurchasesList();
        
        if (purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    handlePurchase(purchase);
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            if (callback != null) {
                callback.onPurchaseFailed("Cancelled");
            }
        } else {
            if (callback != null) {
                callback.onPurchaseFailed("Error: " + billingResult.getResponseCode());
            }
        }
    }

    private void handlePurchase(Purchase purchase) {
        String sku = purchase.getSkus().get(0);
        PreferencesManager prefs = PreferencesManager.getInstance();
        
        switch (sku) {
            case SKU_REMOVE_ADS:
                prefs.setAdsRemoved(true);
                break;
            case SKU_100_COINS:
                prefs.addCoins(100);
                break;
            case SKU_500_COINS:
                prefs.addCoins(500);
                break;
            case SKU_1000_COINS:
                prefs.addCoins(1000);
                break;
            case SKU_50_DIAMONDS:
                prefs.addDiamonds(50);
                break;
            case SKU_100_DIAMONDS:
                prefs.addDiamonds(100);
                break;
        }
        
        // Acknowledge purchase
        if (!purchase.isAcknowledged()) {
            // Acknowledge the purchase
        }
        
        if (callback != null) {
            callback.onPurchaseSuccess(sku);
        }
    }

    public void setCallback(PurchaseCallback callback) {
        this.callback = callback;
    }

    public void disconnect() {
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}
