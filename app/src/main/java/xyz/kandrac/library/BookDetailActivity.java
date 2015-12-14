package xyz.kandrac.library;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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

import java.io.File;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.obj.Book;
import xyz.kandrac.library.utils.DateUtils;

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

    @Bind(R.id.parallax_cover_image)
    ImageView cover;

    @Bind(R.id.parallax_cover_subtitle)
    TextView subtitle;

    @Bind(R.id.book_detail_author)
    TextView author;

    @Bind(R.id.book_detail_isbn)
    TextView isbn;

    @Bind(R.id.book_detail_description)
    TextView description;

    @Bind(R.id.book_detail_publisher)
    TextView publisher;

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
        publisher.setText(book.publisherReadable);

        File imageFile = book.imageFilePath == null ? null : new File(book.imageFilePath);

        Picasso.with(this)
                .load(imageFile != null && imageFile.exists() ? imageFile : null)
                .placeholder(R.drawable.navigation_back)
                .resize(cover.getMeasuredWidth(), cover.getMeasuredHeight())
                .centerInside()
                .into(cover);
    }

    /**
     * Binds borrowed info to content. If {@link xyz.kandrac.library.BookDetailActivity.BorrowDetails}
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

                    new AlertDialog.Builder(BookDetailActivity.this)
                            .setTitle(R.string.dialog_return_book_title)
                            .setMessage(getString(R.string.dialog_return_book_message, details.name, DateUtils.dateFormat.format(details.dateFrom)))
                            .setPositiveButton(R.string.dialog_return_book_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ContentValues cv = new ContentValues();
                                    cv.put(Contract.BorrowInfo.BORROW_DATE_RETURNED, new Date(System.currentTimeMillis()).getTime());
                                    getContentResolver().update(Contract.BorrowInfo.buildUri(details.id), cv, null, null);

                                    ContentValues bookContentValues = new ContentValues();
                                    bookContentValues.put(Contract.Books.BOOK_BORROWED, false);
                                    getContentResolver().update(Contract.Books.buildBookUri(mBookId), bookContentValues, null, null);
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(true)
                            .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
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
                // TODO: check ContactsContract.CommonDataKinds.Email.CONTENT_URI to get Email or
                // developer.android.com/reference/android/provider/ContactsContract.Data.html and MIME types

                String contactId = contactUri.getLastPathSegment();
                return new CursorLoader(
                        this,
                        Data.CONTENT_URI,
                        ContactRequest.GENERAL_COLUMNS,
                        ContactRequest.GENERAL_SELECTION,
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
                if (data != null && data.getCount() > 0) {
                    bindBook(new Book(data));
                }
                break;
            }
            case LOADER_CONTACT: {
                if (!data.moveToFirst()) {
                    Toast.makeText(this, R.string.unexpected_error_occurs, Toast.LENGTH_SHORT).show();
                    break;
                }

                final long dateFrom = new Date(System.currentTimeMillis()).getTime();
                final String name = data.getString(ContactRequest.NAME_COLUMN);

                String contactId = contactUri.getLastPathSegment();
                ContentValues borrowContentValues = new ContentValues();
                borrowContentValues.put(Contract.BorrowInfo.BORROW_TO, contactId);
                borrowContentValues.put(Contract.BorrowInfo.BORROW_DATE_BORROWED, dateFrom);
                borrowContentValues.put(Contract.BorrowInfo.BORROW_NAME, name);
                getContentResolver().insert(Contract.Books.buildBorrowInfoUri(mBookId), borrowContentValues);

                ContentValues bookContentValues = new ContentValues();
                bookContentValues.put(Contract.Books.BOOK_BORROWED, true);
                getContentResolver().update(Contract.Books.buildBookUri(mBookId), bookContentValues, null, null);

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
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_book_delete_title)
                        .setMessage(R.string.dialog_book_delete_message)
                        .setCancelable(true)
                        .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getContentResolver().delete(Contract.Books.buildBookUri(mBookId), null, null);
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

    /**
     * Helper interface for creating Contact Requests
     */
    @SuppressWarnings("unused")
    private interface ContactRequest {

        // WHERE Statements
        String GENERAL_SELECTION = Data.CONTACT_ID + " = ?";
        String PHONE_SELECTION = GENERAL_SELECTION + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'";

        // selection columns
        String[] GENERAL_COLUMNS = new String[]{Data.DISPLAY_NAME};
        String[] PHONE_COLUMNS = new String[]{Phone.NUMBER};

        int NAME_COLUMN = 0;

    }
}
