package xyz.kandrac.library;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import xyz.kandrac.library.utils.LogUtils;

/**
 * Created by Jan Kandrac on 14.7.2016.
 */
public class DriveActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String LOG_TAG = MainActivity.class.getName();
    public static final int RESOLVE_CONNECTION_REQUEST_CODE = 115;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        driveOnCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    GoogleApiClient mGoogleApiClient;

    private void driveOnCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtils.d("janko", connectionResult.getErrorMessage() + " : " + connectionResult.getErrorCode());
        Toast.makeText(this, "Result = " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
    }
}
