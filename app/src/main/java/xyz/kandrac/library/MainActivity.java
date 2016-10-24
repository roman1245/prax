package xyz.kandrac.library;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import xyz.kandrac.library.billing.BillingSkus;
import xyz.kandrac.library.billing.util.IABKeyEncoder;
import xyz.kandrac.library.billing.util.IabException;
import xyz.kandrac.library.billing.util.IabHelper;
import xyz.kandrac.library.billing.util.IabResult;
import xyz.kandrac.library.billing.util.Inventory;
import xyz.kandrac.library.billing.util.Purchase;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.fragments.lists.AuthorBooksListFragment;
import xyz.kandrac.library.fragments.lists.BookListFragment;
import xyz.kandrac.library.fragments.lists.LibraryBooksListFragment;
import xyz.kandrac.library.fragments.lists.PublisherBooksListFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.firebase.FirebaseBook;
import xyz.kandrac.library.model.firebase.References;
import xyz.kandrac.library.utils.DisplayUtils;
import xyz.kandrac.library.utils.LogUtils;
import xyz.kandrac.library.views.DummyDrawerCallback;

/**
 * So far, this is launcher Activity of this Application. It contains side menu Navigation, title as
 * Action Bar and the content fragment replaced by currently selected item.
 *
 * @see NavigationView
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    public static final long WAIT_FOR_DOUBLE_CLICK_BACK = 3000;
    private static final String LOG_TAG = MainActivity.class.getName();

    // Loader constants. Ensure that fragments are using this constants and not the
    public static final int BOOK_LIST_LOADER = 1;
    public static final int AUTHOR_LIST_LOADER = 2;
    public static final int PUBLISHER_LIST_LOADER = 3;
    public static final int LIBRARY_LIST_LOADER = 4;
    public static final int BORROWED_BOOK_LIST_LOADER = 5;
    public static final int BORROWED_TO_ME_LIST_LOADER = 6;
    public static final int WISH_LIST_BOOK_LIST_LOADER = 7;

    public static final int WISH_COUNT = 8;
    public static final int MY_COUNT = 9;
    public static final int BORROWED_COUNT = 10;
    public static final int FROM_FRIENDS_COUNT = 11;

    public static final int SYNC_LOADER = 12;

    public static final int PURCHASE_DRIVE_REQUEST = 1241;

    public static final String PREFERENCE_PHOTOS_RESIZED = "photos_resized_preference_2";
    public static final String PREFERENCE_PHOTOS_REMOVED = "photos_removed_preference";

    private static final int RC_SIGN_IN = 115;

    private IabHelper mHelper;
    private FirebaseAuth mAuth;

    private NavigationView navigation;
    private DrawerLayout drawerLayout;

    private TextView userName;
    private TextView userMail;
    private ImageView userPhoto;

    private MenuItem lastChecked;
    private Fragment mShownFragment;
    private SearchView searchView;
    private ActionBar mActionBar;
    private long mLastFinishingBackClicked;
    private GoogleApiClient mGoogleApiClient;
    private boolean driveBought = false;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Base_NoStatusBar);
        super.onCreate(savedInstanceState);
        InitService.start(this);
        setContentView(R.layout.activity_main);

        // get views
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigation = (NavigationView) findViewById(R.id.main_navigation);
        View navigationHeader = navigation.getHeaderView(0);
        userName = (TextView) navigationHeader.findViewById(R.id.navigation_header_line1);
        userMail = (TextView) navigationHeader.findViewById(R.id.navigation_header_line2);
        userPhoto = (ImageView) navigationHeader.findViewById(R.id.navigation_header_profile_image);
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);


        // Action Bar settings
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(R.string.menu_books_mine);
        }

        // Navigation View settings
        navigation.setNavigationItemSelectedListener(this);
        navigationHeader.setOnClickListener(this);

        MenuItem booksMenuItem = navigation.getMenu().findItem(R.id.main_navigation_books);
        booksMenuItem.setChecked(true);
        lastChecked = booksMenuItem;

        checkLibrariesPreferences();

        // Content settings
        if (savedInstanceState != null) {
            return;
        }

        // setup first fragment
        mShownFragment = BookListFragment.getInstance();
        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, mShownFragment)
                .commit();

        // hide keyboard if drawer opened
        drawerLayout.addDrawerListener(new DummyDrawerCallback() {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (searchView != null) {
                    searchView.clearFocus();
                }
            }
        });

        removeUnusedPhotosIfNeeded();
        resizePhotosIfNeeded();

        getLoaderManager().initLoader(WISH_COUNT, null, this);
        getLoaderManager().initLoader(MY_COUNT, null, this);
        getLoaderManager().initLoader(BORROWED_COUNT, null, this);
        getLoaderManager().initLoader(FROM_FRIENDS_COUNT, null, this);

        configureIAB();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_TOKEN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    userName.setText(user.getDisplayName());
                    userMail.setText(user.getEmail());
                    Picasso.with(MainActivity.this).load(user.getPhotoUrl()).into(userPhoto);
                } else {
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    /**
     * In App Billing configuration
     */
    private void configureIAB() {
        String base64EncodedPublicKey = IABKeyEncoder.getKey();

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    LogUtils.d(LOG_TAG, "Problem setting up In-app Billing: " + result);
                }
                LogUtils.d(LOG_TAG, "IAB is setup - getting info about paid content");
                setupPaidContent();
            }
        });
    }

    private void setupPaidContent() {
        try {
            ArrayList<String> skus = new ArrayList<>();
            skus.add(BillingSkus.getDriveSku());
            mHelper.queryInventoryAsync(true, skus, null, new IabHelper.QueryInventoryFinishedListener() {
                @Override
                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                    if (result.isFailure()) {
                        LogUtils.d(LOG_TAG, "error getting inventory: " + result);
                    } else {
                        driveBought = inventory.hasPurchase(BillingSkus.getDriveSku());
                        evaluatePurchases();
                    }
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        InitService.start(this, InitService.ACTION_CLEAR_DATABASE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogUtils.d(LOG_TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    public void checkLibrariesPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = sharedPref.getBoolean(SettingsFragment.KEY_PREF_LIBRARY_ENABLED, true);
        MenuItem librariesMenuItem = navigation.getMenu().findItem(R.id.main_navigation_libraries);
        if (enabled) {
            librariesMenuItem.setVisible(true);
        } else {
            librariesMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.purchase_test_purchased:
                BillingSkus.getInstance().setDebugAlternative(BillingSkus.TEST_PURCHASED);
                Toast.makeText(this, BillingSkus.TEST_PURCHASED + " set", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.purchase_test_cancelled:
                BillingSkus.getInstance().setDebugAlternative(BillingSkus.TEST_CANCELLED);
                Toast.makeText(this, BillingSkus.TEST_CANCELLED + " set", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.purchase_test_refunded:
                BillingSkus.getInstance().setDebugAlternative(BillingSkus.TEST_REFUNDED);
                Toast.makeText(this, BillingSkus.TEST_REFUNDED + " set", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.purchase_test_unavailable:
                BillingSkus.getInstance().setDebugAlternative(BillingSkus.TEST_UNAVAILABLE);
                Toast.makeText(this, BillingSkus.TEST_UNAVAILABLE + " set", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.purchase_consume:
                // querying inventory on main thread is dangerous, but this is only visible for debug
                try {
                    mHelper.consumeAsync(mHelper.queryInventory().getPurchase(BillingSkus.getDriveSku()), new IabHelper.OnConsumeFinishedListener() {
                        @Override
                        public void onConsumeFinished(Purchase purchase, IabResult result) {
                            Toast.makeText(MainActivity.this, "consumed = " + result.isSuccess(), Toast.LENGTH_SHORT).show();
                            evaluatePurchases();
                        }
                    });
                } catch (IabException | IabHelper.IabAsyncInProgressException ex) {
                    Toast.makeText(this, "consumed = false", Toast.LENGTH_SHORT).show();
                    evaluatePurchases();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // Associate searchable configuration with the SearchView
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (mShownFragment instanceof Searchable) {
                    ((Searchable) mShownFragment).requestSearch(query);
                    return true;
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigation)) {
            // close drawer first (do not give focus to search)
            drawerLayout.closeDrawers();
        } else if (!searchView.isIconified()) {
            // close search second
            searchView.setIconified(true);
            if (!searchView.isIconified()) {
                // first iconify
                searchView.setIconified(true);
            }
        } else {
            // don't close immediately
            long currentTime = System.currentTimeMillis();
            if (currentTime > mLastFinishingBackClicked + WAIT_FOR_DOUBLE_CLICK_BACK) {
                mLastFinishingBackClicked = currentTime;
                Toast.makeText(this, R.string.press_again_to_leave, Toast.LENGTH_SHORT).show();
            } else {
                // take standard action otherwise
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        // if last clicked menu item is same as current, do nothing
        if (lastChecked == menuItem) {
            drawerLayout.closeDrawers();
            return false;
        }

        // Get fragment to show
        int menuItemId = menuItem.getItemId();
        Fragment fragmentToShow = null;

        switch (menuItemId) {
            case R.id.main_navigation_books:
                fragmentToShow = BookListFragment.getInstance();
                break;
            case R.id.main_navigation_borrowed:
                fragmentToShow = BookListFragment.getBorrowedBooksInstance();
                break;
            case R.id.main_navigation_borrowed_to_me:
                fragmentToShow = BookListFragment.getBorrowedToMeBooksInstance();
                break;
            case R.id.main_navigation_wish_list:
                fragmentToShow = BookListFragment.getWishListBooksInstance();
                break;
            case R.id.main_navigation_authors:
                fragmentToShow = new AuthorBooksListFragment();
                break;
            case R.id.main_navigation_publishers:
                fragmentToShow = new PublisherBooksListFragment();
                break;
            case R.id.main_navigation_libraries:
                fragmentToShow = new LibraryBooksListFragment();
                break;
            case R.id.main_navigation_settings:
                fragmentToShow = new SettingsFragment();
                break;
            case R.id.main_navigation_drive:
                try {
                    mHelper.launchPurchaseFlow(this, BillingSkus.getDriveSku(), PURCHASE_DRIVE_REQUEST, new IabHelper.OnIabPurchaseFinishedListener() {
                        @Override
                        public void onIabPurchaseFinished(IabResult result, Purchase info) {
                            if (result.isFailure() && result.getResponse() != 7) {
                                LogUtils.d(LOG_TAG, "Error purchasing: " + result);
                            } else if (info.getSku().equals(BillingSkus.getDriveSku())) {
                                driveBought = true;
                                evaluatePurchases();
                            }
                        }
                    });
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
                return true;
        }

        if (fragmentToShow == null) {
            return false;
        }

        // close drawers and use replace fragment
        drawerLayout.closeDrawers();

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragmentToShow)
                .commit();

        if (fragmentToShow instanceof SettingsFragment) {
            searchView.setVisibility(View.GONE);
        } else {
            searchView.setVisibility(View.VISIBLE);
        }

        // change title
        if (mActionBar != null) {
            mActionBar.setTitle(menuItem.getTitle());
        }

        // remember
        mShownFragment = fragmentToShow;
        lastChecked.setChecked(false);
        menuItem.setChecked(true);
        lastChecked = menuItem;

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass on the activity result to the helper for handling
        if (mHelper.handleActivityResult(requestCode, resultCode, data)) {
            LogUtils.d(LOG_TAG, "onActivityResult handled by IABUtil.");
        } else if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (!result.isSuccess()) {
                Toast.makeText(this, R.string.sign_in_connection_error, Toast.LENGTH_SHORT).show();
            } else {
                firebaseAuthWithGoogle(result.getSignInAccount());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Remove unused photos from disk if they are not referenced in books table
     */
    private void removeUnusedPhotosIfNeeded() {
        long lastTimeRemoval = PreferenceManager.getDefaultSharedPreferences(this).getLong(PREFERENCE_PHOTOS_REMOVED, 0);

        if (System.currentTimeMillis() - lastTimeRemoval < 60_000 * 60 * 24) {
            return;
        }

        File imageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (imageDirectory != null) {
            File[] files = imageDirectory.listFiles();

            new AsyncTask<File, Integer, Void>() {

                @Override
                protected Void doInBackground(File... params) {

                    int count = 0;

                    for (File file : params) {
                        String filePath = file.getAbsolutePath();

                        // search for book with given file name
                        Cursor cursor = MainActivity.this.getContentResolver().query(
                                Contract.Books.CONTENT_URI,
                                new String[]{Contract.Books.BOOK_ID},
                                Contract.Books.BOOK_IMAGE_FILE + " = ?",
                                new String[]{filePath},
                                null
                        );

                        // remove book if found
                        if (cursor == null || cursor.getCount() == 0) {
                            if (file.delete()) {
                                count++;
                            }
                        } else {
                            cursor.close();
                        }
                    }

                    LogUtils.d(MainActivity.class.getSimpleName(), "deleted " + count + " files");

                    return null;
                }

            }.execute(files);
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(PREFERENCE_PHOTOS_REMOVED, System.currentTimeMillis()).apply();
    }

    /**
     * If resizing of photos was never invoked before, try to do so now. All images will be resized
     * to 1024 width with 60% quality. This will keep application size significantly lower.
     */
    private void resizePhotosIfNeeded() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().remove("photos_resized_preference").apply();
        boolean resized = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREFERENCE_PHOTOS_RESIZED, false);

        if (!resized) {

            File imageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (imageDirectory != null) {
                File[] files = imageDirectory.listFiles();

                if (files.length > 0) {

                    new AsyncTask<File, Integer, Void>() {

                        ProgressDialog dialog;

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            dialog = new ProgressDialog(MainActivity.this);
                            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            dialog.setMessage(getString(R.string.dialog_image_optimization_message));
                            dialog.setTitle(R.string.dialog_image_optimization_title);
                            dialog.setProgress(0);
                            dialog.show();
                        }

                        @Override
                        protected Void doInBackground(File... params) {
                            float part = 100f / params.length;
                            for (int i = 0; i < params.length; i++) {
                                DisplayUtils.resizeImageFile(params[i], 1024, 60);
                                publishProgress((int) (i * part));
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            dialog.dismiss();
                        }

                        @Override
                        protected void onProgressUpdate(Integer... values) {
                            super.onProgressUpdate(values);
                            dialog.setProgress(values[0]);
                        }

                    }.execute(files);
                }
            }
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PREFERENCE_PHOTOS_RESIZED, true).apply();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case WISH_COUNT:
                return new CursorLoader(this, Contract.Books.CONTENT_URI, new String[]{"count(*) as c"}, Contract.Books.BOOK_WISH_LIST + " = 1", null, null);
            case MY_COUNT:
                return new CursorLoader(this, Contract.Books.CONTENT_URI, new String[]{"count(*) as c"},
                        Contract.Books.BOOK_WISH_LIST + " = 0 AND " +
                                Contract.Books.BOOK_BORROWED + " = 0 AND " +
                                Contract.Books.BOOK_BORROWED_TO_ME + " = 0", null, null);
            case BORROWED_COUNT:
                return new CursorLoader(this, Contract.Books.CONTENT_URI, new String[]{"count(*) as c"}, Contract.Books.BOOK_BORROWED + " = 1", null, null);
            case FROM_FRIENDS_COUNT:
                return new CursorLoader(this, Contract.Books.CONTENT_URI, new String[]{"count(*) as c"}, Contract.Books.BOOK_BORROWED_TO_ME + " = 1", null, null);
            case SYNC_LOADER:
                return new CursorLoader(this, Contract.Special.TABLE_URI, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        @IdRes int viewId;
        switch (loader.getId()) {
            case WISH_COUNT: {
                viewId = R.id.main_navigation_wish_list;
                break;
            }
            case MY_COUNT: {
                viewId = R.id.main_navigation_books;
                break;
            }
            case BORROWED_COUNT: {
                viewId = R.id.main_navigation_borrowed;
                break;
            }
            case FROM_FRIENDS_COUNT: {
                viewId = R.id.main_navigation_borrowed_to_me;
                break;
            }
            case SYNC_LOADER: {

                // get user identifier
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth == null || auth.getCurrentUser() == null) {
                    return;
                }
                String userUid = auth.getCurrentUser().getUid();


                // store parsed data to database
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                // parse cursor data
                data.moveToFirst();
                do {
                    String id = data.getString(data.getColumnIndex(Contract.Books.BOOK_ID));
                    String title = data.getString(data.getColumnIndex(Contract.Books.BOOK_TITLE));
                    String isbn = data.getString(data.getColumnIndex(Contract.Books.BOOK_ISBN));
                    String description = data.getString(data.getColumnIndex(Contract.Books.BOOK_DESCRIPTION));
                    String subtitle = data.getString(data.getColumnIndex(Contract.Books.BOOK_SUBTITLE));
                    String published = data.getString(data.getColumnIndex(Contract.Books.BOOK_PUBLISHED));
                    String authors = data.getString(data.getColumnIndex(Contract.Authors.AUTHOR_NAME));
                    String publisher = data.getString(data.getColumnIndex(Contract.Publishers.PUBLISHER_NAME));

                    database.getReference()
                            .child(References.USERS_REFERENCE).child(userUid)
                            .child(References.BOOKS_REFERENCE).child(id)
                            .setValue(new FirebaseBook(title, id, isbn, description, subtitle, published, authors, publisher));

                } while (data.moveToNext());

                return;
            }
            default:
                return;
        }
        setActionViewTextFromCursor(viewId, data.moveToFirst() ? data.getString(0) : "0");
    }

    private void setActionViewTextFromCursor(@IdRes int viewId, String show) {
        if (show != null) {
            View actionView = navigation.getMenu().findItem(viewId).getActionView();
            TextView text = (TextView) actionView.findViewById(R.id.action_view);
            text.setText(show);
            text.setVisibility(View.VISIBLE);
        }
    }

    private void setMenuViewVisibility(@IdRes int menuItemId, boolean visible) {
        navigation.getMenu().findItem(menuItemId).setVisible(visible);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.navigation_header:
                if (mAuth.getCurrentUser() == null) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    private void evaluatePurchases() {
        if (!driveBought && mAuth.getCurrentUser() != null) {
            setMenuViewVisibility(R.id.main_navigation_drive, true);
        } else {
            setMenuViewVisibility(R.id.main_navigation_drive, false);
        }

        if (driveBought && mAuth.getCurrentUser() != null) {
            LogUtils.d(LOG_TAG, "starting sync");
            invokeSync();
        }
    }

    private void invokeSync() {
        getLoaderManager().initLoader(SYNC_LOADER, null, this);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, R.string.sign_in_connection_error, Toast.LENGTH_SHORT).show();
                        }
                        evaluatePurchases();
                    }
                });
    }
}