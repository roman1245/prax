package xyz.kandrac.library;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.File;

import xyz.kandrac.library.dialogs.BorrowBookDialog;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.firebase.References;
import xyz.kandrac.library.model.obj.Author;
import xyz.kandrac.library.model.obj.Book;
import xyz.kandrac.library.model.obj.Borrowed;
import xyz.kandrac.library.model.obj.BorrowedToMe;
import xyz.kandrac.library.model.obj.Library;
import xyz.kandrac.library.model.obj.Publisher;
import xyz.kandrac.library.mvp.view.EditBookActivity;
import xyz.kandrac.library.utils.LogUtils;

/**
 * Shows all the details about book based on its ID from {@link #EXTRA_BOOK_ID}.
 * <p/>
 * Created by VizGhar on 18.10.2015.
 */
public class BookDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, TabLayout.OnTabSelectedListener {

    public static final String LOG_TAG = BookDetailActivity.class.getName();
    public static final String EXTRA_BOOK_ID = "book_id_extra";

    // LOADERS
    static final int LOADER_BOOK = 1;
    static final int LOADER_BORROW_DETAIL = 3;
    static final int LOADER_BORROW_ME_DETAIL = 4;
    static final int LOADER_AUTHOR = 5;
    static final int LOADER_PUBLISHER = 6;
    static final int LOADER_LIBRARY = 7;

    // PERMISSIONS
    static final int PICK_CONTACT_PERMISSION = 1;

    private Long mBookId;
    private Book mBook;

    private Fragment[] mContents;

    // Layout binding
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView cover;
    private TabLayout tabs;

    private boolean mShowBorrowDialog = false;
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        cover = (ImageView) findViewById(R.id.parallax_cover_image);
        tabs = (TabLayout) findViewById(R.id.tabs);

        // set Action Bar
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        mBookId = getIntent().getExtras().getLong(EXTRA_BOOK_ID);

        mContents = new Fragment[]{
                BookDetailBasicFragment.newInstance(mBookId),
                BookDetailOthersFragment.newInstance(mBookId)
        };

        LogUtils.d(LOG_TAG, "Showing book : " + mBookId);

        tabs.addTab(tabs.newTab().setText(R.string.book_detail_tab_basic));
        tabs.addTab(tabs.newTab().setText(R.string.book_detail_tab_others));

