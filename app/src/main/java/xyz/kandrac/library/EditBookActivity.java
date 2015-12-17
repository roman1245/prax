package xyz.kandrac.library;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.DatabaseStoreUtils;
import xyz.kandrac.library.model.obj.Author;
import xyz.kandrac.library.model.obj.Book;
import xyz.kandrac.library.model.obj.Publisher;
import xyz.kandrac.library.net.BookResponse;
import xyz.kandrac.library.net.GoogleBooksUtils;
import xyz.kandrac.library.net.OkHttpConfigurator;
import xyz.kandrac.library.utils.DisplayUtils;

/**
 * Created by VizGhar on 11.10.2015.
 */
public class EditBookActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, Callback<BookResponse> {

    public static final String EXTRA_BOOK_ID = "book_id_extra";
    public static final String EXTRA_WISH_LIST = "wish_list_extra";

    private static final String TAG = EditBookActivity.class.getName();

    public static final String SAVE_STATE_FILE_NAME = "save_state_file_name";

    public static final int BOOK_LOADER = 1;

    private Long mBookId;
    private boolean mToWishList;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_BARCODE = 2;

    private static final int TAKE_PHOTO_PERMISSIONS = 2;
    private static final int BARCODE_PERMISSIONS = 3;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.book_input_author)
    AutoCompleteTextView mAuthorEdit;

    @Bind(R.id.book_input_publisher)
    AutoCompleteTextView mPublisherEdit;

    @Bind(R.id.book_input_title)
    EditText mTitleEdit;

    @Bind(R.id.book_input_subtitle)
    EditText mSubitleEdit;

    @Bind(R.id.book_input_isbn)
    EditText mIsbnEdit;

    @Bind(R.id.parallax_cover_image)
    ImageView mImageEdit;

    @Bind(R.id.book_input_cover)
    Button mCoverButton;

    @Bind(R.id.book_input_description_edit)
    EditText mDescritpion;

    String imageFileName;
    String imageUrl;

    // Basic Activity Tasks
    @SuppressWarnings("SimplifiableConditionalExpression")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_input);

        ButterKnife.bind(this);

        // set ToolBar
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        Bundle extras = getIntent().getExtras();

        mBookId = (extras != null) ? extras.getLong(EXTRA_BOOK_ID, -1) : -1;
        mToWishList = (extras != null) ? extras.getBoolean(EXTRA_WISH_LIST) : false;

        if (mBookId > 0) {
            getSupportLoaderManager().initLoader(BOOK_LOADER, null, this);
        }

        setAuthorAdapter();
        setPublisherAdapter();
    }

    private void setAuthorAdapter() {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{Contract.Authors.AUTHOR_NAME},
                new int[]{android.R.id.text1},
                0);
        mAuthorEdit.setAdapter(adapter);

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getAuthorCursor(str);
            }
        });

        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(Contract.Authors.AUTHOR_NAME);
                return cur.getString(index);
            }
        });
    }

    private void setPublisherAdapter() {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{Contract.Publishers.PUBLISHER_NAME},
                new int[]{android.R.id.text1},
                0);
        mPublisherEdit.setAdapter(adapter);

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getPublisherCursor(str);
            }
        });

        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(Contract.Publishers.PUBLISHER_NAME);
                return cur.getString(index);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_BARCODE:
                // Handle Barcode
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        String barcode = data.getStringExtra(BarcodeActivity.BARCODE_TEXT);
                        mIsbnEdit.setText(barcode);
                        String searchQuery = GoogleBooksUtils.getSearchQuery(GoogleBooksUtils.QUERY_ISBN, barcode);

                        OkHttpConfigurator
                                .getInstance()
                                .getApi()
                                .getBooksByQuery(searchQuery)
                                .enqueue(this);
                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                // Handle Image Capture
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "display image : " + imageFileName);
                    try {
                        DisplayUtils.displayScaledImage(this, imageFileName, mImageEdit);
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

    // Storing result synchronously
    private void save() {

        String authorsReadable = mAuthorEdit.getText().toString();

        Publisher publisher = new Publisher.Builder()
                .setName(mPublisherEdit.getText().toString())
                .build();

        String[] authorsSplit = TextUtils.split(authorsReadable, ",");

        Author[] authors = new Author[authorsSplit.length];
        for (int i = 0; i < authorsSplit.length; i++) {
            String authorName = authorsSplit[i].trim();
            authors[i] = new Author.Builder().setName(authorName).build();
        }

        Book book = new Book.Builder()
                .setId(mBookId)
                .setPublisher(publisher)
                .setAuthors(authors)
                .setTitle(mTitleEdit.getText().toString())
                .setSubtitle(mSubitleEdit.getText().toString())
                .setIsbn(mIsbnEdit.getText().toString())
                .setImageFilePath(imageFileName)
                .setDescription(mDescritpion.getText().toString())
                .setImageUrlPath(imageUrl)
                .setPublisherReadable(mPublisherEdit.getText().toString())
                .setAuthorsRedable(authorsReadable)
                .setWish(mToWishList)
                .build();

        DatabaseStoreUtils.saveBook(getContentResolver(), book);
        finish();
    }

    // Open Camevra for taking image of Book Cover
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
                        R.string.edit_book_take_photo_permission,
                        TAKE_PHOTO_PERMISSIONS,
                        Manifest.permission.CAMERA);
                break;
            case 2:
                requestPermissions(
                        view,
                        R.string.edit_book_take_photo_permission,
                        TAKE_PHOTO_PERMISSIONS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
            case 3:
                requestPermissions(
                        view,
                        R.string.edit_book_take_photo_permission,
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
            requestPermissions(
                    view,
                    R.string.edit_book_barcode_permission,
                    BARCODE_PERMISSIONS,
                    Manifest.permission.CAMERA);
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
                Uri uri = Uri.fromFile(photoFile);
                Log.d(TAG, "taking picture to store into " + uri.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Log.d(TAG, "No application for taking photo available");
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestPermissions(final View view, final int message, final int request, final String... permissions) {

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(EditBookActivity.this, permissions, request);
            }
        };

        Snackbar.make(view, message,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_ok, listener)
                .show();
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
            case TAKE_PHOTO_PERMISSIONS: {
                boolean execute = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        execute = false;
                    }
                }

                if (execute) {
                    dispatchTakePictureIntent();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(R.string.edit_book_no_permission_title)
                        .setMessage(R.string.edit_book_no_permission)
                        .setPositiveButton(R.string.action_ok, null)
                        .show();
                break;
            }
            case BARCODE_PERMISSIONS: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(this, BarcodeActivity.class), REQUEST_BARCODE);
                    return;
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle(R.string.edit_book_no_permission_title)
                            .setMessage(R.string.edit_book_no_permission)
                            .setPositiveButton(R.string.action_ok, null)
                            .show();
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
        String fileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File result = new File(storageDir, fileName + ".jpg");
        imageFileName = result.getPath();
        Log.d(TAG, "Created file : " + imageFileName);
        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case BOOK_LOADER:
                return new CursorLoader(this, Contract.Books.buildBookUri(mBookId), null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();

        switch (id) {
            case BOOK_LOADER:
                // bind book data
                if (data.getCount() == 1) {
                    Book book = new Book(data);
                    setBook(book);
                }
                break;
            default:
        }
    }

    private void setBook(Book book) {
        mAuthorEdit.setText(book.authorsReadable);
        mPublisherEdit.setText(book.publisherReadable);
        mTitleEdit.setText(book.title);
        mSubitleEdit.setText(book.subtitle);
        mIsbnEdit.setText(book.isbn);
        mDescritpion.setText(book.description);
        mToWishList = book.wish;

        if (book.imageFilePath != null) {
            File imageFile = new File(book.imageFilePath);
            if (imageFile.exists()) {
                Picasso.with(this).load(imageFile).into(mImageEdit);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public Cursor getAuthorCursor(CharSequence str) {
        String select = Contract.Authors.AUTHOR_NAME + " LIKE ? ";
        String[] selectArgs = {"%" + str + "%"};
        String[] contactsProjection = new String[]{BaseColumns._ID, Contract.Authors.AUTHOR_NAME};

        return getContentResolver().query(Contract.Authors.CONTENT_URI, contactsProjection, select, selectArgs, null);
    }

    public Cursor getPublisherCursor(CharSequence str) {
        String select = Contract.Publishers.PUBLISHER_NAME + " LIKE ? ";
        String[] selectArgs = {"%" + str + "%"};
        String[] contactsProjection = new String[]{BaseColumns._ID, Contract.Publishers.PUBLISHER_NAME};

        return getContentResolver().query(Contract.Publishers.CONTENT_URI, contactsProjection, select, selectArgs, null);
    }

    @Override
    public void onResponse(Response<BookResponse> response, Retrofit retrofit) {
        if (response.isSuccess()) {
            BookResponse s = response.body();
            if (s.totalItems > 0) {
                for (BookResponse.Book book : s.books) {
                    if (book.volumeInfo != null) {
                        mPublisherEdit.setText(book.volumeInfo.publisher);
                        mTitleEdit.setText(book.volumeInfo.title);
                        mDescritpion.setText(book.volumeInfo.description);
                        mAuthorEdit.setText(book.volumeInfo.authors[0]);

                        if (book.volumeInfo.imageLinks != null) {
                            imageUrl = book.volumeInfo.imageLinks.thumbnail;
                            Picasso.with(this).load(imageUrl).into(mImageEdit);
                        }
                    }
                }
            }
        } else {
            Log.d(TAG, "Error parsing response");
        }
    }

    @Override
    public void onFailure(Throwable t) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_STATE_FILE_NAME, imageFileName);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageFileName = savedInstanceState.getString(SAVE_STATE_FILE_NAME);
    }
}
