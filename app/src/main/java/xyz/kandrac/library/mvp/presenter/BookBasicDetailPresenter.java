package xyz.kandrac.library.mvp.presenter;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import javax.inject.Inject;

import xyz.kandrac.library.R;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.mvp.view.bookdetail.BookDetailView;

import static xyz.kandrac.library.model.Contract.BooksColumns.BOOK_MY_SCORE;
import static xyz.kandrac.library.model.Contract.BooksColumns.BOOK_NOTES;
import static xyz.kandrac.library.model.Contract.BooksColumns.BOOK_PROGRESS;
import static xyz.kandrac.library.model.Contract.BooksColumns.BOOK_QUOTE;
import static xyz.kandrac.library.model.Contract.BooksColumns.BOOK_REFERENCE;
import static xyz.kandrac.library.mvp.view.bookdetail.BookDetailView.LOADER_BASIC;
import static xyz.kandrac.library.mvp.view.bookdetail.BookDetailView.LOADER_BASIC_AUTHORS;
import static xyz.kandrac.library.mvp.view.bookdetail.BookDetailView.LOADER_BASIC_BORROW;
import static xyz.kandrac.library.mvp.view.bookdetail.BookDetailView.LOADER_BASIC_BORROW_ME;
import static xyz.kandrac.library.mvp.view.bookdetail.BookDetailView.LOADER_BASIC_GENRE;
import static xyz.kandrac.library.mvp.view.bookdetail.BookDetailView.LOADER_BASIC_LIBRARY;
import static xyz.kandrac.library.mvp.view.bookdetail.BookDetailView.LOADER_BASIC_PUBLISHER;
import static xyz.kandrac.library.mvp.view.bookdetail.BookDetailView.LOADER_OTHER;

/**
 * Presenter for getting data about book
 * <p>
 * Created by jan on 26.12.2016.
 */
public class BookBasicDetailPresenter implements Presenter<BookDetailView> {

    private BookDetailView mView;
    private long bookId;
    private String bookReference;
    private BookShareData shareData = new BookShareData();

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
        mView.getActivity().getLoaderManager().initLoader(LOADER_BASIC_GENRE, null, callbacks);
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

                case LOADER_BASIC_GENRE:

                    return new CursorLoader(mView.getActivity(), Contract.Books.buildBookGenreUri(bookId),
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
                    shareData.title = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_TITLE));
                    shareData.isbn = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_ISBN));
                    mView.onBasicDataLoaded(cursor);
                    break;

                case LOADER_BASIC_AUTHORS:
                    String[] authors = new String[cursor.getCount()];
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        authors[i] = cursor.getString(cursor.getColumnIndex(Contract.Authors.AUTHOR_NAME));
                    }
                    shareData.authors = authors;
                    mView.onAuthorsDataLoaded(authors);
                    break;

                case LOADER_BASIC_PUBLISHER:
                    String pubName = cursor.getString(cursor.getColumnIndex(Contract.Publishers.PUBLISHER_NAME));
                    shareData.publisher = cursor.getString(cursor.getColumnIndex(Contract.Publishers.PUBLISHER_NAME));
                    mView.onPublisherLoaded(pubName);
                    break;

                case LOADER_BASIC_GENRE:
                    String genreName = cursor.getString(cursor.getColumnIndex(Contract.Genres.GENRE_NAME));
                    mView.onGenreLoaded(genreName);
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

    public Intent getShareIntent() {

        if (shareData.title == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(shareData.title);

        if (shareData.authors != null && shareData.authors.length > 0) {
            StringBuilder authors = new StringBuilder();
            for (int i = 0; i < shareData.authors.length; i++) {
                if (i != 0) {
                    authors.append(", ");
                }
                authors.append(shareData.authors[i]);
            }

            sb.append("\n")
                    .append(mView.getActivity().getResources().getQuantityString(R.plurals.book_authors, shareData.authors.length, authors));
        }

        if (!TextUtils.isEmpty(shareData.publisher)) {
            sb.append("\n")
                    .append(mView.getActivity().getString(R.string.book_share_by_publisher, shareData.publisher));
        }

        if (!TextUtils.isEmpty(shareData.isbn)) {
            sb.append("\n")
                    .append(mView.getActivity().getString(R.string.book_share_isbn, shareData.isbn));
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.setType("text/plain");

        return sendIntent;
    }

    private class BookShareData {

        private String isbn;
        private String title;
        private String[] authors;
        private String publisher;

    }
}