        tabs.addOnTabSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.content, mContents[0]).commit();
        }

        mAuth = FirebaseAuth.getInstance();

        getSupportLoaderManager().initLoader(BookDetailActivity.LOADER_BOOK, null, this);
        getSupportLoaderManager().initLoader(BookDetailActivity.LOADER_AUTHOR, null, this);
        getSupportLoaderManager().initLoader(BookDetailActivity.LOADER_BORROW_DETAIL, null, this);
        getSupportLoaderManager().initLoader(BookDetailActivity.LOADER_BORROW_ME_DETAIL, null, this);
        getSupportLoaderManager().initLoader(BookDetailActivity.LOADER_PUBLISHER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_detail_menu, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem borrowItem = menu.findItem(R.id.action_borrow);
        MenuItem moveItem = menu.findItem(R.id.action_move);

        if (mBook != null) {
            borrowItem.setVisible(!mBook.wish && !mBook.borrowed && !mBook.borrowedToMe);
            moveItem.setVisible(mBook.wish);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_borrow: {
                int readContactPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
                if (readContactPermission == PackageManager.PERMISSION_GRANTED) {
                    searchContact();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PICK_CONTACT_PERMISSION);
                }
                return true;
            }
            case R.id.action_edit: {
                Intent intent = new Intent(this, EditBookActivity.class);
                intent.putExtra(EditBookActivity.EXTRA_BOOK_ID, mBookId);
                startActivity(intent);
                return true;
            }
            case R.id.action_move: {
                ContentValues cv = new ContentValues();
                cv.put(Contract.Books.BOOK_WISH_LIST, false);
                cv.put(Contract.Books.BOOK_UPDATED_AT, System.currentTimeMillis());
                getContentResolver().update(Contract.Books.buildBookUri(mBookId), cv, null, null);
                finish();
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
                                NotificationReceiver.cancelNotification(BookDetailActivity.this, mBookId);
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
                    mShowBorrowDialog = true;
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mBookId = getIntent().getExtras().getLong(EXTRA_BOOK_ID);

        LogUtils.d(LOG_TAG, "Showing book from new intent : " + mBookId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {

            case LOADER_BOOK:

                return new CursorLoader(
                        this,
                        Contract.Books.buildBookUri(mBookId),
                        new String[]{
                                Contract.Books.BOOK_TITLE,
                                Contract.Books.BOOK_SUBTITLE,
                                Contract.Books.BOOK_ISBN,
                                Contract.Books.BOOK_DESCRIPTION,
                                Contract.Books.BOOK_WISH_LIST,
                                Contract.Books.BOOK_BORROWED_TO_ME,
                                Contract.Books.BOOK_BORROWED,
                                Contract.Books.BOOK_PUBLISHED,
                                Contract.Books.BOOK_IMAGE_FILE,
                                Contract.Books.BOOK_REFERENCE
                        },
                        null,
                        null,
                        null);

            case LOADER_AUTHOR:

                return new CursorLoader(
                        this,
                        Contract.Books.buildBookWithAuthorUri(mBookId),
                        new String[]{
                                Contract.Authors.AUTHOR_NAME
                        },
                        null,
                        null,
                        null);

            case LOADER_BORROW_DETAIL:

                return new CursorLoader(
                        this,
                        Contract.Books.buildBorrowInfoUri(mBookId),
                        null,
                        Contract.BorrowInfo.BORROW_DATE_RETURNED + " = 0",
                        null,
                        null);


            case LOADER_BORROW_ME_DETAIL:

                return new CursorLoader(
                        this,
                        Contract.Books.buildBorrowedToMeInfoUri(mBookId),
                        null,
                        null,
                        null,
                        null);

            case LOADER_PUBLISHER:

                return new CursorLoader(
                        this,
                        Contract.Books.buildBookPublisherUri(mBookId),
                        new String[]{
                                Contract.Publishers.PUBLISHER_NAME
                        },
                        null,
                        null,
                        null);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        switch (loader.getId()) {

            case LOADER_BOOK:
                bindBook(data);
                break;

            case LOADER_AUTHOR:
                bindAuthors(data);
                break;

            case LOADER_BORROW_DETAIL:
                bindBorrowed(data);
                break;

            case LOADER_BORROW_ME_DETAIL: {
                bindBorrowedToMe(data);
                break;
            }

            case BookDetailActivity.LOADER_PUBLISHER: {
                bindPublisher(data);
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void bindBook(Cursor bookCursor) {

        if (!bookCursor.moveToFirst()) {
            return;
        }

        mBook = new Book.Builder()
                .setTitle(bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_TITLE)))
                .setSubtitle(bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_SUBTITLE)))
                .setIsbn(bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_ISBN)))
                .setWish(bookCursor.getInt(bookCursor.getColumnIndex(Contract.Books.BOOK_WISH_LIST)) == 1)
                .setBorrowedToMe(bookCursor.getInt(bookCursor.getColumnIndex(Contract.Books.BOOK_BORROWED_TO_ME)) == 1)
                .setBorrowed(bookCursor.getInt(bookCursor.getColumnIndex(Contract.Books.BOOK_BORROWED)) == 1)
                .setImageFilePath(bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_IMAGE_FILE)))
                .setFirebaseReference(bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_REFERENCE)))
                .build();

        File imageFile = mBook.imageFilePath == null ? null : new File(mBook.imageFilePath);

        collapsingToolbarLayout.setTitle(mBook.title);

        // TODO: do not use getMeasuredXXX
        int width = cover.getMeasuredWidth();
        int height = cover.getMeasuredHeight();
        if (width == 0 && height == 0) {
            Picasso.with(this)
                    .load(R.drawable.navigation_back)
                    .into(cover);
        } else {
            Picasso.with(this)
                    .load(imageFile != null && imageFile.exists() ? imageFile : null)
                    .placeholder(R.drawable.navigation_back)
                    .resize(width, height)
                    .centerInside()
                    .into(cover);
        }
//        TODO: uncomment this in order to display other details
//        if (mBook.wish) {
        tabs.setVisibility(View.GONE);
