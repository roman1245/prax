package xyz.kandrac.library.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

import xyz.kandrac.library.billing.BillingSkus;
import xyz.kandrac.library.billing.util.IABKeyEncoder;
import xyz.kandrac.library.billing.util.IabException;
import xyz.kandrac.library.billing.util.IabHelper;
import xyz.kandrac.library.billing.util.IabResult;
import xyz.kandrac.library.billing.util.Inventory;
import xyz.kandrac.library.billing.util.Purchase;

/**
 * Created by jan on 6.12.2016.
 */

public class IABConfigurator {

    public static final int PURCHASE_DRIVE_REQUEST = 1241;
    public static final String LOG_TAG = IABConfigurator.class.getName();

    private IabHelper mHelper;
    private PurchasedListener mListener;
    private boolean mAdmin;

    public void consume(String sku) {
        // querying inventory on main thread is dangerous, but this is only visible for debug
        try {
            mHelper.consumeAsync(mHelper.queryInventory().getPurchase(sku), new IabHelper.OnConsumeFinishedListener() {
                @Override
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    mListener.onDrivePurchased(false);
                }
            });
        } catch (IabException | IabHelper.IabAsyncInProgressException ex) {
            Log.d(LOG_TAG, "error consuming");
        }
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        return mHelper.handleActivityResult(requestCode, resultCode, data);
    }

    public interface PurchasedListener {
        void onDrivePurchased(boolean purchased);
    }

    public void setAdmin(boolean isAdmin) {
        mAdmin = isAdmin;
    }

    public IABConfigurator(Context context, PurchasedListener listener) {
        mHelper = new IabHelper(context, IABKeyEncoder.getKey());
        mListener = listener;
    }

    public void onDestroy() {
        mHelper.disposeWhenFinished();
        mHelper = null;
    }

    public boolean start() {

        try {
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        LogUtils.d(LOG_TAG, "Problem setting up In-app Billing: " + result);
                    }
                    LogUtils.d(LOG_TAG, "IAB is setup - getting info about paid content");
                    setupPaidContent();
                }
            });
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void setupPaidContent() {
        try {
            ArrayList<String> skus = new ArrayList<>();
            skus.add(BillingSkus.getDriveSku());
            mHelper.queryInventoryAsync(true, skus, null, new IabHelper.QueryInventoryFinishedListener() {
                @Override
                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                    if (result.isFailure()) {
                        LogUtils.d(LOG_TAG, "error getting inventory: " + result);
                    } else {
                        if (mAdmin) {
                            mListener.onDrivePurchased(true);
                        } else {
                            mListener.onDrivePurchased(inventory.hasPurchase(BillingSkus.getDriveSku()));
                        }
                    }
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    public void purchaseFlow(Activity activity, String sku) {
        try {
            mHelper.launchPurchaseFlow(activity, sku, PURCHASE_DRIVE_REQUEST, new IabHelper.OnIabPurchaseFinishedListener() {
                @Override
                public void onIabPurchaseFinished(IabResult result, Purchase info) {

                    if (mAdmin) {
                        mListener.onDrivePurchased(true);
                        return;
                    }

                    if (result.isFailure() && result.getResponse() != 7) {
                        LogUtils.d(LOG_TAG, "Error purchasing: " + result);
                    } else if (info.getSku().equals(BillingSkus.getDriveSku())) {
                        mListener.onDrivePurchased(true);
                    }
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }
}
