package kandrac.xyz.library;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import kandrac.xyz.library.databinding.BookDetailBinding;
import kandrac.xyz.library.model.DatabaseProvider;
import kandrac.xyz.library.model.obj.Book;

/**
 * Created by VizGhar on 18.10.2015.
 */
public class BookDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_BOOK_ID = "book_id_extra";
    private Long mBookId;
    private BookDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.book_detail);
        mBookId = getIntent().getExtras().getLong(EXTRA_BOOK_ID);
        getSupportLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DatabaseProvider.getUriWithId(DatabaseProvider.BOOK_ID, mBookId), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 1) {
            Book book = new Book(data);
            binding.setBook(book);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
