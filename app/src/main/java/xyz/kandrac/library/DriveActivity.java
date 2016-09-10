package xyz.kandrac.library;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Toast.makeText(this, "Already signed in", Toast.LENGTH_SHORT).show();
        } else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(
                                    AuthUI.EMAIL_PROVIDER,
                                    AuthUI.GOOGLE_PROVIDER)
                            .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                            .build(),
                    RC_SIGN_IN);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Log.d("janko", "signed in");
                Toast.makeText(this, "hooray", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("janko", "not signed in");
                Toast.makeText(this, "nah", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
