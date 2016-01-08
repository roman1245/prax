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
 * Adapter for {@link RecyclerView} that automatically handles requests for books based on 3
 * identifiers:
 * <ol>
 * <li><strong>Loader ID</strong>: so that no conflicts in loading data will occur</li>
 * <li><strong>Wish List</strong>: whether books belongs to wish list or not</li>
 * <li><strong>Borrowed</strong>: whether books were borrowed or not</li>
 * </ol>
 * <p/>
 * There is also provided interface for getting changes in count of books in cursor currently
 * handled. This can be used when cursor is being freed and you want to handle such situation by
 * displaying placeholder. There is no default behavior provided for such situations, but can
 * be extended in future.
 * <p/>
 * Created by kandrac on 23/11/15.
 */
public class BookCursorAdapter extends RecyclerView.Adapter<BookCursorAdapter.BindingHolder> implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final int ANY = -1;
    public static final int TRUE = 1;
    public static final int FALSE = 0;

    @IntDef({ANY, TRUE, FALSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FieldState {
    }

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
     * Interface for listening changes in Cursor length.
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
    private String mSelectionString;                //
    private ArrayList<String> mSelectionArguments;  //
    private int mLoaderId;                          //
    private Activity mActivity;                     //

    /**
     * Default constructor with no values set
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
    }

    public String[] getSelectionArguments() {
        ArrayList<String> result = new ArrayList<>();
        result.add(mSearchQuery);
        result.add(mSearchQuery);
        result.add(mSearchQuery);
        result.add(mSearchQuery);
        result.addAll(mSelectionArguments);

        String[] arrayResult = new String[mSelectionArguments.size() + 4];
        return result.toArray(arrayResult);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == mLoaderId) {
            return new CursorLoader(
                    mActivity,
                    Contract.BOOKS_AUTHORS_URI,
                    PROJECTION,
                    mSelectionString,
                    getSelectionArguments(),
                    null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        int count = data.getCount();
        if (mLastCount != count) {
            mLastCount = count;
            if (mListener != null) {
                mListener.onCountChanged(count);
            }
        }
        notifyDataSetChanged();
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
     * View holder for this adapter
     */
    public class BindingHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView title;
        private TextView subtitle;

        public BindingHolder(View rowView) {
            super(rowView);
            image = (ImageView) rowView.findViewById(R.id.list_item_book_image);
            title = (TextView) rowView.findViewById(R.id.list_item_book_title);
            subtitle = (TextView) rowView.findViewById(R.id.list_item_book_subtitle);
        }
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new BindingHolder(inflater.inflate(R.layout.book_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        mCursor.moveToPosition(position);

        final Long bookId = mCursor.getLong(mCursor.getColumnIndex(Contract.Books.BOOK_ID));
        final String bookTitle = mCursor.getString(mCursor.getColumnIndex(Contract.Books.BOOK_TITLE));
        final String authors = mCursor.getString(mCursor.getColumnIndex(Contract.ConcatAliases.AUTHORS_CONCAT_ALIAS));
        final String image = mCursor.getString(mCursor.getColumnIndex(Contract.Books.BOOK_IMAGE_FILE));

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

    public static class Builder {

        @FieldState
        int wishList = ANY;

        @FieldState
        int borrowed = ANY;

        long publisher = ANY;
        long author = ANY;
        int loaderId = 1;
        Activity activity;
        CursorSizeChangedListener listener;
        String filter = "";


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

        public Builder setAuthor(long authorId) {
            this.author = authorId;
            return this;
        }

        public Builder setLoaderId(int loaderId) {
            this.loaderId = loaderId;
            return this;
        }

        public Builder setFilter(String filter) {
            this.filter = filter;
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
