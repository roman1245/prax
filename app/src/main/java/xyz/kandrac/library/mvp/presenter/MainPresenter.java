package xyz.kandrac.library.mvp.presenter;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

import javax.inject.Inject;

import xyz.kandrac.library.BuildConfig;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.DatabaseStoreUtils;
import xyz.kandrac.library.model.firebase.FirebaseBook;
import xyz.kandrac.library.model.firebase.References;
import xyz.kandrac.library.model.obj.Author;
import xyz.kandrac.library.model.obj.Book;
import xyz.kandrac.library.model.obj.Library;
import xyz.kandrac.library.model.obj.Publisher;
import xyz.kandrac.library.mvp.view.MainActivity;
import xyz.kandrac.library.mvp.view.MainView;
import xyz.kandrac.library.utils.IABConfigurator;
import xyz.kandrac.library.utils.LogUtils;
import xyz.kandrac.library.utils.SharedPreferencesManager;

import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_AUTHORS;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_BORROWED_TO_NAME;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_BORROWED_WHEN;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_BORROW_ME_NAME;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_BORROW_ME_WHEN;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_BORROW_NOTIFY;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_DESCRIPTION;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_ISBN;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_LIBRARY;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_PUBLISHED;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_PUBLISHER;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_SUBTITLE;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_TITLE;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_UPDATED_AT;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_WISH_LIST;
import static xyz.kandrac.library.mvp.view.MainActivity.PREFERENCE_PHOTOS_REMOVED;
import static xyz.kandrac.library.mvp.view.MainActivity.WAIT_FOR_DOUBLE_CLICK_BACK;
import static xyz.kandrac.library.mvp.view.MainView.ERROR_TYPE_GOOGLE_API_CONNECTION;
import static xyz.kandrac.library.mvp.view.MainView.ERROR_TYPE_GOOGLE_SIGNIN;
import static xyz.kandrac.library.mvp.view.MainView.INFO_PRESS_AGAIN_TO_LEAVE;
import static xyz.kandrac.library.utils.SharedPreferencesManager.KEY_PREF_DRIVER_BOUGHT;

/**
 * Created by jan on 6.12.2016.
 */
