package kandrac.xyz.library;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

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

    static final int LOADER_BOOK = 1;
    static final int LOADER_CONTACT = 2;

    static final int PICK_CONTACT_PERMISSION = 1;
    static final int PICK_CONTACT_ACTION = 1;

    Uri contactUri;

    private Long mBookId;
    private BookDetailBinding binding;
    private MenuItem mBorrowMenuItem;
    private Book mBook;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Bind(R.id.book_input_cover_image)
    ImageView cover;

    @Bind(R.id.subtitle)
    TextView subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.book_detail);

        ButterKnife.bind(this);

        // set ToolBar;
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        mBookId = getIntent().getExtras().getLong(EXTRA_BOOK_ID);
        getSupportLoaderManager().initLoader(LOADER_BOOK, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_BOOK:
                return new CursorLoader(this, Contract.Books.buildBookUri(mBookId), null, null, null, null);
            case LOADER_CONTACT:
                String contactId = contactUri.getLastPathSegment();
                return new CursorLoader(
                        this,
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        new String[]{ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email.DATA1},
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                        new String[]{contactId},
                        null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        switch (id) {
            case LOADER_BOOK:
                if (data.getCount() == 1) {
                    mBook = new Book(data);
                    binding.setBook(mBook);
                    collapsingToolbarLayout.setTitle(mBook.title);
                    subtitle.setText(mBook.subtitle);

                    if (mBorrowMenuItem != null) {
                        mBorrowMenuItem.setVisible(mBook.borrowedTo == null);
                    }

                    if (mBook.imageFilePath == null) {
                        return;
                    }

                    File file = new File(mBook.imageFilePath);

                    if (!file.exists()) {
                        return;
                    }

                    Picasso.with(this)
                            .load(file)
                            .resize(cover.getMeasuredWidth(), cover.getMeasuredHeight())
                            .centerInside()
                            .into(cover);
                }
                break;
            case LOADER_CONTACT: {
                if (data.moveToFirst()) {
                    do {
                        Toast.makeText(this, data.getString(0) + "," + data.getString(1), Toast.LENGTH_SHORT).show();
                    } while (data.moveToNext());
                }

                String contactId = contactUri.getLastPathSegment();
                ContentValues cv = new ContentValues();
                cv.put(Contract.Books.BOOK_BORROWED_TO, contactId);
                getContentResolver().update(Contract.Books.buildBookUri(mBookId), cv, null, null);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // ToolBar option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_detail_menu, menu);
        mBorrowMenuItem = menu.findItem(R.id.action_borrow);

        if (mBook != null) {
            mBorrowMenuItem.setVisible(mBook.borrowedTo == null);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit: {
                Intent intent = new Intent(this, EditBookActivity.class);
                intent.putExtra(EditBookActivity.EXTRA_BOOK_ID, mBookId);
                startActivity(intent);
                return true;
            }
            case R.id.action_borrow: {
                int readContactPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
                if (readContactPermission == PackageManager.PERMISSION_GRANTED) {
                    searchContact();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PICK_CONTACT_PERMISSION);
                }
                return true;
            }
            case R.id.action_delete: {
                getContentResolver().delete(Contract.Books.buildBookUri(mBookId), null, null);
                finish();
                return true;
            }
            case android.R.id.home: {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PICK_CONTACT_PERMISSION: {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    searchContact();
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void searchContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_ACTION);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT_ACTION): {
                if (resultCode == Activity.RESULT_OK) {
                    contactUri = data.getData();
                    getSupportLoaderManager().initLoader(LOADER_CONTACT, null, this);
                }
            }
        }
    }
}
