package xyz.kandrac.library.billing;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import androidx.core.util.Pair;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import xyz.kandrac.library.utils.LogUtils;

/**
 * Created by Jan Kandrac on 15.7.2016.
 */
public class MultipleBillingItemAsyncTask extends AsyncTask<String, Void, Pair<String, String>> {

    public static final String LOG_TAG = "LibraryBilling";

    private String mPackageName;
    private IInAppBillingService mBillingService;

    public MultipleBillingItemAsyncTask(String packageName, IInAppBillingService billingService) {
        mPackageName = packageName;
        mBillingService = billingService;
    }

    @Override
    protected Pair<String, String> doInBackground(String... strings) {
        ArrayList<String> skuList = new ArrayList<>();
        Collections.addAll(skuList, strings);

        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        try {
            Bundle skuDetails = mBillingService.getSkuDetails(3, mPackageName, "inapp", querySkus);

            int response = skuDetails.getInt("RESPONSE_CODE");
            Log.d(LOG_TAG, "Response = " + response);
            if (response == 0) {
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                if (responseList == null) {
                    return null;
                }
                Log.d(LOG_TAG, "Response count = " + responseList.size());
                for (String thisResponse : responseList) {
                    try {
                        JSONObject object = new JSONObject(thisResponse);
                        return new Pair<>(object.getString("productId"), object.getString("price"));
                    } catch (JSONException ex) {
                        LogUtils.e(LOG_TAG, "Error occurs during parsing", ex);
                    }
                }
            }


        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Pair<String, String> stringStringPair) {
        super.onPostExecute(stringStringPair);
    }
}
