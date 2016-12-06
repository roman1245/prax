package xyz.kandrac.library;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.kandrac.barcode.BarcodeActivity;
import xyz.kandrac.library.api.RetrofitConfig;
import xyz.kandrac.library.api.library.LibraryResponse;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.DatabaseStoreUtils;
import xyz.kandrac.library.model.obj.Author;
import xyz.kandrac.library.model.obj.Book;
import xyz.kandrac.library.model.obj.Library;
import xyz.kandrac.library.model.obj.Publisher;
import xyz.kandrac.library.utils.BookCursorAdapter;
import xyz.kandrac.library.utils.ConnectivityUtils;
import xyz.kandrac.library.utils.DisplayUtils;
import xyz.kandrac.library.utils.LogUtils;
import xyz.kandrac.library.utils.MediaUtils;
import xyz.kandrac.library.views.DummyTextWatcher;

/**
 * Book Adding/Editing
 * <p>
 * Created by VizGhar on 11.10.2015.
 */
public class EditBookActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String TAG = EditBookActivity.class.getName();

    // Activity extras
    public static final String EXTRA_BOOK_ID = "book_id_extra";
    public static final String EXTRA_WISH_LIST = "wish_list_extra";
    public static final String EXTRA_BORROWED_TO_ME = "borrowed_to_me_extra";

    // Save instance state constants
    private static final String SAVE_STATE_FILE_NAME = "save_state_file_name";
    private static final String SAVE_STATE_BOOK_ID = "save_state_book_id";
    private static final String SAVE_STATE_WISH_LIST = "save_state_wish";
    private static final String SAVE_STATE_BORROWED_TO_ME = "save_state_borrowed_to_me";
    private static final String SAVE_STATE_ORIGINAL_FILE = "save_state_original_file";

    // Loaders
    public static final int LOADER_BOOK = 1;
    public static final int LOADER_AUTHOR = 2;
    public static final int LOADER_PUBLISHER = 3;
    public static final int LOADER_LIBRARY = 4;

    // Content menus
    public static final int CONTENT_MENU_PHOTO = 123;
    public static final int CONTENT_MENU_GALLERY = 124;

    // Requests to other activities
    private static final int REQUEST_IMAGE_CAPTURE = 201;
    private static final int REQUEST_BARCODE = 202;
    private static final int REQUEST_PICK_IMAGE = 203;

    // Permission requests
    private static final int PERMISSION_TAKE_PHOTO = 301;
    private static final int PERMISSION_BARCODE = 302;
    private static final int PERMISSION_PICK_CONTACT = 303;
    private static final int PERMISSION_GALLERY = 304;

    // Globals
    private long mBookId;
    private boolean mToWishList;
    private boolean mBorrowedToMe;
    private String imageFileName;
    private boolean mStartingActivityForResult = false;

    private Toolbar toolbar;
    private ImageView mOriginImage;
    private AutoCompleteTextView mOriginEdit;
    private AutoCompleteTextView mAuthorEdit;
    private AutoCompleteTextView mPublisherEdit;
    private ImageView mLibraryImage;
    private AutoCompleteTextView mLibraryEdit;
    private EditText mTitleEdit;
    private EditText mSubtitleEdit;
    private EditText mIsbnEdit;
    private Button mScanButton;
    private ImageView mImageEdit;
    private EditText mDescription;
    private EditText mPublished;

    // Basic Activity Tasks
    @SuppressWarnings("SimplifiableConditionalExpression")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_book_edit);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mOriginImage = (ImageView) findViewById(R.id.book_input_origin_image);
        mOriginEdit = (AutoCompleteTextView) findViewById(R.id.book_input_origin);
        mAuthorEdit = (AutoCompleteTextView) findViewById(R.id.book_input_author);
        mPublisherEdit = (AutoCompleteTextView) findViewById(R.id.book_input_publisher);
        mLibraryImage = (ImageView) findViewById(R.id.book_input_library_icon);
        mLibraryEdit = (AutoCompleteTextView) findViewById(R.id.book_input_library);
        mTitleEdit = (EditText) findViewById(R.id.book_input_title);
        mSubtitleEdit = (EditText) findViewById(R.id.book_input_subtitle);
        mIsbnEdit = (EditText) findViewById(R.id.book_input_isbn);
        mScanButton = (Button) findViewById(R.id.book_input_scan);
        mImageEdit = (ImageView) findViewById(R.id.parallax_cover_image);
        mDescription = (EditText) findViewById(R.id.book_input_description_edit);
        mPublished = (EditText) findViewById(R.id.book_input_published);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // set ToolBar
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        // handle extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mBookId = extras.getLong(EXTRA_BOOK_ID, 0);
            mToWishList = extras.getInt(EXTRA_WISH_LIST) == BookCursorAdapter.TRUE;
            mBorrowedToMe = extras.getInt(EXTRA_BORROWED_TO_ME) == BookCursorAdapter.TRUE;
        } else {
            mBookId = 0L;
            mToWishList = false;
            mBorrowedToMe = false;
        }

        if (mBorrowedToMe) {
            requestPickContactPermission();
        }

        mOriginEdit.setVisibility(mBorrowedToMe ? View.VISIBLE : View.GONE);
        mOriginImage.setVisibility(mBorrowedToMe ? View.VISIBLE : View.GONE);
        fab.setOnClickListener(this);
        mImageEdit.setOnClickListener(this);
        mScanButton.setOnClickListener(this);

        if (mBookId > 0) {
            setTitle(R.string.title_edit_book);
            getSupportLoaderManager().initLoader(LOADER_BOOK, null, this);
            getSupportLoaderManager().initLoader(LOADER_AUTHOR, null, this);
            getSupportLoaderManager().initLoader(LOADER_PUBLISHER, null, this);
            getSupportLoaderManager().initLoader(LOADER_LIBRARY, null, this);
        } else {
            setTitle(R.string.title_add_new_book);
        }

        mIsbnEdit.addTextChangedListener(new DummyTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mIsbnEdit.getText().length() == 0) {
                    mScanButton.setText(R.string.edit_book_scan);
                } else {
                    mScanButton.setText(R.string.edit_book_search);
                }
            }
        });

        registerForContextMenu(mImageEdit);

        checkLibrariesPreferences();

        // set adapters for autocomplete fields
        setAdapter(Contract.Authors.CONTENT_URI, Contract.Authors.AUTHOR_NAME, mAuthorEdit);
        setAdapter(Contract.Publishers.CONTENT_URI, Contract.Publishers.PUBLISHER_NAME, mPublisherEdit);
        setAdapter(Contract.Libraries.CONTENT_URI, Contract.Libraries.LIBRARY_NAME, mLibraryEdit);
        setAdapter(ContactsContract.Contacts.CONTENT_URI, ContactsContract.Contacts.DISPLAY_NAME, mOriginEdit);
    }

    private void requestPickContactPermission() {
        int pickContactPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (pickContactPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    mOriginEdit,
                    R.string.edit_book_barcode_contact_permission,
                    PERMISSION_PICK_CONTACT,
                    Manifest.permission.READ_CONTACTS);
        }
    }

    public void checkLibrariesPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = sharedPref.getBoolean(SettingsFragment.KEY_PREF_LIBRARY_ENABLED, true);
        String name = sharedPref.getString(SettingsFragment.KEY_PREF_LIBRARY_DEFAULT, "");
        if (!enabled) {
            mLibraryEdit.setVisibility(View.GONE);
            mLibraryImage.setVisibility(View.GONE);
        } else {
            mLibraryEdit.setText(name);
        }
    }

    /**
     * Sets adapter for given autocomplete text view
     *
     * @param uri           to get data from
     * @param displayColumn identifier
     * @param target        AutoCompleteTextView
     */
    private void setAdapter(final Uri uri, final String displayColumn, final AutoCompleteTextView target) {

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{displayColumn},
                new int[]{android.R.id.text1},
                0);

        target.setAdapter(adapter);

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                String select = displayColumn + " LIKE ? ";
                String[] selectArgs = {"%" + str + "%"};
                String[] projection = new String[]{BaseColumns._ID, displayColumn};
                return getContentResolver().query(uri, projection, select, selectArgs, null);
            }
        });

        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(displayColumn);
                return cur.getString(index);
            }
        });
    }

    private void searchIsbn(String barcode) {

        if (!ConnectivityUtils.isConnected(this)) {
            Toast.makeText(this, R.string.not_connected_book_info, Toast.LENGTH_LONG).show();
            return;
        }

        final ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.parsing_book_details), getString(R.string.parsing_book_details_message));

        RetrofitConfig.getInstance().getLibraryApi().getBookByIsbn(barcode).enqueue(new Callback<LibraryResponse>() {
            @Override
            public void onResponse(Call<LibraryResponse> call, Response<LibraryResponse> response) {
                if (response.isSuccessful()) {
                    LibraryResponse book = response.body();
                    mTitleEdit.setText(book.title);
                    mSubtitleEdit.setText(book.subtitle);
                    mAuthorEdit.setText(book.authors);
                    mPublisherEdit.setText(book.publisher);
                    mPublished.setText(book.published);
                } else {
                    Toast.makeText(EditBookActivity.this, R.string.communication_error, Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<LibraryResponse> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(EditBookActivity.this, R.string.communication_error, Toast.LENGTH_LONG).show();
                LogUtils.e(TAG, "retrofit error", t);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mStartingActivityForResult = false;
        switch (requestCode) {
            case REQUEST_BARCODE:
                // Handle Barcode
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String barcode = data.getStringExtra(BarcodeActivity.BARCODE_TEXT);
                        mIsbnEdit.setText(barcode);
                        searchIsbn(barcode);
                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                // Handle Image Capture
                if (resultCode == RESULT_OK) {
                    refreshImage();
                }
                break;
            case REQUEST_PICK_IMAGE:
                // Handle image pick from gallery
                if (resultCode == Activity.RESULT_OK) {
                    imageFileName = MediaUtils.x(data, this);
                    refreshImage();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void takePhoto() {
        int cameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissions = 0;
        permissions |= (cameraPermission == PackageManager.PERMISSION_GRANTED) ? 0 : 1;
        permissions |= (writePermission == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT > 18) ? 0 : 2;

        switch (permissions) {
            case 1:
                requestPermissions(
                        mImageEdit,
                        R.string.edit_book_take_photo_permission,
                        PERMISSION_TAKE_PHOTO,
                        Manifest.permission.CAMERA);
                break;
            case 2:
                requestPermissions(
                        mImageEdit,
                        R.string.edit_book_take_photo_permission,
                        PERMISSION_TAKE_PHOTO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
            case 3:
                requestPermissions(
                        mImageEdit,
                        R.string.edit_book_take_photo_permission,
                        PERMISSION_TAKE_PHOTO,
                        Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
            default:
                dispatchTakePictureIntent();
        }
    }

    public void takeFromGallery() {

        int externalPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (externalPermission == PackageManager.PERMISSION_GRANTED) {
            dispatchGalleryIntent();
        } else {
            requestPermissions(
                    mImageEdit,
                    R.string.edit_book_gallery_permission,
                    PERMISSION_GALLERY,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    // Open Barcode scanner
    public void save() {
        String isbn = mIsbnEdit.getText().toString();

        if (mBookId != 0 || TextUtils.isEmpty(isbn)) {
            saveConfirmed();
            return;
        }

        Cursor c = getContentResolver().query(Contract.Books.buildBookIsbnUri(mIsbnEdit.getText().toString()), null, null, null, null);

        if (c != null && c.getCount() > 0) {
            c.close();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.edit_book_already_exists)
                    .setMessage(R.string.edit_book_already_exists_message)
                    .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveConfirmed();
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(true)
                    .show();
        } else {
            saveConfirmed();
        }
    }

    public void saveConfirmed() {
        String authorsReadable = mAuthorEdit.getText().toString();

        Publisher publisher = new Publisher.Builder()
                .setName(mPublisherEdit.getText().toString())
                .build();

        Library library = new Library.Builder()
                .setName(mLibraryEdit.getText().toString())
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
                .setLibrary(library)
                .setAuthors(authors)
                .setTitle(mTitleEdit.getText().toString())
                .setSubtitle(mSubtitleEdit.getText().toString())
                .setIsbn(mIsbnEdit.getText().toString())
                .setImageFilePath((String) mImageEdit.getTag())
                .setDescription(mDescription.getText().toString())
                .setWish(mToWishList)
                .setBorrowedToMe(mBorrowedToMe)
                .setPublished(mPublished.getText().toString())
                .setUpdatedAt(System.currentTimeMillis())
                .build();

        long bookId = DatabaseStoreUtils.saveBook(getContentResolver(), book);

        if (mBorrowedToMe) {
            final long dateFrom = new Date(System.currentTimeMillis()).getTime();

            ContentValues borrowContentValues = new ContentValues();
            borrowContentValues.put(Contract.BorrowMeInfoColumns.BORROW_DATE_BORROWED, dateFrom);
            borrowContentValues.put(Contract.BorrowMeInfoColumns.BORROW_NAME, mOriginEdit.getText().toString());

            getContentResolver().insert(Contract.Books.buildBorrowedToMeInfoUri(bookId), borrowContentValues);
        }

        finish();
    }

    public void scan(View view) {
        if (mIsbnEdit.getText().length() == 0) {
            int cameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        view,
                        R.string.edit_book_barcode_permission,
                        PERMISSION_BARCODE,
                        Manifest.permission.CAMERA);
            } else {
                startActivityForResult(new Intent(this, BarcodeActivity.class), REQUEST_BARCODE);
            }
        } else {
            searchIsbn(mIsbnEdit.getText().toString());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.pick_image_select);
        menu.add(0, CONTENT_MENU_PHOTO, 0, R.string.pick_image_photo);
        menu.add(0, CONTENT_MENU_GALLERY, 0, R.string.pick_image_gallery);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CONTENT_MENU_PHOTO: {
                takePhoto();
                break;
            }
            case CONTENT_MENU_GALLERY: {
                takeFromGallery();
                break;
            }
            default:
                return false;
        }
        return true;
    }

    private void dispatchTakePictureIntent() {
        // do not start image capture if another activity is about to run
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri uri = FileProvider.getUriForFile(this, BuildConfig.FILE_PROVIDER_AUTHORITY, photoFile);
                takePictureIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            LogUtils.d(TAG, "No application for taking photo available");
        }
    }

    private void dispatchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                LogUtils.d(TAG, "Taking photo from gallery");
                startActivityForResult(intent, REQUEST_PICK_IMAGE);
            }
        } else {
            LogUtils.d(TAG, "No gallery application");
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "SnackBar" message of why the permission is needed then
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
            case PERMISSION_TAKE_PHOTO: {
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
            case PERMISSION_BARCODE: {
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
            case PERMISSION_GALLERY: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchGalleryIntent();
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

    /**
     * Create unique file name for storing image file from camera
     *
     * @return file created
     */
    private File createImageFile() {
        // Create an image file name
        @SuppressWarnings("SpellCheckingInspection")
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String fileName = "book_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File result = new File(storageDir, fileName + ".jpg");
        imageFileName = result.getAbsolutePath();
        return result;
    }

    /**
     * Try to set and display image that should be visible
     */
    private void refreshImage() {
        if (imageFileName == null) {
            return;
        }

        try {
            DisplayUtils.resizeImageFile(new File(imageFileName), 1024, 60);
            DisplayUtils.displayScaledImage(this, imageFileName, mImageEdit);
            mImageEdit.setTag(imageFileName);
        } catch (OutOfMemoryError ex) {
            Toast.makeText(this, R.string.edit_book_save_image_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_BOOK:
                return new CursorLoader(this, Contract.Books.buildBookUri(mBookId), null, null, null, null);
            case LOADER_AUTHOR:
                return new CursorLoader(
                        this,
                        Contract.Books.buildBookWithAuthorUri(mBookId),
                        new String[]{
                                Contract.Authors.AUTHOR_NAME
                        },
                        null,
                        null,
                        null);
            case LOADER_PUBLISHER:
                return new CursorLoader(
                        this,
                        Contract.Books.buildBookPublisherUri(mBookId),
                        new String[]{
                                Contract.Publishers.PUBLISHER_NAME
                        },
                        null,
                        null,
                        null);
            case LOADER_LIBRARY:
                return new CursorLoader(
                        this,
                        Contract.Books.buildBookLibraryUri(mBookId),
                        new String[]{
                                Contract.Libraries.LIBRARY_NAME
                        },
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();

        switch (id) {
            case LOADER_BOOK:
                bindBook(data);
                break;

            case LOADER_AUTHOR:
                bindAuthors(data);
                break;

            case LOADER_PUBLISHER:
                bindPublisher(data);
                break;

            case LOADER_LIBRARY:
                bindLibrary(data);
                break;

            default:
        }
    }

    /**
     * Bind book to view
     *
     * @param data to bind
     */
    private void bindBook(Cursor data) {
        if (data.getCount() != 1) {
            return;
        }

        data.moveToFirst();

        String title = data.getString(data.getColumnIndex(Contract.Books.BOOK_TITLE));
        String subtitle = data.getString(data.getColumnIndex(Contract.Books.BOOK_SUBTITLE));
        String isbn = data.getString(data.getColumnIndex(Contract.Books.BOOK_ISBN));
        String description = data.getString(data.getColumnIndex(Contract.Books.BOOK_DESCRIPTION));
        String path = data.getString(data.getColumnIndex(Contract.Books.BOOK_IMAGE_FILE));
        String published = data.getString(data.getColumnIndex(Contract.Books.BOOK_PUBLISHED));
        boolean wish = data.getInt(data.getColumnIndex(Contract.Books.BOOK_WISH_LIST)) == 1;
        boolean borrowedToMe = data.getInt(data.getColumnIndex(Contract.Books.BOOK_BORROWED_TO_ME)) == 1;

        mTitleEdit.setText(title);
        mSubtitleEdit.setText(subtitle);
        mIsbnEdit.setText(isbn);
        mDescription.setText(description);
        mPublished.setText(published);
        mToWishList = wish;
        mBorrowedToMe = borrowedToMe;
        mImageEdit.setTag(path);

        if (path != null) {
            File imageFile = new File(path);
            if (imageFile.exists()) {
                Picasso.with(this).load(imageFile).into(mImageEdit);
            }
        }
    }

    private void bindAuthors(Cursor authorsCursor) {
        if (authorsCursor == null || authorsCursor.getCount() == 0 || !authorsCursor.moveToFirst()) {
            return;
        }

        String result = authorsCursor.getString(authorsCursor.getColumnIndex(Contract.Authors.AUTHOR_NAME));

        while (authorsCursor.moveToNext()) {
            result += ", " + authorsCursor.getString(authorsCursor.getColumnIndex(Contract.Authors.AUTHOR_NAME));
        }

        mAuthorEdit.setText(result);
    }

    private void bindPublisher(Cursor publisherCursor) {
        if (publisherCursor == null || publisherCursor.getCount() == 0 || !publisherCursor.moveToFirst()) {
            return;
        }

        String result = publisherCursor.getString(publisherCursor.getColumnIndex(Contract.Publishers.PUBLISHER_NAME));

        mPublisherEdit.setText(result);
    }

    private void bindLibrary(Cursor libraryCursor) {
        if (libraryCursor == null || libraryCursor.getCount() == 0 || !libraryCursor.moveToFirst()) {
            return;
        }

        String result = libraryCursor.getString(libraryCursor.getColumnIndex(Contract.Libraries.LIBRARY_NAME));

        mLibraryEdit.setText(result);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_STATE_FILE_NAME, imageFileName);
        outState.putLong(SAVE_STATE_BOOK_ID, mBookId);
        outState.putBoolean(SAVE_STATE_WISH_LIST, mToWishList);
        outState.putBoolean(SAVE_STATE_BORROWED_TO_ME, mBorrowedToMe);
        outState.putString(SAVE_STATE_ORIGINAL_FILE, (String) mImageEdit.getTag());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageFileName = savedInstanceState.getString(SAVE_STATE_FILE_NAME);
        mBookId = savedInstanceState.getLong(SAVE_STATE_BOOK_ID);
        mToWishList = savedInstanceState.getBoolean(SAVE_STATE_WISH_LIST);
        mBorrowedToMe = savedInstanceState.getBoolean(SAVE_STATE_BORROWED_TO_ME);
        String original = savedInstanceState.getString(SAVE_STATE_ORIGINAL_FILE);
        mImageEdit.setTag(original);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        if (!mStartingActivityForResult) {
            mStartingActivityForResult = true;
            super.startActivityForResult(intent, requestCode, options);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fab:
                save();
                break;
            case R.id.book_input_scan:
                scan(view);
                break;
            case R.id.parallax_cover_image:
                openContextMenu(view);
                break;
        }
    }
}
