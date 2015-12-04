package kandrac.xyz.library;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import kandrac.xyz.library.views.DummyDrawerCallback;

/**
 * So far, this is launcher Activity of this Application. It contains side menu Navigation, title as
 * Action Bar and the content fragment replaced by currently selected item.
 *
 * @see NavigationView
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SubtitledFragment.ChangeTitleListener {

    // Loader constants. Ensure that fragments are using this constants and not the
    public static final int BOOK_LIST_LOADER = 1;
    public static final int AUTHOR_LIST_LOADER = 2;
    public static final int PUBLISHER_LIST_LOADER = 3;
    public static final int BORROWED_BOOK_LIST_LOADER = 4;

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
        setContentView(R.layout.book_list);
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
        MenuItem menuItem = navigation.getMenu().findItem(R.id.main_navigation_books);
        menuItem.setChecked(true);
        lastChecked = menuItem;

        // Content settings
        if (savedInstanceState != null) {
            return;
        }

        // setup first fragment
        mShownFragment = BookListFragment.getInstance();
        getSupportFragmentManager()
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
        SubtitledFragment fragmentToShow = getFragmentToShow(menuItem.getItemId());

        if (fragmentToShow == null) {
            return false;
        }

        // close drawers and use replace fragment
        drawerLayout.closeDrawers();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragmentToShow)
                .commit();

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
    private SubtitledFragment getFragmentToShow(final int menuItemId) {
        switch (menuItemId) {
            case R.id.main_navigation_about:
                // about dialog doesn't run any fragment only displays About dialog now
                drawerLayout.closeDrawers();
                new AboutDialog().show(getFragmentManager(), null);
                return null;
            case R.id.main_navigation_books:
                return BookListFragment.getInstance();
            case R.id.main_navigation_borrowed:
                return BookListFragment.getBorrowedBooksInstance();
            case R.id.main_navigation_authors:
                return new AuthorListFragment();
            case R.id.main_navigation_publishers:
                return new PublisherListFragment();
            default:
                return null;
        }
    }

    @Override
    public void onTitleLoaded(String title) {
        // change title
        if (mActionBar != null) {
            mActionBar.setTitle(title);
        }
    }
}