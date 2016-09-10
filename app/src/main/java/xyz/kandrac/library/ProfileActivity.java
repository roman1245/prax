package xyz.kandrac.library;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by jan on 10.9.2016.
 */
public class ProfileActivity extends AppCompatActivity {

    public static final int LOGIN_REQUEST = 510;

    public static boolean startOrInvokeSignIn(Activity from) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            from.startActivity(new Intent(from, ProfileActivity.class));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
