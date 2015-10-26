package kandrac.xyz.library;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import kandrac.xyz.library.databinding.BookDetailBinding;
import kandrac.xyz.library.model.Contract;
import kandrac.xyz.library.model.obj.Book;

/**
 * Created by VizGhar on 18.10.2015.
 */
public class BookDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_BOOK_ID = "book_id_extra";
    private Long mBookId;
    private BookDetailBinding binding;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.book_input_cover_image)
    ImageView cover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.book_detail);

        ButterKnife.bind(this);

        // set ToolBar
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        mBookId = getIntent().getExtras().getLong(EXTRA_BOOK_ID);
        getSupportLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Contract.Books.buildBookWithAuthorUri(mBookId), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 1) {
            Book book = new Book(data);
            binding.setBook(book);

            if (book.imageFilePath == null) {
                return;
            }

            File file = new File(book.imageFilePath);

            if (!file.exists()) {
                return;
            }

            Picasso.with(this)
                    .load(file)
                    .resize(cover.getMeasuredWidth(), cover.getMeasuredHeight())
                    .centerInside()
                    .into(cover);
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
                intent.putExtra(EditBookActivity.EXTRA_BOOK_ID, mBookId);
                startActivity(intent);
                return true;
            case R.id.action_delete:
                getContentResolver().delete(Contract.Books.buildBookUri(mBookId), null, null);
                finish();
                return true;
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
