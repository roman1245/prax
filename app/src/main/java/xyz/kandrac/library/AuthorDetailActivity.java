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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.Database;
import xyz.kandrac.library.model.obj.Author;
import xyz.kandrac.library.utils.BookCursorAdapter;

/**
 * Activity that displays details about current author, based on {@link #EXTRA_AUTHOR_ID}.
 * For simplicity and minimalistic application approach, there are only books linked to
 * author displayed.
 * <p/>
 * This Activity should only run in case we are running on mobile device. Currently only
 * mobile devices are supported and this activity doesn't contain any fragment. This will
 * be fixed same time as tablet version is introduced.
 * <p/>
 * Created by VizGhar on 25.10.2015.
 */
public class AuthorDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_AUTHOR_ID = "author_id_extra";

    private long mAuthorId;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Bind(R.id.list)
    RecyclerView recyclerView;

    private BookCursorAdapter mAuthorBooksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.author_detail);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        mAuthorBooksAdapter = new BookCursorAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAuthorBooksAdapter);

        mAuthorId = getIntent().getExtras().getLong(EXTRA_AUTHOR_ID);

        getSupportLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                Contract.Authors.buildBooksUri(mAuthorId),
                new String[]{
                        Contract.Authors.AUTHOR_NAME,
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
            Author author = new Author(data);
            collapsingToolbarLayout.setTitle(TextUtils.isEmpty(author.name) ? getString(R.string.author_unknown) : author.name);
            mAuthorBooksAdapter.setCursor(data);
            mAuthorBooksAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.author_detail_menu, menu);
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
                        .setTitle(R.string.dialog_author_delete_title)
                        .setMessage(R.string.dialog_author_delete_message)
                        .setCancelable(true)
                        .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getContentResolver().delete(Contract.Authors.buildAuthorUri(mAuthorId), null, null);
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
}
