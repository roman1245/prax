package kandrac.xyz.library;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kandrac.xyz.library.databinding.BookListItemBinding;
import kandrac.xyz.library.model.DatabaseProvider;
import kandrac.xyz.library.model.obj.Book;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.list)
    RecyclerView list;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.main_drawer)
    NavigationView navigation;

    @Bind(R.id.main_navigation)
    DrawerLayout drawerLayout;

    BookCursorAdapter adapter;

    private MenuItem lastChecked;

    @OnClick(R.id.fab)
    public void addItem(View view) {
        startActivity(new Intent(this, EditBookActivity.class));
    }

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
        MenuItem menuItem = navigation.getMenu().getItem(0);
        menuItem.setChecked(true);
        lastChecked = menuItem;

        // Content settings
        adapter = new BookCursorAdapter();
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        // Init database loading
        getSupportLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DatabaseProvider.getUri(DatabaseProvider.BOOKS), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.setCursor(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
        int id = menuItem.getItemId();

        switch (id) {
            case R.id.main_navigation_about:
                new AboutFragment().show(getFragmentManager(), null);
                break;
            default:
                if (lastChecked == menuItem) {
                    break;
                }
                lastChecked.setChecked(false);
                menuItem.setChecked(true);
                lastChecked = menuItem;
        }
        drawerLayout.closeDrawers();
        return true;
    }

    private class BookCursorAdapter extends RecyclerView.Adapter<BookCursorAdapter.BindingHolder> {

        public class BindingHolder extends RecyclerView.ViewHolder {
            public BindingHolder(View rowView) {
                super(rowView);
            }
        }

        Cursor mCursor;

        public void setCursor(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BindingHolder(BookListItemBinding.inflate(getLayoutInflater(), parent, false).getRoot());
        }

        @Override
        public void onBindViewHolder(BindingHolder holder, int position) {
            mCursor.moveToPosition(position);
            final Book book = new Book(mCursor);

            BookListItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
            binding.setBook(book);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                    intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.id);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (mCursor != null) {
                return mCursor.getCount();
            } else return 0;
        }
    }
}