public class MainPresenter implements Presenter<MainView>, LoaderManager.LoaderCallbacks<Cursor>, IABConfigurator.PurchasedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = MainPresenter.class.getName();

    private static final int WISH_COUNT = 8;
    private static final int MY_COUNT = 9;
    private static final int BORROWED_COUNT = 10;
    private static final int FROM_FRIENDS_COUNT = 11;
    private static final int SYNC_LOADER = 12;

    private static final int RC_SIGN_IN = 115;

    @Inject
    SharedPreferencesManager manager;

    private MainView view;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private long mLastFinishingBackClicked;

    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                view.showUserDetail(user.getDisplayName(), user.getEmail(), user.getPhotoUrl());
            } else {
                Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
            }
        }
    };

    @Inject
    MainPresenter() {
    }

    @Override
    public void setView(MainView view) {
        this.view = view;
        mAuth = FirebaseAuth.getInstance();
    }

    public void onStart() {
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void onStop() {
        mAuth.removeAuthStateListener(mAuthListener);
    }

    public void onDestroy() {
        LogUtils.d(LOG_TAG, "Destroying helper.");
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass on the activity result to the helper for handling
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (!result.isSuccess()) {
                view.interact(ERROR_TYPE_GOOGLE_SIGNIN, null);
            } else {
                firebaseAuthWithGoogle(result.getSignInAccount());
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * On Left side of Main Activity there should be NavigationView displayed with fields as
     * Wishlist, My books etc. All this section should have small number with count of those items
     * in it.
     * <p>
     * This methods should be invoked on result:
     * <ul>
     * <li>{@link MainView#onWishListItemsCount(int)}</li>
     * <li>{@link MainView#onBooksFromFriendsCount(int)}</li>
     * <li>{@link MainView#onBorrowedBooksCount(int)}</li>
     * <li>{@link MainView#onMyBooksCount(int)}</li>
     * </ul>
     */
    public void initNavigationView() {
        view.getActivity().getLoaderManager().initLoader(WISH_COUNT, null, this);
        view.getActivity().getLoaderManager().initLoader(MY_COUNT, null, this);
        view.getActivity().getLoaderManager().initLoader(BORROWED_COUNT, null, this);
        view.getActivity().getLoaderManager().initLoader(FROM_FRIENDS_COUNT, null, this);

        view.setLibraryItemVisibility(manager.getBooleanPreference(SharedPreferencesManager.KEY_PREF_LIBRARY_ENABLED, true));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case WISH_COUNT:
                return new CursorLoader(view.getActivity(), Contract.Books.CONTENT_URI, new String[]{"count(*) as c"}, Contract.Books.BOOK_WISH_LIST + " = 1", null, null);
            case MY_COUNT:
                return new CursorLoader(view.getActivity(), Contract.Books.CONTENT_URI, new String[]{"count(*) as c"},
                        Contract.Books.BOOK_WISH_LIST + " = 0 AND " +
                                Contract.Books.BOOK_BORROWED + " = 0 AND " +
                                Contract.Books.BOOK_BORROWED_TO_ME + " = 0", null, null);
            case BORROWED_COUNT:
                return new CursorLoader(view.getActivity(), Contract.Books.CONTENT_URI, new String[]{"count(*) as c"}, Contract.Books.BOOK_BORROWED + " = 1", null, null);
            case FROM_FRIENDS_COUNT:
                return new CursorLoader(view.getActivity(), Contract.Books.CONTENT_URI, new String[]{"count(*) as c"}, Contract.Books.BOOK_BORROWED_TO_ME + " = 1", null, null);
            case SYNC_LOADER:
                long lastSync = manager.getLongPreference(SharedPreferencesManager.KEY_PREF_LAST_CLOUD_SYNC);
                LogUtils.d(LOG_TAG, "last sync " + lastSync);
                return new CursorLoader(
                        view.getActivity(),
                        Contract.Special.TABLE_URI,
                        null,
                        Contract.Books.BOOK_UPDATED_AT + " >= ?",
                        new String[]{Long.toString(lastSync)},
                        null
                );
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case WISH_COUNT:
                view.onWishListItemsCount(data.moveToFirst() ? data.getInt(0) : 0);
                break;
            case MY_COUNT:
                view.onMyBooksCount(data.moveToFirst() ? data.getInt(0) : 0);
                break;
            case BORROWED_COUNT:
                view.onBorrowedBooksCount(data.moveToFirst() ? data.getInt(0) : 0);
                break;
            case FROM_FRIENDS_COUNT:
                view.onBooksFromFriendsCount(data.moveToFirst() ? data.getInt(0) : 0);
                break;
            case SYNC_LOADER: {

                // get user identifier
                if (mAuth.getCurrentUser() == null) {
                    return;
                }

                storeUserToFirebase();

                String userUid = mAuth.getCurrentUser().getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                long lastSync = manager.getLongPreference(SharedPreferencesManager.KEY_PREF_LAST_CLOUD_SYNC);
                long beforeSync = System.currentTimeMillis();
                LogUtils.d(LOG_TAG, "storing " + data.getCount() + " books to cloud");

                storeToCloud(data, database, userUid);
                storeFromCloud(database, userUid, lastSync, beforeSync);

                manager.editPreference(SharedPreferencesManager.KEY_PREF_LAST_CLOUD_SYNC, System.currentTimeMillis());

                break;
            }
        }
    }

    /**
     * @param database
     * @param userUid
     * @param fromTime
     * @param toTime
     */
    private void storeFromCloud(final FirebaseDatabase database, final String userUid, final long fromTime, final long toTime) {
        database.getReference()
                .child(References.USERS_REFERENCE).child(userUid)
                .child(References.BOOKS_REFERENCE).orderByChild(KEY_UPDATED_AT)
                .startAt(fromTime + 1)
                .endAt(toTime)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
                            long updatedAt = (long) bookSnapshot.child(KEY_UPDATED_AT).getValue();

                            // authors
                            String[] authorsSplit = TextUtils.split((String) bookSnapshot.child(KEY_AUTHORS).getValue(), ",");

                            Author[] authors = new Author[authorsSplit.length];
                            for (int i = 0; i < authorsSplit.length; i++) {
                                String authorName = authorsSplit[i].trim();
                                authors[i] = new Author.Builder().setName(authorName).build();
                            }


                            Book.Builder builder = new Book.Builder()
                                    .setFirebaseReference(bookSnapshot.getRef().getKey())
                                    .setTitle((String) bookSnapshot.child(KEY_TITLE).getValue())
                                    .setIsbn((String) bookSnapshot.child(KEY_ISBN).getValue())
                                    .setDescription((String) bookSnapshot.child(KEY_DESCRIPTION).getValue())
                                    .setSubtitle((String) bookSnapshot.child(KEY_SUBTITLE).getValue())
                                    .setPublished((String) bookSnapshot.child(KEY_PUBLISHED).getValue())
                                    .setWish((boolean) bookSnapshot.child(KEY_WISH_LIST).getValue())
                                    .setAuthors(authors)
                                    .setLibrary(new Library.Builder().setName((String) bookSnapshot.child(KEY_LIBRARY).getValue()).build())
                                    .setPublisher(new Publisher.Builder()
                                            .setName((String) bookSnapshot.child(KEY_PUBLISHER).getValue())
                                            .build())
                                    .setUpdatedAt(updatedAt);

                            // borrowed
                            String borrowedName = (String) bookSnapshot.child(KEY_BORROWED_TO_NAME).getValue();
                            if (!TextUtils.isEmpty(borrowedName)) {
                                builder.setBorrowed(true)
                                        .setBorrowedTo(borrowedName)
                                        .setBorrowedToNotify((long) bookSnapshot.child(KEY_BORROW_NOTIFY).getValue())
                                        .setBorrowedToWhen((long) bookSnapshot.child(KEY_BORROWED_WHEN).getValue());
                            } else {
                                builder.setBorrowed(false);
                            }

                            // borrowed to me
                            String borrowedToMeName = (String) bookSnapshot.child(KEY_BORROW_ME_NAME).getValue();
                            if (!TextUtils.isEmpty(borrowedToMeName)) {
                                builder.setBorrowedToMe(true)
                                        .setBorrowedToMeName(borrowedToMeName)
                                        .setBorrowedToMeWhen((long) bookSnapshot.child(KEY_BORROW_ME_WHEN).getValue());
                            } else {
                                builder.setBorrowedToMe(false);
                            }

                            DatabaseStoreUtils.saveBook(view.getActivity().getContentResolver(), builder.build());

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void storeToCloud(Cursor data, FirebaseDatabase database, String userUid) {

        // parse cursor data
        if (!data.moveToFirst()) {
            return;
        }
        do {
            long id = data.getLong(data.getColumnIndex(Contract.Books.BOOK_ID));
            String dbReference = data.getString(data.getColumnIndex(Contract.Books.BOOK_REFERENCE));
            String title = data.getString(data.getColumnIndex(Contract.Books.BOOK_TITLE));
            String isbn = data.getString(data.getColumnIndex(Contract.Books.BOOK_ISBN));
            String description = data.getString(data.getColumnIndex(Contract.Books.BOOK_DESCRIPTION));
            String subtitle = data.getString(data.getColumnIndex(Contract.Books.BOOK_SUBTITLE));
            String published = data.getString(data.getColumnIndex(Contract.Books.BOOK_PUBLISHED));
            String authors = data.getString(data.getColumnIndex(Contract.Authors.AUTHOR_NAME));
            String publisher = data.getString(data.getColumnIndex(Contract.Publishers.PUBLISHER_NAME));
            String library = data.getString(data.getColumnIndex(Contract.Libraries.LIBRARY_NAME));
            String borrowedToName = data.getString(data.getColumnIndex(Contract.BorrowInfo.BORROW_NAME));
            long borrowedWhen = data.getLong(data.getColumnIndex(Contract.BorrowInfo.BORROW_DATE_BORROWED));
            long borrowNotify = data.getLong(data.getColumnIndex(Contract.BorrowInfo.BORROW_NEXT_NOTIFICATION));

            String borrowedToMeName = data.getString(data.getColumnIndex(Contract.BorrowMeInfo.BORROW_NAME));
            long borrowedToMeWhen = data.getLong(data.getColumnIndex(Contract.BorrowMeInfo.BORROW_DATE_BORROWED));

            boolean wish = data.getInt(data.getColumnIndex(Contract.Books.BOOK_WISH_LIST)) == 1;
            long updatedAt = data.getLong(data.getColumnIndex(Contract.Books.BOOK_UPDATED_AT));

            FirebaseBook result = new FirebaseBook(title, isbn, description, subtitle, published, authors, publisher, updatedAt, library, wish, borrowedToName, borrowedWhen, borrowNotify, borrowedToMeName, borrowedToMeWhen);

            if (TextUtils.isEmpty(dbReference)) {
                DatabaseReference reference = database.getReference()
                        .child(References.USERS_REFERENCE).child(userUid)
                        .child(References.BOOKS_REFERENCE).push();

                dbReference = reference.getKey();

                reference.setValue(result);

                ContentValues cv = new ContentValues();
                cv.put(Contract.Books.BOOK_REFERENCE, dbReference);
                view.getActivity().getContentResolver().update(Contract.Books.buildBookUri(id), cv, null, null);
            } else {
                database.getReference()
                        .child(References.USERS_REFERENCE).child(userUid)
                        .child(References.BOOKS_REFERENCE).child(dbReference)
                        .setValue(result);
            }
        } while (data.moveToNext());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onDrivePurchased(boolean purchased) {
        manager.editPreference(KEY_PREF_DRIVER_BOUGHT, purchased);
        evaluatePurchases(purchased);
    }

    private void evaluatePurchases(boolean drivePurchased) {
        if (!drivePurchased && mAuth.getCurrentUser() != null) {
            view.setDriveVisibility(true);
        } else {
            view.setDriveVisibility(false);
        }

        if (drivePurchased && mAuth.getCurrentUser() != null) {
            LogUtils.d(LOG_TAG, "starting sync");
            view.getActivity().getLoaderManager().initLoader(SYNC_LOADER, null, this);
        }
    }

    public void authenticate() {
        if (mAuth.getCurrentUser() == null) {
            try {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                view.getActivity().startActivityForResult(signInIntent, RC_SIGN_IN);
            } catch (Exception ex) {
                view.interact(ERROR_TYPE_GOOGLE_SIGNIN, null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(view.getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            view.interact(ERROR_TYPE_GOOGLE_SIGNIN, null);
                        }
                    }
                });
    }

    private void storeUserToFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference()
                .child(References.USERS_REFERENCE).child(user.getUid());

        reference
                .child("email")
                .setValue(user.getEmail());

        reference
                .child("name")
                .setValue(user.getDisplayName());

        if (user.getPhotoUrl() != null) {
            reference
                    .child("photo")
                    .setValue(user.getPhotoUrl().toString());
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        view.interact(ERROR_TYPE_GOOGLE_API_CONNECTION, connectionResult.getErrorMessage());
    }


    /**
     * Handle back if possible.
     *
     * @return true if back not handled
     */
    public boolean evaluateBack(DrawerLayout drawerLayout, NavigationView navigation) {

        if (drawerLayout.isDrawerOpen(navigation)) {
            // close drawer first (do not give focus to search)
            drawerLayout.closeDrawers();
        } else {
            // don't close immediately
            long currentTime = System.currentTimeMillis();
            if (currentTime > mLastFinishingBackClicked + WAIT_FOR_DOUBLE_CLICK_BACK) {
                mLastFinishingBackClicked = currentTime;
                view.interact(INFO_PRESS_AGAIN_TO_LEAVE, null);
            } else {
                // handle back standard way
                return true;
            }
        }

        return false;
    }


    /**
     * Remove unused photos from disk if they are not referenced in books table
     */
    @Deprecated
    public void removeUnusedPhotosIfNeeded() {
        long lastTimeRemoval = PreferenceManager.getDefaultSharedPreferences(view.getActivity()).getLong(PREFERENCE_PHOTOS_REMOVED, 0);

        if (System.currentTimeMillis() - lastTimeRemoval < 60_000 * 60 * 24) {
            return;
        }

        File imageDirectory = view.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (imageDirectory != null) {
            File[] files = imageDirectory.listFiles();

            new AsyncTask<File, Integer, Void>() {

                @Override
                protected Void doInBackground(File... params) {

                    int count = 0;

                    for (File file : params) {
                        String filePath = file.getAbsolutePath();

                        // search for book with given file name
                        Cursor cursor = view.getActivity().getContentResolver().query(
                                Contract.Books.CONTENT_URI,
                                new String[]{Contract.Books.BOOK_ID},
                                Contract.Books.BOOK_IMAGE_FILE + " = ?",
                                new String[]{filePath},
                                null
                        );

                        // remove book if found
                        if (cursor == null || cursor.getCount() == 0) {
                            if (file.delete()) {
                                count++;
                            }
                        } else {
                            cursor.close();
                        }
                    }

                    LogUtils.d(MainActivity.class.getSimpleName(), "deleted " + count + " files");

                    return null;
                }

            }.execute(files);
        }

        PreferenceManager.getDefaultSharedPreferences(view.getActivity()).edit().putLong(PREFERENCE_PHOTOS_REMOVED, System.currentTimeMillis()).apply();
    }

    public void checkNews() {
        if (manager.getIntPreference(SharedPreferencesManager.KEY_PREF_NEWS_VERSION) < BuildConfig.NEWS_VERSION) {
            manager.editPreference(SharedPreferencesManager.KEY_PREF_NEWS_VERSION, BuildConfig.NEWS_VERSION);
            if (BuildConfig.NEWS_SHOW) {
                view.displayNews();
            }
        }
    }
}
