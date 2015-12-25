package xyz.kandrac.library;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.Database;

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
public class AuthorDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AuthorDetailFragment.AuthorFragmentCallbacks {

    public static final String EXTRA_AUTHOR_ID = "author_id_extra";

    public static final String AUTHOR_DETAIL_FRAGMENT = "author_detail";

    private long mAuthorId;
    private AuthorDetailFragment mDetailFragment;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_author_detail);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        mAuthorId = getIntent().getExtras().getLong(EXTRA_AUTHOR_ID);

        if (savedInstanceState == null) {
            mDetailFragment = AuthorDetailFragment.getInstance();
            getFragmentManager().beginTransaction().add(R.id.content, mDetailFragment, AUTHOR_DETAIL_FRAGMENT).commit();
        } else {
            Fragment fragment = getFragmentManager().findFragmentByTag(AUTHOR_DETAIL_FRAGMENT);
            if (fragment != null && fragment instanceof AuthorDetailFragment) {
                mDetailFragment = (AuthorDetailFragment) fragment;
            } else {
                throw new IllegalStateException("Author detail fragment was not found");
            }
        }

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
            mDetailFragment.processCursor(data);
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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void deleteAuthor() {
        getContentResolver().delete(Contract.Authors.buildAuthorUri(mAuthorId), null, null);
        finish();
    }

    @Override
    public void onChangeAuthorName(String name) {
        collapsingToolbarLayout.setTitle(TextUtils.isEmpty(name) ? getString(R.string.author_unknown) : name);
    }
}
