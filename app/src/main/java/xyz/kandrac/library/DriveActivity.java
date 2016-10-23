package xyz.kandrac.library;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.firebase.FirebaseBook;
import xyz.kandrac.library.model.firebase.References;

import static xyz.kandrac.library.model.Contract.Books.FULL_BOOK_ID;

/**
 * Created by Jan Kandrac on 14.7.2016.
 */
public class DriveActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = DriveActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(122, null, this);
    }

    @Override
    public CursorLoader onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, Contract.Special.TABLE_URI, new String[]{
                Contract.Books.BOOK_TITLE,
                FULL_BOOK_ID,
                Contract.Books.BOOK_ISBN,
                Contract.Books.BOOK_PUBLISHED,
                Contract.Books.BOOK_BORROWED}, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case 122:

                // get user identifier
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth == null || auth.getCurrentUser() == null) {
                    return;
                }
                String userUid = auth.getCurrentUser().getUid();


                // store parsed data to database
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                // parse cursor data
                cursor.moveToFirst();
                do {
                    String id = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_ID));
                    String title = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_TITLE));
                    String isbn = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_ISBN));
                    String description = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_DESCRIPTION));
                    String subtitle = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_SUBTITLE));
                    String published = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_PUBLISHED));
                    String authors = cursor.getString(cursor.getColumnIndex(Contract.Authors.AUTHOR_NAME));
                    String publisher = cursor.getString(cursor.getColumnIndex(Contract.Publishers.PUBLISHER_NAME));

                    database.getReference()
                            .child(References.USERS_REFERENCE).child(userUid)
                            .child(References.BOOKS_REFERENCE).child(id)
                            .setValue(new FirebaseBook(title, id, isbn, description, subtitle, published, authors, publisher));

                } while (cursor.moveToNext());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
