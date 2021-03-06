package xyz.kandrac.library;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import xyz.kandrac.library.fragments.AuthorDetailFragment;
import xyz.kandrac.library.model.Contract;

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
public class AuthorDetailActivity extends AppCompatActivity implements AuthorDetailFragment.AuthorFragmentCallbacks, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_AUTHOR_ID = "author_id_extra";

    public static final String AUTHOR_DETAIL_FRAGMENT = "author_detail";

    public static final int LOADER_AUTHOR_NAME = 1;
    public static final int LOADER_AUTHOR_DETAILS = 2;

    private long mAuthorId;

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        mAuthorId = getIntent().getExtras().getLong(EXTRA_AUTHOR_ID);

        if (savedInstanceState == null) {
            Fragment mDetailFragment = AuthorDetailFragment.getInstance(mAuthorId);
            getFragmentManager().beginTransaction().add(R.id.content, mDetailFragment, AUTHOR_DETAIL_FRAGMENT).commit();
        }

        getSupportLoaderManager().initLoader(LOADER_AUTHOR_NAME, null, this);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_AUTHOR_NAME) {
            return new CursorLoader(
                    this,
                    Contract.Authors.buildAuthorUri(mAuthorId),
                    new String[] {Contract.Authors.AUTHOR_NAME},
                    null,
                    null,
                    null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_AUTHOR_NAME && data.moveToFirst()) {
            onChangeAuthorName(data.getString(data.getColumnIndex(Contract.Authors.AUTHOR_NAME)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
