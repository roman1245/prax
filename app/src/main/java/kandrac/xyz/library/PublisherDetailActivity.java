package kandrac.xyz.library;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import kandrac.xyz.library.databinding.PublisherDetailBinding;
import kandrac.xyz.library.model.Contract;
import kandrac.xyz.library.model.Database;
import kandrac.xyz.library.model.obj.Publisher;
import kandrac.xyz.library.utils.BookCursorAdapter;

/**
 * Created by VizGhar on 25.10.2015.
 */
public class PublisherDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_PUBLISHER_ID = "publisher_id_extra";
    private long mPublisherId;
    private PublisherDetailBinding binding;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Bind(R.id.book_input_cover_image)
    ImageView cover;

    @Bind(R.id.list)
    RecyclerView recyclerView;

    BookCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.publisher_detail);

        ButterKnife.bind(this);

        // set ToolBar
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        adapter = new BookCursorAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        mPublisherId = getIntent().getExtras().getLong(EXTRA_PUBLISHER_ID);
        getSupportLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                this,
                Contract.Publishers.buildBooksUri(mPublisherId),
                new String[]{
                        Contract.Publishers.PUBLISHER_NAME,
                        Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID,
                        Contract.Books.BOOK_TITLE,
                        Contract.Books.BOOK_IMAGE_FILE,
                        Contract.Books.BOOK_AUTHORS_READ
                },
                null, null, null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            Publisher publisher = new Publisher(data);
            binding.setPublisher(publisher);
            collapsingToolbarLayout.setTitle(publisher.name);

            adapter.setCursor(data);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
}
