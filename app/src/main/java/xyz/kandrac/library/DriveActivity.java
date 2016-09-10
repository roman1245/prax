package xyz.kandrac.library;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Jan Kandrac on 14.7.2016.
 */
public class DriveActivity extends AppCompatActivity {

    public static final String LOG_TAG = DriveActivity.class.getName();

    public static final int RESOLVE_CONNECTION_REQUEST_CODE = 115;
    public static final int RC_SIGN_IN = 322;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {

        }
    }
}
