package kandrac.xyz.library;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import kandrac.xyz.library.model.Contract;
import kandrac.xyz.library.model.obj.Book;

/**
 * Shows all the details about book based on its ID from {@link #EXTRA_BOOK_ID}.
 * <p/>
 * Created by VizGhar on 18.10.2015.
 */
public class BookDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_BOOK_ID = "book_id_extra";

    // LOADERS
    static final int LOADER_BOOK = 1;
    static final int LOADER_CONTACT = 2;
    static final int LOADER_BORROW_DETAIL = 3;

    // PERMISSIONS
    static final int PICK_CONTACT_PERMISSION = 1;

    // INTENT ACTIONS
    static final int PICK_CONTACT_ACTION = 1;

    Uri contactUri;

    private Long mBookId;
    private boolean displayShare;

    // Layout binding
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Bind(R.id.book_input_cover_image)
    ImageView cover;

    @Bind(R.id.subtitle)
    TextView subtitle;

    @Bind(R.id.book_detail_author)
    TextView author;

    @Bind(R.id.book_detail_isbn)
    TextView isbn;

    @Bind(R.id.book_detail_description)
    TextView description;

    @Bind(R.id.book_detail_borrow_image)
    ImageView borrowImage;

    @Bind(R.id.book_detail_borrow)
    Button borrowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.book_detail);

        ButterKnife.bind(this);

        // set Action Bar
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        mBookId = getIntent().getExtras().getLong(EXTRA_BOOK_ID);

        getSupportLoaderManager().initLoader(LOADER_BOOK, null, this);
        getSupportLoaderManager().initLoader(LOADER_BORROW_DETAIL, null, this);
    }

    /**
     * Binds book to content
     *
     * @param book to bind
     */
    private void bindBook(Book book) {

        collapsingToolbarLayout.setTitle(book.title);
        subtitle.setText(book.subtitle);
        author.setText(book.authorsReadable);
        isbn.setText(book.isbn);
        description.setText(book.description);

        Picasso.with(this)
                .load(book.imageFilePath)
                .placeholder(R.drawable.navigation_back)
                .resize(cover.getMeasuredWidth(), cover.getMeasuredHeight())
                .centerInside()
                .into(cover);
    }

    /**
     * Binds borrowed info to content. If {@link kandrac.xyz.library.BookDetailActivity.BorrowDetails}
     * is null,
     *
     * @param details to bind
     */
    private void bindBorrowDetails(final BorrowDetails details) {
        if (details != null) {
            borrowImage.setVisibility(View.VISIBLE);
            borrowButton.setVisibility(View.VISIBLE);
            borrowButton.setText(details.name);
            borrowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BorrowFragment.getInstance(details.name, details.dateFrom, details.dateTo, details.id).show(getFragmentManager(), null);
                }
            });
            displayShare = false;
        } else {
            borrowImage.setVisibility(View.GONE);
            borrowButton.setVisibility(View.GONE);
            displayShare = true;
        }

        supportInvalidateOptionsMenu();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_BOOK:
                return new CursorLoader(this, Contract.Books.buildBookUri(mBookId), null, null, null, null);
            case LOADER_CONTACT:
                // invoked after result came from Contacts
                String contactId = contactUri.getLastPathSegment();
                return new CursorLoader(
                        this,
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        new String[]{ContactsContract.Data.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email.DATA1},
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{contactId},
                        null);
            case LOADER_BORROW_DETAIL:
                return new CursorLoader(this, Contract.Books.buildBorrowInfoUri(mBookId), null, Contract.BorrowInfo.BORROW_DATE_RETURNED + " = 0", null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        switch (id) {
            case LOADER_BOOK: {
                bindBook(new Book(data));
                break;
            }
            case LOADER_CONTACT: {
                if (!data.moveToFirst()) {
                    // TODO: replace
                    Toast.makeText(this, "no data to obtain", Toast.LENGTH_SHORT).show();
                    break;
                }

                final long dateFrom = new Date(System.currentTimeMillis()).getTime();
                final String name = data.getString(0);
                final String mail = data.getString(1);

                String contactId = contactUri.getLastPathSegment();
                ContentValues cv = new ContentValues();
                cv.put(Contract.BorrowInfo.BORROW_TO, contactId);
                cv.put(Contract.BorrowInfo.BORROW_DATE_BORROWED, dateFrom);
                cv.put(Contract.BorrowInfo.BORROW_NAME, name);
                cv.put(Contract.BorrowInfo.BORROW_MAIL, mail);
                getContentResolver().insert(Contract.Books.buildBorrowInfoUri(mBookId), cv);

                break;
            }
            case LOADER_BORROW_DETAIL: {
                if (data.moveToFirst() && data.getCount() != 0) {
                    BorrowDetails borrowDetails = new BorrowDetails(
                            data.getLong(data.getColumnIndex(Contract.BorrowInfo.BORROW_ID)),
                            data.getString(data.getColumnIndex(Contract.BorrowInfo.BORROW_NAME)),
                            data.getLong(data.getColumnIndex(Contract.BorrowInfo.BORROW_DATE_BORROWED)),
                            data.getLong(data.getColumnIndex(Contract.BorrowInfo.BORROW_DATE_RETURNED))
                    );
                    bindBorrowDetails(borrowDetails);

                } else {
                    bindBorrowDetails(null);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_borrow).setVisible(displayShare);
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
        switch (reqCode) {
            case (PICK_CONTACT_ACTION): {
                if (resultCode == Activity.RESULT_OK) {
                    contactUri = data.getData();
                    getSupportLoaderManager().restartLoader(LOADER_CONTACT, null, this);
                }
                break;
            }
            default:
                super.onActivityResult(reqCode, resultCode, data);
        }
    }

    private class BorrowDetails {
        long id;
        String name;
        long dateFrom;
        long dateTo;

        public BorrowDetails(long id, String name, long dateFrom, long dateTo) {
            this.id = id;
            this.name = name;
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
        }
    }
}
