package xyz.kandrac.library.mvp.presenter;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import javax.inject.Inject;

import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.mvp.view.bookdetail.BookDetailView;

import static xyz.kandrac.library.model.Contract.BooksColumns.*;
import static xyz.kandrac.library.mvp.view.bookdetail.BookDetailView.*;

/**
 * Presenter for getting data about book
 * <p>
 * Created by jan on 26.12.2016.
 */
public class BookBasicDetailPresenter implements Presenter<BookDetailView> {

    private BookDetailView mView;
    private long bookId;
    private String bookReference;

    @Inject
    BookBasicDetailPresenter() {
    }

    @Override
    public void setView(BookDetailView view) {
        mView = view;
    }

    /**
     * Sets book id this presenter is connected to
     *
     * @param bookId to be set
     */
    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    /**
     * Loads all data about book
     */
    public void loadBasicBookData() {
        mView.getActivity().getLoaderManager().initLoader(LOADER_BASIC, null, callbacks);
        mView.getActivity().getLoaderManager().initLoader(LOADER_BASIC_LIBRARY, null, callbacks);
        mView.getActivity().getLoaderManager().initLoader(LOADER_BASIC_AUTHORS, null, callbacks);
        mView.getActivity().getLoaderManager().initLoader(LOADER_BASIC_PUBLISHER, null, callbacks);
        mView.getActivity().getLoaderManager().initLoader(LOADER_BASIC_BORROW, null, callbacks);
        mView.getActivity().getLoaderManager().initLoader(LOADER_BASIC_BORROW_ME, null, callbacks);
    }

    /**
     * Loads just personal data about given book that are returned
     * in {@link BookDetailView#onPersonalDataLoaded(Cursor)} method
     */
    public void loadPersonalBookData() {
        mView.getActivity().getLoaderManager().initLoader(LOADER_OTHER, null, callbacks);
    }

    private LoaderManager.LoaderCallbacks<Cursor> callbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            switch (i) {
                case LOADER_BASIC:
                    return new CursorLoader(mView.getActivity(), Contract.Books.buildBookUri(bookId),
                            null, null, null, null);

                case LOADER_BASIC_AUTHORS:

                    return new CursorLoader(mView.getActivity(), Contract.Books.buildBookWithAuthorUri(bookId),
                            null, null, null, null);

                case LOADER_BASIC_PUBLISHER:

                    return new CursorLoader(mView.getActivity(), Contract.Books.buildBookPublisherUri(bookId),
                            null, null, null, null);

                case LOADER_BASIC_LIBRARY:
                    return new CursorLoader(mView.getActivity(), Contract.Books.buildBookLibraryUri(bookId),
                            null, null, null, null);

                case LOADER_BASIC_BORROW:

                    return new CursorLoader(mView.getActivity(), Contract.Books.buildBorrowInfoUri(bookId),
                            null, null, null, null);

                case LOADER_BASIC_BORROW_ME:

                    return new CursorLoader(mView.getActivity(), Contract.Books.buildBorrowedToMeInfoUri(bookId),
                            null, null, null, null);

                case LOADER_OTHER:
                    return new CursorLoader(mView.getActivity(), Contract.Books.buildBookUri(bookId),
                            new String[]{BOOK_PROGRESS, BOOK_MY_SCORE, BOOK_QUOTE, BOOK_NOTES, BOOK_REFERENCE},
                            null, null, null
                    );

                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (cursor == null || !cursor.moveToFirst()) {
                //noinspection WrongConstant - loader id always equals to one of required constants
                mView.onDataLoadEmpty(loader.getId());
                return;
            }

            switch (loader.getId()) {
                case LOADER_BASIC:
                    bookReference = cursor.getString(cursor.getColumnIndex(BOOK_REFERENCE));
                    mView.onBasicDataLoaded(cursor);
                    break;

                case LOADER_BASIC_AUTHORS:
                    String[] authors = new String[cursor.getCount()];
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        authors[i] = cursor.getString(cursor.getColumnIndex(Contract.Authors.AUTHOR_NAME));
                    }
                    mView.onAuthorsDataLoaded(authors);
                    break;

                case LOADER_BASIC_PUBLISHER:
                    String pubName = cursor.getString(cursor.getColumnIndex(Contract.Publishers.PUBLISHER_NAME));
                    mView.onPublisherLoaded(pubName);
                    break;

                case LOADER_BASIC_LIBRARY:
                    String libName = cursor.getString(cursor.getColumnIndex(Contract.Libraries.LIBRARY_NAME));
                    mView.onLibraryLoaded(libName);
                    break;

                case LOADER_BASIC_BORROW:

                    long borrowedId = cursor.getLong(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_ID));
                    long borrowedFrom = cursor.getLong(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_DATE_BORROWED));
                    long borrowedTo = cursor.getLong(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_DATE_RETURNED));
                    String borrowedName = cursor.getString(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_NAME));

                    mView.onBorrowDataLoaded(borrowedId, borrowedFrom, borrowedTo, borrowedName);
                    break;

                case LOADER_BASIC_BORROW_ME:

                    long borrowMeId = cursor.getLong(cursor.getColumnIndex(Contract.BorrowMeInfo.BORROW_ID));
                    String borrowMeName = cursor.getString(cursor.getColumnIndex(Contract.BorrowMeInfo.BORROW_NAME));
                    long borrowMeDate = cursor.getLong(cursor.getColumnIndex(Contract.BorrowMeInfo.BORROW_DATE_BORROWED));

                    mView.onBorrowMeDataLoaded(borrowMeId, borrowMeName, borrowMeDate);
                    break;

                case LOADER_OTHER:
                    bookReference = cursor.getString(cursor.getColumnIndex(BOOK_REFERENCE));
                    mView.onPersonalDataLoaded(cursor);
                    break;
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    /**
     * Save Book data. If accessible book firebase reference is automatically added
     *
     * @param cv to store
     */
    public void save(@NonNull ContentValues cv) {
        mView.getActivity()
                .getContentResolver()
                .update(TextUtils.isEmpty(bookReference)
                        ? Contract.Books.buildBookUri(bookId)
                        : Contract.Books.buildBookFirebaseUri(bookReference), cv, null, null);
    }

    /**
     * @return current book id
     * @see #setBookId(long)
     */
    public long getBookId() {
        return bookId;
    }
}
