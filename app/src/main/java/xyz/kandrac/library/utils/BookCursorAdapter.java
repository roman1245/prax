package xyz.kandrac.library.utils;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import xyz.kandrac.library.BookDetailActivity;
import xyz.kandrac.library.R;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.Database;
import xyz.kandrac.library.model.DatabaseUtils;

/**
 * Adapter for {@link RecyclerView} that automatically handles requests for books based on 5
 * identifiers:
 * <ol>
 * <li><strong>Loader ID</strong>: so that no conflicts in loading data will occur</li>
 * <li><strong>Wish List</strong>: whether books belongs to wish list or not</li>
 * <li><strong>Borrowed</strong>: whether books were borrowed or not</li>
 * <li><strong>Author ID</strong>: ID of some of the authors</li>
 * <li><strong>Publisher ID</strong>: ID of book publisher</li>
 * </ol>
 * To properly set those values please use {@link xyz.kandrac.library.utils.BookCursorAdapter.Builder}
 * and all its setter methods.
 * <p>
 * There is also interface provided for getting changes in count of books in cursor currently
 * handled. This can be used when cursor is being freed and you want to handle such situation by
 * displaying placeholder. There is no default behavior provided for such situation. This listener
 * can also be set in mentioned {@code Builder}.
 * <p>
 * Cursor is listening for {@link Contract#BOOKS_AUTHORS_URI} notification uri
 * <p>
 * Created by kandrac on 23/11/15.
 *
 * @see xyz.kandrac.library.utils.BookCursorAdapter.CursorSizeChangedListener
 */
