package kandrac.xyz.library;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.main_navigation)
    NavigationView navigation;

    @Bind(R.id.main_drawer)
    DrawerLayout drawerLayout;

    private MenuItem lastChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_list);
        ButterKnife.bind(this);

        // Action Bar settings
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
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
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new BookListFragment())
                .commit();
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
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigation)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (lastChecked == menuItem) {
            drawerLayout.closeDrawers();
            return true;
        }

        int id = menuItem.getItemId();

        Fragment fragmentToShow;
        switch (id) {
            case R.id.main_navigation_about:
                drawerLayout.closeDrawers();
                new AboutDialog().show(getFragmentManager(), null);
                return true;
            case R.id.main_navigation_books:
            default:
                fragmentToShow = new BookListFragment();
        }

        drawerLayout.closeDrawers();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, fragmentToShow)
                .commit();

        lastChecked.setChecked(false);
        menuItem.setChecked(true);
        lastChecked = menuItem;
        return true;
    }
}