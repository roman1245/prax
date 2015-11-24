package kandrac.xyz.library.barcode;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import kandrac.xyz.library.barcode.ui.GraphicOverlay;

/**
 * Created by VizGhar on 18.10.2015.
 */
public abstract class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {

    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

    public BarcodeTrackerFactory(GraphicOverlay<BarcodeGraphic> barcodeGraphicOverlay) {
        mGraphicOverlay = barcodeGraphicOverlay;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay);
        return new BarcodeGraphicTracker(mGraphicOverlay, graphic) {
            @Override
            public void onUpdate(Detector.Detections<Barcode> detectionResults, Barcode item) {
                super.onUpdate(detectionResults, item);
                onBarcodeRead(item.displayValue);
            }
        };
    }

    public abstract void onBarcodeRead(String barcode);
}