public class BookCursorAdapter extends RecyclerView.Adapter<BookCursorAdapter.ViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    // Field state possible values
    public static final int ANY = -1;
    public static final int TRUE = 1;
    public static final int FALSE = 0;

    /**
     * Based on field state we can identify whether some of search query attributes are mandatory,
     * forbidden or its value is not necessary
     */
    @IntDef({ANY, TRUE, FALSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FieldState {
    }

    /**
     * Based on {@link xyz.kandrac.library.utils.BookCursorAdapter.FieldState} get its String
     * representation needed for database queries
     *
     * @param state to get value from
     * @return state String representation
     */
    private static String getFieldStateStringValue(@FieldState int state) {
        switch (state) {
            case TRUE:
                return "1";
            case FALSE:
                return "0";
            default:
                return null;
        }
    }

    /**
     * Interface for listening changes in Cursor length inside {@link BookCursorAdapter}.
     */
    public interface CursorSizeChangedListener {

        /**
         * Invoked in case count of items in adapter is changed. This will not be invoked in case
         * adapter is changed and count remains same. Be cautious in such case.
         *
         * @param newCount count of items currently in adapter
         */
        void onCountChanged(int newCount);
    }

    /**
     * Default (and currently only used) projection for database query
     */
    private static final String[] PROJECTION = new String[]{
            Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID,
            Contract.Books.BOOK_TITLE,
            Contract.Books.BOOK_IMAGE_FILE,
            DatabaseUtils.getConcat(Contract.Authors.AUTHOR_NAME, Contract.ConcatAliases.AUTHORS_CONCAT_ALIAS)};

    private Cursor mCursor;                         // Cursor with current data
    private int mLastCount = -1;                    // Last count of books in adapter
    private String mSearchQuery = "";               // Filter is based on this search query
    private CursorSizeChangedListener mListener;    // Listener for book counter
    private String mSelectionString;                // Where part of selection query
    private ArrayList<String> mSelectionArguments;  // Selection arguments without filter arguments
    private int mLoaderId;                          // Loader id to be used with queries
    private Activity mActivity;                     // To make operations on

    /**
     * Default constructor with no values set, proper setting of the adapter is handled in
     * {@link xyz.kandrac.library.utils.BookCursorAdapter.Builder}
     */
    private BookCursorAdapter() {
    }

    /**
     * Change query for getting books based on {@code filter} text provided
     *
     * @param filter for searching
     */
    public void setFilter(String filter) {
        mSearchQuery = filter;
        mActivity.getLoaderManager().restartLoader(mLoaderId, null, this);
    }

    /**
     * Merges {@link #mSearchQuery} filter string with rest of the selection arguments from
     * {@link #mSelectionArguments}. Filter is intended to be updated multiple times while
     * other selection arguments not (like author id or publisher id). To change search query
     * {@link #setFilter(String)} have to be used
     *
     * @return all selection arguments
     */
    private String[] getSelectionArguments() {
        ArrayList<String> result = new ArrayList<>();
        result.add("%" + mSearchQuery + "%");
        result.add("%" + mSearchQuery + "%");
        result.add("%" + mSearchQuery + "%");
        result.add("%" + mSearchQuery + "%");
        result.addAll(mSelectionArguments);

        String[] arrayResult = new String[mSelectionArguments.size() + 4];
        return result.toArray(arrayResult);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // handle only set loader, because you can receive other loader calls from same activity
        if (id == mLoaderId) {
            return new CursorLoader(
                    mActivity,                  // activity required for cursor loading
                    Contract.BOOKS_AUTHORS_URI, // keep in mind we are listening for books/authors
                    PROJECTION,                 // static projection columns, cannot be edited
                    mSelectionString,           // static selection string from initialization
                    getSelectionArguments(),    // dynamically updated selection arguments
                    null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // handle only set loader, because you can receive other loader calls from same activity
        if (loader.getId() == mLoaderId) {
            mCursor = data;                     // data set changed, don't forget to notify parent
            int count = data.getCount();
            if (mLastCount != count) {          // count changed callback handling
                mLastCount = count;
                if (mListener != null) {
                    mListener.onCountChanged(count);
                }
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mActivity.getLoaderManager().restartLoader(mLoaderId, null, this);
    }

    /**
     * {@link BookCursorAdapter}'s {@code ViewHolder}. Required for using with {@link RecyclerView}
     * and really the best practice in order to be efficient with {@link android.widget.AdapterView}
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView title;
        private TextView subtitle;

        public ViewHolder(View rowView) {
            super(rowView);
            image = (ImageView) rowView.findViewById(R.id.list_item_book_image);
            title = (TextView) rowView.findViewById(R.id.list_item_book_title);
            subtitle = (TextView) rowView.findViewById(R.id.list_item_book_subtitle);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ViewHolder(inflater.inflate(R.layout.list_item_book, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        // get values from cursor on given position
        final Long bookId = mCursor.getLong(mCursor.getColumnIndex(Contract.Books.BOOK_ID));
        final String bookTitle = mCursor.getString(mCursor.getColumnIndex(Contract.Books.BOOK_TITLE));
        final String authors = mCursor.getString(mCursor.getColumnIndex(Contract.ConcatAliases.AUTHORS_CONCAT_ALIAS));
        final String image = mCursor.getString(mCursor.getColumnIndex(Contract.Books.BOOK_IMAGE_FILE));

        // update view with cursor values
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, BookDetailActivity.class);
                intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, bookId);
                mActivity.startActivity(intent);
            }
        });

        holder.title.setText(bookTitle);
        holder.subtitle.setText(authors);
        DisplayUtils.displayScaledImage(mActivity, image, holder.image);
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    /**
     * Use this {@code Builder} to create proper {@link BookCursorAdapter} that can be used with
     * your {@link RecyclerView}.
     */
    public static class Builder {

        // fields
        @FieldState private int wishList = ANY;
        @FieldState private int borrowed = ANY;
        private long publisher = ANY;
        private long author = ANY;
        private long library = ANY;
        private int loaderId = 1;
        private Activity activity;
        private CursorSizeChangedListener listener;


        public Builder setWishList(@FieldState int wishList) {
            this.wishList = wishList;
            return this;
        }

        public Builder setBorrowed(@FieldState int borrowed) {
            this.borrowed = borrowed;
            return this;
        }

        public Builder setPublisher(long publisherId) {
            this.publisher = publisherId;
            return this;
        }

        public Builder setLibrary(long library) {
            this.library = library;
            return this;
        }

        public Builder setAuthor(long authorId) {
            this.author = authorId;
            return this;
        }

        public Builder setLoaderId(int loaderId) {
            this.loaderId = loaderId;
            return this;
        }

        public Builder setActivity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public Builder setListener(CursorSizeChangedListener listener) {
            this.listener = listener;
            return this;
        }

        public BookCursorAdapter build() {
            BookCursorAdapter result = new BookCursorAdapter();

            String selectionString = " ( " +
                    Contract.Books.BOOK_TITLE + " LIKE ? OR " +
                    Contract.Authors.AUTHOR_NAME + " LIKE ? OR " +
                    Contract.Books.BOOK_DESCRIPTION + " LIKE ? OR " +
                    Contract.Books.BOOK_ISBN + " LIKE ?" +
                    ") ";

            ArrayList<String> selectionArguments = new ArrayList<>();

            if (publisher != ANY) {
                selectionString += " AND " + Contract.Books.BOOK_PUBLISHER_ID + " = ? ";
                selectionArguments.add(Long.toString(publisher));
            }

            if (library != ANY) {
                selectionString += " AND " + Contract.Books.BOOK_LIBRARY_ID + " = ? ";
                selectionArguments.add(Long.toString(library));
            }

            if (author != ANY) {
                selectionString += " AND " + Database.Tables.BOOKS_AUTHORS + "." + Contract.BookAuthors.AUTHOR_ID + " = ? ";
                selectionArguments.add(Long.toString(author));
            }

            if (wishList != ANY) {
                selectionString += " AND " + Contract.Books.BOOK_WISH_LIST + " = ? ";
                selectionArguments.add(getFieldStateStringValue(wishList));
            }

            if (borrowed != ANY) {
                selectionString += " AND " + Contract.Books.BOOK_BORROWED + " = ?";
                selectionArguments.add(getFieldStateStringValue(borrowed));
            }

            result.mSelectionString = selectionString;
            result.mSelectionArguments = selectionArguments;
            result.mLoaderId = loaderId;
            result.mActivity = activity;
            result.mListener = listener;

            return result;
        }
    }
}
