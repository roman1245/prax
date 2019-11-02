package xyz.kandrac.library;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.utils.BookCursorAdapter;

/**
 * Created by VizGhar on 25.10.2015.
 */
public class LibraryDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_LIBRARY_NAME = 1;
    public static final int LOADER_LIBRARY_DETAILS = 2;

    public static final String EXTRA_LIBRARY_ID = "library_id_extra";
    private long mLibraryId;

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView cover;
    private RecyclerView recyclerView;

    BookCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_publisher_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        cover = (ImageView) findViewById(R.id.parallax_cover_image);
        recyclerView = (RecyclerView) findViewById(R.id.list);

        // set ToolBar
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mLibraryId = getIntent().getExtras().getLong(EXTRA_LIBRARY_ID);

        adapter = new BookCursorAdapter.Builder()
                .setLibrary(mLibraryId)
                .setWishList(BookCursorAdapter.FALSE)
                .setLoaderId(LOADER_LIBRARY_DETAILS)
                .setActivity(this)
                .build();

        recyclerView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(LOADER_LIBRARY_NAME, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.library_detail_menu, menu);
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
                        .setTitle(R.string.dialog_library_delete_title)
                        .setMessage(R.string.dialog_library_delete_message)
                        .setCancelable(true)
                        .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getContentResolver().delete(Contract.Libraries.buildLibraryUri(mLibraryId), null, null);
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
        if (id == LOADER_LIBRARY_NAME) {
            return new CursorLoader(
                    this,
                    Contract.Libraries.buildLibraryUri(mLibraryId),
                    new String[]{Contract.Libraries.LIBRARY_NAME},
                    null,
                    null,
                    null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String title;
        if (loader.getId() == LOADER_LIBRARY_NAME && data.moveToFirst()) {
            title = data.getString(data.getColumnIndex(Contract.Libraries.LIBRARY_NAME));
            if (TextUtils.isEmpty(title)) {
                title = getString(R.string.library_unknown);
            }
        } else {
            title = getString(R.string.library_unknown);
        }
        collapsingToolbarLayout.setTitle(title);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
