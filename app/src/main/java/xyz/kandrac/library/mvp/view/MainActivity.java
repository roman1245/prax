package xyz.kandrac.library.mvp.view;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

import javax.inject.Inject;

import xyz.kandrac.library.InitService;
import xyz.kandrac.library.LibraryApplication;
import xyz.kandrac.library.R;
import xyz.kandrac.library.Searchable;
import xyz.kandrac.library.billing.BillingSkus;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.fragments.lists.AuthorBooksListFragment;
import xyz.kandrac.library.fragments.lists.BookListFragment;
import xyz.kandrac.library.fragments.lists.LibraryBooksListFragment;
import xyz.kandrac.library.fragments.lists.PublisherBooksListFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.mvp.presenter.MainPresenter;
import xyz.kandrac.library.utils.LogUtils;
import xyz.kandrac.library.views.DummyDrawerCallback;

/**
 * So far, this is launcher Activity of this Application. It contains side menu Navigation, title as
 * Action Bar and the content fragment replaced by currently selected item.
 *
 * @see NavigationView
 */
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        MainView {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    public static final long WAIT_FOR_DOUBLE_CLICK_BACK = 3000;

    // Loader constants. Ensure that fragments are using this constants and not the
    public static final int BOOK_LIST_LOADER = 1;
    public static final int AUTHOR_LIST_LOADER = 2;
    public static final int PUBLISHER_LIST_LOADER = 3;
    public static final int LIBRARY_LIST_LOADER = 4;
    public static final int BORROWED_BOOK_LIST_LOADER = 5;
    public static final int BORROWED_TO_ME_LIST_LOADER = 6;
    public static final int WISH_LIST_BOOK_LIST_LOADER = 7;

    public static final String PREFERENCE_PHOTOS_REMOVED = "photos_removed_preference";

    private NavigationView navigation;
    private DrawerLayout drawerLayout;

    private TextView userName;
    private TextView userMail;
    private ImageView userPhoto;

    private MenuItem lastChecked;
    private Fragment mShownFragment;
    private ActionBar mActionBar;

    private LinearLayout mContentHolder;

    private MenuItem isbnSearch;

    private boolean searchOpened;
    private EditText searchView;

    @Inject
    MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Base_NoStatusBar);

        super.onCreate(savedInstanceState);
        InitService.start(this);

        setContentView(R.layout.activity_main);

        LibraryApplication.getNetComponent(this).inject(this);
        presenter.setView(this);

        // get views
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigation = (NavigationView) findViewById(R.id.main_navigation);
        View navigationHeader = navigation.getHeaderView(0);
        userName = (TextView) navigationHeader.findViewById(R.id.navigation_header_line1);
        userMail = (TextView) navigationHeader.findViewById(R.id.navigation_header_line2);
        userPhoto = (ImageView) navigationHeader.findViewById(R.id.navigation_header_profile_image);
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);
        mContentHolder = (LinearLayout) findViewById(R.id.main_content_holder);


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

        presenter.initNavigationView();
        presenter.configureSignIn();
        presenter.configureIAB();
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        InitService.start(this, InitService.ACTION_CLEAR_DATABASE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                if (searchOpened) {
                    mContentHolder.removeViewAt(1);
                    searchOpened = false;
                } else {
                    View searchView2 = getLayoutInflater().inflate(R.layout.search, mContentHolder, false);
                    this.searchView = (EditText) searchView2.findViewById(R.id.search_text);
                    searchView.requestFocus();
                    this.searchView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (mShownFragment instanceof Searchable) {
                                ((Searchable) mShownFragment).requestSearch(searchView.getText().toString());
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            searchView.clearFocus();
                        }
                    });
                    mContentHolder.addView(searchView2, 1);
                    searchOpened = true;
                }
                return true;
            case R.id.search_by_ean:
                return true;
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
                presenter.consume(BillingSkus.getDriveSku());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // Associate searchable configuration with the SearchView
        isbnSearch = menu.findItem(R.id.search_by_ean);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchOpened) {
            searchOpened = false;
            mContentHolder.removeViewAt(1);
            searchView = null;
        } else if (presenter.evaluateBack(drawerLayout, navigation)) {
            super.onBackPressed();
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
                presenter.startPurchaseFlow(BillingSkus.getDriveSku());
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
        if (!presenter.onActivityResult(requestCode, resultCode, data)) {
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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.navigation_header:
                presenter.authenticate();
        }
    }

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    // Action view texts based on db results

    @Override
    public void onWishlistItemsCount(int count) {
        setActionViewText(R.id.main_navigation_wish_list, Integer.toString(count));
    }

    @Override
    public void onMyBooksCount(int count) {
        setActionViewText(R.id.main_navigation_books, Integer.toString(count));
    }

    @Override
    public void onBorrowedBooksCount(int count) {
        setActionViewText(R.id.main_navigation_borrowed, Integer.toString(count));
    }

    @Override
    public void onBooksFromFriendsCount(int count) {
        setActionViewText(R.id.main_navigation_borrowed_to_me, Integer.toString(count));
    }

    @Override
    public void setLibraryItemVisibility(boolean visibility) {
        MenuItem librariesMenuItem = navigation.getMenu().findItem(R.id.main_navigation_libraries);
        if (visibility) {
            librariesMenuItem.setVisible(true);
        } else {
            librariesMenuItem.setVisible(false);
        }
    }

    @Override
    public void setDriveVisibility(boolean visible) {
        navigation.getMenu().findItem(R.id.main_navigation_drive).setVisible(visible);
    }

    @Override
    public void showUserDetail(String displayName, String email, Uri photoUrl) {
        userName.setText(displayName);
        userMail.setText(email);
        Picasso.with(MainActivity.this).load(photoUrl).into(userPhoto);
    }

    @Override
    public void interact(int type, String message) {
        switch (type) {
            case ERROR_TYPE_GOOGLE_API_CONNECTION:
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
            case ERROR_TYPE_GOOGLE_SIGNIN:
                Toast.makeText(this, R.string.sign_in_connection_error, Toast.LENGTH_SHORT).show();
                break;
            case INFO_PRESS_AGAIN_TO_LEAVE:
                Toast.makeText(this, R.string.press_again_to_leave, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setActionViewText(@IdRes int viewId, String show) {
        if (show != null) {
            View actionView = navigation.getMenu().findItem(viewId).getActionView();
            TextView text = (TextView) actionView.findViewById(R.id.action_view);
            text.setText(show);
            text.setVisibility(View.VISIBLE);
        }
    }
}