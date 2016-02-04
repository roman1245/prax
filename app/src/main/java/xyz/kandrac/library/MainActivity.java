package xyz.kandrac.library;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
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
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Loader constants. Ensure that fragments are using this constants and not the
    public static final int BOOK_LIST_LOADER = 1;
    public static final int AUTHOR_LIST_LOADER = 2;
    public static final int PUBLISHER_LIST_LOADER = 3;
    public static final int LIBRARY_LIST_LOADER = 4;
    public static final int BORROWED_BOOK_LIST_LOADER = 5;
    public static final int WISH_LIST_BOOK_LIST_LOADER = 6;

    public static final String PREFERENCE_PHOTOS_RESIZED = "photos_resized_preference_2";
    public static final String PREFERENCE_PHOTOS_REMOVED = "photos_removed_preference";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.main_navigation)
    NavigationView navigation;

    @Bind(R.id.main_drawer)
    DrawerLayout drawerLayout;

    private MenuItem lastChecked;
    private Fragment mShownFragment;
    private SearchView searchView;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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
        drawerLayout.setDrawerListener(new DummyDrawerCallback() {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (searchView != null) {
                    searchView.clearFocus();
                }
            }
        });

        removeUnusedPhotosIfNeeded();
        resizePhotosIfNeeded();
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
            // take standard action otherwise
            super.onBackPressed();
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
        Fragment fragmentToShow = getFragmentToShow(menuItem.getItemId());

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
     * Based on ID of menu item, get new instance of fragment related to it.
     *
     * @param menuItemId ID of menu item
     * @return null if nothing to be shown
     */
    private Fragment getFragmentToShow(final int menuItemId) {
        switch (menuItemId) {
            case R.id.main_navigation_books:
                return BookListFragment.getInstance();
            case R.id.main_navigation_borrowed:
                return BookListFragment.getBorrowedBooksInstance();
            case R.id.main_navigation_wish_list:
                return BookListFragment.getWishListBooksInstance();
            case R.id.main_navigation_authors:
                return new AuthorBooksListFragment();
            case R.id.main_navigation_publishers:
                return new PublisherBooksListFragment();
            case R.id.main_navigation_libraries:
                return new LibraryBooksListFragment();
            case R.id.main_navigation_settings:
                return new SettingsFragment();
            default:
                return null;
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
}