package xyz.kandrac.library;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import java.io.File;

import xyz.kandrac.library.billing.MultipleBillingItemAsyncTask;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.fragments.lists.AuthorBooksListFragment;
import xyz.kandrac.library.fragments.lists.BookListFragment;
import xyz.kandrac.library.fragments.lists.LibraryBooksListFragment;
import xyz.kandrac.library.fragments.lists.PublisherBooksListFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.utils.DisplayUtils;
import xyz.kandrac.library.utils.LogUtils;
import xyz.kandrac.library.views.DummyDrawerCallback;

/**
 * So far, this is launcher Activity of this Application. It contains side menu Navigation, title as
 * Action Bar and the content fragment replaced by currently selected item.
 *
 * @see NavigationView
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final long WAIT_FOR_DOUBLE_CLICK_BACK = 3000;
    public static final String IAB_LOG = "In-App-Billing";

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

    public static final String PREFERENCE_PHOTOS_RESIZED = "photos_resized_preference_2";
    public static final String PREFERENCE_PHOTOS_REMOVED = "photos_removed_preference";

    private Toolbar toolbar;
    private NavigationView navigation;
    private DrawerLayout drawerLayout;

    private MenuItem lastChecked;
    private Fragment mShownFragment;
    private SearchView searchView;
    private ActionBar mActionBar;
    private long mLastFinishingBackClicked;


    // In App billing service specification
    private IInAppBillingService mBillingService;

    private ServiceConnection mBillingServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d(IAB_LOG, "service disconnected");
            mBillingService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            LogUtils.d(IAB_LOG, "service connected");
            mBillingService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitService.start(this);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigation = (NavigationView) findViewById(R.id.main_navigation);
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

        bindBillingService();
    }

    /**
     * Binds Billing service so application can actually get data about in app products
     */
    private void bindBillingService() {
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mBillingServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        InitService.start(this, InitService.ACTION_CLEAR_DATABASE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBillingService != null) {
            unbindService(mBillingServiceConn);
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
    public boolean onNavigationItemSelected(MenuItem menuItem) {

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

                new MultipleBillingItemAsyncTask(getPackageName(), mBillingService).execute("drive");
                // start Drive activity only if drive plugin is purchased
                startActivity(new Intent(this, DriveActivity.class));
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
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case WISH_COUNT: {
                setActionViewTextFromCursor(R.id.main_navigation_wish_list, data);
                break;
            }
            case MY_COUNT: {
                setActionViewTextFromCursor(R.id.main_navigation_books, data);
                break;
            }
            case BORROWED_COUNT: {
                setActionViewTextFromCursor(R.id.main_navigation_borrowed, data);
                break;
            }
            case FROM_FRIENDS_COUNT: {
                setActionViewTextFromCursor(R.id.main_navigation_borrowed_to_me, data);
                break;
            }
        }
    }

    private void setActionViewTextFromCursor(@IdRes int viewId, Cursor data) {
        if (data.moveToFirst()) {
            String count = data.getString(0);
            View actionView = navigation.getMenu().findItem(viewId).getActionView();
            TextView text = (TextView) actionView.findViewById(R.id.action_view);
            text.setText(count);
            text.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}