//        }

        for (Fragment fragment : mContents) {
            if (fragment instanceof BookDatabaseCallbacks) {
                ((BookDatabaseCallbacks) fragment).onBookGet(mBook);
            }
        }

        invalidateOptionsMenu();
    }

    private void bindAuthors(Cursor authorsCursor) {
        Author[] authors = null;
        if (authorsCursor != null && authorsCursor.getCount() > 0 && authorsCursor.moveToFirst()) {
            authors = new Author[authorsCursor.getCount()];
            for (int i = 0; i < authorsCursor.getCount(); i++, authorsCursor.moveToNext()) {
                Author author = new Author.Builder()
                        .setName(authorsCursor.getString(authorsCursor.getColumnIndex(Contract.Authors.AUTHOR_NAME)))
                        .build();
                authors[i] = author;
            }
        }

        for (Fragment fragment : mContents) {
            if (fragment instanceof BookDatabaseCallbacks) {
                ((BookDatabaseCallbacks) fragment).onAuthorsGet(authors);
            }
        }
    }

    private void bindBorrowed(Cursor cursor) {

        Borrowed borrowed = null;

        if (cursor.moveToFirst() && cursor.getCount() != 0) {
            borrowed = new Borrowed.Builder()
                    .setId(cursor.getLong(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_ID)))
                    .setFrom(cursor.getLong(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_DATE_BORROWED)))
                    .setTo(cursor.getLong(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_DATE_RETURNED)))
                    .setName(cursor.getString(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_NAME)))
                    .build();
        }

        for (Fragment fragment : mContents) {
            if (fragment instanceof BookDatabaseCallbacks) {
                ((BookDatabaseCallbacks) fragment).onBorrowedGet(borrowed);
            }
        }
    }

    private void bindBorrowedToMe(Cursor cursor) {

        BorrowedToMe borrowedToMe = null;

        if (cursor.moveToFirst() && cursor.getCount() != 0) {
            borrowedToMe = new BorrowedToMe.Builder()
                    .setId(cursor.getLong(cursor.getColumnIndex(Contract.BorrowMeInfo.BORROW_ID)))
                    .setName(cursor.getString(cursor.getColumnIndex(Contract.BorrowMeInfo.BORROW_NAME)))
                    .setDateBorrowed(cursor.getLong(cursor.getColumnIndex(Contract.BorrowMeInfo.BORROW_DATE_BORROWED)))
                    .build();
        }

        for (Fragment fragment : mContents) {
            if (fragment instanceof BookDatabaseCallbacks) {
                ((BookDatabaseCallbacks) fragment).onBorrowedToMeGet(borrowedToMe);
            }
        }
    }

    private void bindPublisher(Cursor publisherCursor) {

        Publisher publisher = null;

        if (publisherCursor.moveToFirst() && publisherCursor.getCount() != 0) {
            publisher = new Publisher.Builder()
                    .setName(publisherCursor.getString(publisherCursor.getColumnIndex(Contract.Publishers.PUBLISHER_NAME)))
                    .build();
        }

        for (Fragment fragment : mContents) {
            if (fragment instanceof BookDatabaseCallbacks) {
                ((BookDatabaseCallbacks) fragment).onPublisherGet(publisher);
            }
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, mContents[tab.getPosition()]).commit();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        // play with fragments here
        if (mShowBorrowDialog) {
            mShowBorrowDialog = false;
            searchContact();
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private void searchContact() {
        BorrowBookDialog.getInstance(mBookId).show(getSupportFragmentManager(), null);
    }

    public interface BookDatabaseCallbacks {
        void onBookGet(Book book);

        void onAuthorsGet(Author[] author);

        void onPublisherGet(Publisher publisher);

        void onLibraryGet(Library library);

        void onBorrowedGet(Borrowed borrowed);

        void onBorrowedToMeGet(BorrowedToMe borrowedToMe);
    }


    /**
     * Helper interface for creating Contact Requests
     */
    @SuppressWarnings("unused")
    public interface ContactRequest {

        // WHERE Statements
        String GENERAL_SELECTION = ContactsContract.Data.CONTACT_ID + " = ?";
        String PHONE_SELECTION = GENERAL_SELECTION + " AND " + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'";

        // selection columns
        String[] GENERAL_COLUMNS = new String[]{ContactsContract.Data.DISPLAY_NAME};
        String[] PHONE_COLUMNS = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};

        int NAME_COLUMN = 0;
    }
}
