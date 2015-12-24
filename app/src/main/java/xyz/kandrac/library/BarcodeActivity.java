package xyz.kandrac.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import xyz.kandrac.library.barcode.BarcodeGraphic;
import xyz.kandrac.library.barcode.BarcodeTrackerFactory;
import xyz.kandrac.library.barcode.ui.CameraSourcePreview;
import xyz.kandrac.library.barcode.ui.GraphicOverlay;

/**
 * Activity that displays barcode scanner. This was implemented based on Google Vision
 * samples, that can be easily found on web, but heavily modified to fit needs of this
 * application.
 * <p/>
 * Created by VizGhar on 18.10.2015.
 */
public class BarcodeActivity extends AppCompatActivity {

    private static final String TAG = "Barcode-reader";

    private static final int RC_HANDLE_GMS = 9001;

    // constants used to pass extra data in the intent
    public static final String AUTO_FOCUS = "AUTO_FOCUS";
    public static final String BARCODE_TEXT = "Barcode";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.barcode_capture);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.graphicOverlay);

        // read parameters from the intent used to launch the activity.
        boolean autoFocus = getIntent().getBooleanExtra(AUTO_FOCUS, true);

        createCameraSource(autoFocus);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     * <p/>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus) {
        Context context = getApplicationContext();

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.ISBN | Barcode.EAN_13).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay) {
            @Override
            public void onBarcodeRead(final String barcode) {
                complete(barcode);
            }
        };
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, "low storage", Toast.LENGTH_LONG).show();
                Log.w(TAG, "low storage");
            }
        }

        mCameraSource = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(com.google.android.gms.vision.CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(autoFocus)
                .setRequestedPreviewSize(1600, 1024)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS).show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void complete(String barcode) {
        Intent data = new Intent();
        data.putExtra(BARCODE_TEXT, barcode);
        setResult(CommonStatusCodes.SUCCESS, data);
        finish();
    }
}
