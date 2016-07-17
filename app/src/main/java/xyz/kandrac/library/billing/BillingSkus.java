package xyz.kandrac.library.billing;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import xyz.kandrac.library.BuildConfig;

/**
 * Created by Jan Kandrac on 16.7.2016.
 */
public final class BillingSkus {

    private static final String DRIVE_SKU = "cloud";

    public static final String TEST_PURCHASED = "android.test.purchased";
    public static final String TEST_CANCELLED = "android.test.canceled";
    public static final String TEST_REFUNDED = "android.test.refunded";
    public static final String TEST_UNAVAILABLE = "android.test.item_unavailable";

    @StringDef({TEST_PURCHASED, TEST_CANCELLED, TEST_REFUNDED, TEST_UNAVAILABLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TestSkus {
    }

    private static String debugAlternative = TEST_PURCHASED;
    private static BillingSkus mInstance;

    private BillingSkus() {

    }

    public static BillingSkus getInstance() {
        return mInstance == null ? mInstance = new BillingSkus() : mInstance;
    }

    public void setDebugAlternative(@TestSkus String debugAlternative) {
        BillingSkus.debugAlternative = debugAlternative;
    }

    public static String getDriveSku(@TestSkus String debugAlternative) {
        return BuildConfig.DEBUG ? debugAlternative : DRIVE_SKU;
    }

    public static String getDriveSku() {
        return BuildConfig.DEBUG ? debugAlternative : DRIVE_SKU;
    }
}
