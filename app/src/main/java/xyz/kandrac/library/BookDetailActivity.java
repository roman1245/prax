package xyz.kandrac.library;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.File;

import xyz.kandrac.library.dialogs.BorrowBookDialog;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.mvp.view.EditBookActivity;
import xyz.kandrac.library.mvp.view.bookdetail.BookDetailBasicFragment;
import xyz.kandrac.library.mvp.view.bookdetail.BookDetailOthersFragment;
import xyz.kandrac.library.utils.LogUtils;

/**
 * Shows all the details about book based on its ID from {@link #EXTRA_BOOK_ID}.
 * <p/>
 * Created by VizGhar on 18.10.2015.
 */
public class BookDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, TabLayout.OnTabSelectedListener {

    public static final String LOG_TAG = BookDetailActivity.class.getName();
    public static final String EXTRA_BOOK_ID = "book_id_extra";

    // PERMISSIONS
    static final int PICK_CONTACT_PERMISSION = 1;
    private static final int LOADER_BOOK = 555;

    private Long mBookId;

    private Fragment[] mContents;

    // Layout binding
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView cover;
    private TabLayout tabs;

    private boolean mShowBorrowDialog = false;
    public FirebaseAuth mAuth;

    private boolean wish;
    private boolean borrowed;
    private boolean borrowedToMe;

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

        borrowItem.setVisible(!wish && !borrowed && !borrowedToMe);
        moveItem.setVisible(wish);

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
                return new CursorLoader(this, Contract.Books.buildBookUri(mBookId), null, null, null, null);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        switch (loader.getId()) {
            case LOADER_BOOK:
                if (data != null && data.moveToFirst()) {
                    bindBook(data);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void bindBook(Cursor bookCursor) {

        String title = bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_TITLE));
        String image = bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_IMAGE_FILE));
        wish = bookCursor.getInt(bookCursor.getColumnIndex(Contract.Books.BOOK_WISH_LIST)) == 1;
        borrowed = bookCursor.getInt(bookCursor.getColumnIndex(Contract.Books.BOOK_BORROWED)) == 1;
        borrowedToMe = bookCursor.getInt(bookCursor.getColumnIndex(Contract.Books.BOOK_BORROWED_TO_ME)) == 1;
        File imageFile = image == null ? null : new File(image);

        collapsingToolbarLayout.setTitle(title);

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

        if (wish) {
            tabs.setVisibility(View.GONE);
        }

        invalidateOptionsMenu();
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
}
