package kandrac.xyz.library;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kandrac.xyz.library.databinding.BookInputBinding;
import kandrac.xyz.library.model.DatabaseProvider;
import kandrac.xyz.library.model.obj.Book;

/**
 * Created by VizGhar on 11.10.2015.
 */
public class EditBookActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_BOOK_ID = "book_id_extra";
    private static final String TAG = EditBookActivity.class.getName();

    private Long mBookId;
    private BookInputBinding binding;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_BARCODE = 2;

    private static final int TAKE_PHOTO_PERMISSIONS = 2;
    private static final int BARCODE_PERMISSIONS = 3;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.book_input_author)
    EditText mAuthorEdit;

    @Bind(R.id.book_input_title)
    EditText mTitleEdit;

    @Bind(R.id.book_input_isbn)
    EditText mIsbnEdit;

    @Bind(R.id.book_input_cover_image)
    ImageView mImageEdit;

    @Bind(R.id.book_input_cover)
    Button mCoverButton;

    String imageFileName;

    // Basic Activity Tasks
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.book_input);

        ButterKnife.bind(this);

        // set ToolBar
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            long bookId = extras.getLong(EXTRA_BOOK_ID, -1);
            if (bookId > 0) {
                mBookId = bookId;
                getSupportLoaderManager().initLoader(1, null, this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_BARCODE:
                // Handle Barcode
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        Barcode barcode = data.getParcelableExtra(BarcodeActivity.BARCODE_OBJECT);
                        mIsbnEdit.setText(barcode.displayValue);
                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                // Handle Image Capture
                if (resultCode == RESULT_OK) {
                    try {
                        File f = getImageFile(imageFileName);
                        Log.d(TAG, "getting picture from " + f.getPath());
                        DisplayMetrics metrics = getResources().getDisplayMetrics();
                        int densityDpi = (int)(metrics.density * 96);
                        Picasso.with(this).load(f).resize(densityDpi, densityDpi).centerInside().into(mImageEdit);
                    } catch (Exception ex) {
                        Log.e(TAG, "cannot open file", ex);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // ToolBar option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                save();
                return true;
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // Storing result
    private void save() {
        Book book = new Book.Builder()
                .setAuthor(mAuthorEdit.getText().toString())
                .setTitle(mTitleEdit.getText().toString())
                .setIsbn(mIsbnEdit.getText().toString()).build();
        getContentResolver().insert(
                DatabaseProvider.getUri(DatabaseProvider.BOOKS),
                book.getContentValues());
        finish();
    }

    // Open Camera for taking image of Book Cover
    @OnClick(R.id.book_input_cover)
    public void takePhoto(View view) {
        int cameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissions = 0;
        permissions |= (cameraPermission == PackageManager.PERMISSION_GRANTED) ? 0 : 1;
        permissions |= (writePermission == PackageManager.PERMISSION_GRANTED) ? 0 : 2;

        switch (permissions) {
            case 1:
                requestPermissions(
                        view,
                        TAKE_PHOTO_PERMISSIONS,
                        Manifest.permission.CAMERA);
                break;
            case 2:
                requestPermissions(
                        view,
                        TAKE_PHOTO_PERMISSIONS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
            case 3:
                requestPermissions(
                        view,
                        TAKE_PHOTO_PERMISSIONS,
                        Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
            default:
                dispatchTakePictureIntent();
        }
    }

    // Open Barcode scanner
    @OnClick(R.id.fab)
    public void click(View view) {
        int cameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(view, BARCODE_PERMISSIONS, Manifest.permission.CAMERA, Manifest.permission.CAMERA);
        } else {
            startActivityForResult(new Intent(this, BarcodeActivity.class), REQUEST_BARCODE);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
            if (photoFile != null) {
                Log.d(TAG, "taking picture to store into " + photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestPermissions(final View view, final int request, final String... permissions) {

        final String[] ungiven = autoPermission(request, permissions);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(EditBookActivity.this, ungiven, request);
            }
        };

        Snackbar.make(view, "camera pls",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("ok", listener)
                .show();
    }

    /**
     * Grant permissions immediately (no user input required) for items, that doesn't need users approval
     *
     * @param permissions
     */
    private String[] autoPermission(final int request, final String... permissions) {

        ArrayList<String> ungivenPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                ActivityCompat.requestPermissions(this, permissions, request);
            } else {
                ungivenPermissions.add(permission);
            }
        }

        return ungivenPermissions.toArray(new String[ungivenPermissions.size()]);
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case TAKE_PHOTO_PERMISSIONS:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                    return;
                }

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Multitracker sample")
                        .setMessage("no camera permission")
                        .setPositiveButton("OK", listener)
                        .show();
                break;
            case BARCODE_PERMISSIONS:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(this, BarcodeActivity.class), REQUEST_BARCODE);
                    return;
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, imageFileName + ".jpg");
    }

    private File getImageFile(String imageFileName) throws IOException {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, imageFileName + ".jpg");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DatabaseProvider.getUriWithId(DatabaseProvider.BOOK_ID, mBookId), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 1) {
            Book book = new Book(data);
            binding.setBook(book);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
