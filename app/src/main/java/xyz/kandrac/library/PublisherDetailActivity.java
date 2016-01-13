package xyz.kandrac.library;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.utils.BookCursorAdapter;

/**
 * Created by VizGhar on 25.10.2015.
 */
public class PublisherDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_PUBLISHER_NAME = 1;
    public static final int LOADER_PUBLISHER_DETAILS = 2;

    public static final String EXTRA_PUBLISHER_ID = "publisher_id_extra";
    private long mPublisherId;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Bind(R.id.parallax_cover_image)
    ImageView cover;

    @Bind(R.id.list)
    RecyclerView recyclerView;

    BookCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.publisher_detail);

        ButterKnife.bind(this);

        // set ToolBar
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPublisherId = getIntent().getExtras().getLong(EXTRA_PUBLISHER_ID);
        adapter = new BookCursorAdapter.Builder()
                .setPublisher(mPublisherId)
                .setLoaderId(LOADER_PUBLISHER_DETAILS)
                .setWishList(BookCursorAdapter.FALSE)
                .setActivity(this)
                .build();

        recyclerView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(LOADER_PUBLISHER_NAME, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.publisher_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_publisher_delete_title)
                        .setMessage(R.string.dialog_publisher_delete_message)
                        .setCancelable(true)
                        .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getContentResolver().delete(Contract.Publishers.buildPublisherUri(mPublisherId), null, null);
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_PUBLISHER_NAME) {
            return new CursorLoader(
                    this,
                    Contract.Publishers.buildPublisherUri(mPublisherId),
                    new String[]{Contract.Publishers.PUBLISHER_NAME},
                    null,
                    null,
                    null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_PUBLISHER_NAME && data.moveToFirst()) {
            collapsingToolbarLayout.setTitle(data.getString(data.getColumnIndex(Contract.Publishers.PUBLISHER_NAME)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
