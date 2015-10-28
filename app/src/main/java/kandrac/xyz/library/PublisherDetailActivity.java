package kandrac.xyz.library;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import kandrac.xyz.library.databinding.PublisherDetailBinding;
import kandrac.xyz.library.model.Contract;
import kandrac.xyz.library.model.obj.Publisher;

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

        mPublisherId = getIntent().getExtras().getLong(EXTRA_PUBLISHER_ID);
        getSupportLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Contract.Publishers.buildPublisherUri(mPublisherId), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 1) {
            Publisher publisher = new Publisher(data);
            binding.setPublisher(publisher);
            collapsingToolbarLayout.setTitle(publisher.name);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // ToolBar option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit:
                Intent intent = new Intent(this, EditBookActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_delete:
                getContentResolver().delete(Contract.Publishers.CONTENT_URI, Contract.Publishers.PUBLISHER_ID + " = ?", new String[]{Long.toString(mPublisherId)});
                finish();
                return true;
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
