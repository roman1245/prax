package xyz.kandrac.library.utils;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import xyz.kandrac.library.BookDetailActivity;
import xyz.kandrac.library.R;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.Database;
import xyz.kandrac.library.model.DatabaseUtils;
import xyz.kandrac.library.model.firebase.References;

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
 * @see AdapterChangedListener
 */
public class BookCursorAdapter extends RecyclerView.Adapter<BookCursorAdapter.ViewHolder> implements LoaderManager.LoaderCallbacks<Cursor> {

    // Field state possible values
    public static final int ANY = -1;
    public static final int TRUE = 1;
    public static final int FALSE = 0;

    // Holds list of selected positions in multi-select mode
    private Set<Integer> selectedPositions = new HashSet<>();

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
            case ANY:
                return "";
            default:
                return null;
        }
    }

    /**
     * Interface for listening changes in Cursor length inside {@link BookCursorAdapter}.
     */
    public interface AdapterChangedListener {

        /**
         * Invoked in case count of items in adapter is changed. This will not be invoked in case
         * adapter is changed and count remains same. Be cautious in such case.
         *
         * @param newCount count of items currently in adapter
         */
        void onCountChanged(int newCount);

        void onMultiSelectStart();

        void onMultiSelectEnd();
    }

    /**
     * Default (and currently only used) projection for database query
     */
    private static final String[] PROJECTION = new String[]{
            Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID,
            Contract.Books.BOOK_TITLE,
            Contract.Books.BOOK_WISH_LIST,
            Contract.Books.BOOK_BORROWED,
            Contract.Books.BOOK_BORROWED_TO_ME,
            Contract.Books.BOOK_IMAGE_FILE,
            Contract.Books.BOOK_REFERENCE,
            DatabaseUtils.getConcat(Contract.Authors.AUTHOR_NAME, Contract.ConcatAliases.AUTHORS_CONCAT_ALIAS)};

    private Cursor mCursor;                         // Cursor with current data
    private int mLastCount = -1;                    // Last count of books in adapter
    private String mSearchQuery = "";               // Filter is based on this search query
    private AdapterChangedListener mListener;    // Listener for book counter
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
        mCursor = null;
        notifyDataSetChanged();
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
    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView title;
        private TextView subtitle;
        private ImageView wishList;
        private ImageView borrowed;
        private ImageView borrowedToMe;

        private ViewHolder(View rowView) {
            super(rowView);
            image = (ImageView) rowView.findViewById(R.id.list_item_book_image);
            title = (TextView) rowView.findViewById(R.id.list_item_book_title);
            subtitle = (TextView) rowView.findViewById(R.id.list_item_book_subtitle);
            wishList = (ImageView) rowView.findViewById(R.id.list_item_book_wish_list);
            borrowed = (ImageView) rowView.findViewById(R.id.list_item_book_borrowed);
            borrowedToMe = (ImageView) rowView.findViewById(R.id.list_item_book_borrowed_to_me);
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
        final boolean wishList = mCursor.getInt(mCursor.getColumnIndex(Contract.Books.BOOK_WISH_LIST)) == 1;
        final boolean borrowed = mCursor.getInt(mCursor.getColumnIndex(Contract.Books.BOOK_BORROWED)) == 1;
        final boolean borrowedToMe = mCursor.getInt(mCursor.getColumnIndex(Contract.Books.BOOK_BORROWED_TO_ME)) == 1;

        holder.title.setText(bookTitle);
        holder.subtitle.setText(authors);
        holder.wishList.setVisibility(wishList ? View.VISIBLE : View.GONE);
        holder.borrowed.setVisibility(borrowed ? View.VISIBLE : View.GONE);
        holder.borrowedToMe.setVisibility(borrowedToMe ? View.VISIBLE : View.GONE);

        if (!TextUtils.isEmpty(image)) {
            DisplayUtils.displayScaledImage(mActivity, image, holder.image);
        } else {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
            boolean conservative = sharedPref.getBoolean(SettingsFragment.KEY_PREF_CONSERVATIVE_ENABLED, true);
            if (!conservative) {
                int[] colors = mActivity.getResources().getIntArray(R.array.md_colors_300);
                holder.image.setBackgroundColor(colors[(int) (bookId % colors.length)]);
            }
        }
        holder.itemView.setSelected(selectedPositions.contains(position));

        holder.itemView.setOnLongClickListener(new MultiSelectLongClickListener(position));
        holder.itemView.setOnClickListener(new MultiSelectClickListener(position, bookId));
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public int getSelectedItemCount() {
        return selectedPositions.size();
    }

    private class MultiSelectLongClickListener implements View.OnLongClickListener {

        int position;

        MultiSelectLongClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onLongClick(View view) {
            if (selectedPositions.contains(position)) {
                selectedPositions.remove(position);
                if (selectedPositions.size() == 0) {
                    mListener.onMultiSelectEnd();
                }
            } else {
                if (selectedPositions.size() == 0) {
                    mListener.onMultiSelectStart();
                }
                selectedPositions.add(position);
            }
            notifyItemChanged(position);
            return true;
        }
    }

    private class MultiSelectClickListener implements View.OnClickListener {

        int position;
        long bookId;

        MultiSelectClickListener(int position, Long bookId) {
            this.position = position;
            this.bookId = bookId;
        }

        @Override
        public void onClick(View view) {
            if (selectedPositions.size() > 0) {
                // multi-select mode
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position);
                    if (selectedPositions.size() == 0) {
                        mListener.onMultiSelectEnd();
                    }
                } else {
                    selectedPositions.add(position);
                }
                notifyItemChanged(position);
            } else {
                Intent intent = new Intent(mActivity, BookDetailActivity.class);
                intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, bookId);
                mActivity.startActivity(intent);
            }
        }
    }

    public void closeMultiSelect() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void deleteSelectedBooks (Context context) {
        for (int position : selectedPositions) {
            mCursor.moveToPosition(position);
            long id = mCursor.getLong(mCursor.getColumnIndex(Contract.Books.BOOK_ID));
            context.getContentResolver().delete(Contract.Books.buildBookUri(id), null, null);
            notifyItemRemoved(position);
        }
        mListener.onMultiSelectEnd();
    }

    /**
     * Use this {@code Builder} to create proper {@link BookCursorAdapter} that can be used with
     * your {@link RecyclerView}.
     */
    public static class Builder {

        // fields
        @FieldState private int wishList = ANY;
        @FieldState private int borrowed = ANY;
        @FieldState private int borrowedToMe = ANY;
        private long publisher = ANY;
        private long author = ANY;
        private long library = ANY;
        private int loaderId = 1;
        private Activity activity;
        private AdapterChangedListener listener;


        public Builder setWishList(@FieldState int wishList) {
            this.wishList = wishList;
            return this;
        }

        public Builder setBorrowed(@FieldState int borrowed) {
            this.borrowed = borrowed;
            return this;
        }

        public Builder setBorrowedToMe(@FieldState int borrowedToMe) {
            this.borrowedToMe = borrowedToMe;
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

        public Builder setListener(AdapterChangedListener listener) {
            this.listener = listener;
            return this;
        }

        public BookCursorAdapter build() {
            BookCursorAdapter result = new BookCursorAdapter();

            String selectionString = " ( " +
                    Contract.Books.BOOK_TITLE + " LIKE ? OR " +
                    Contract.Books.BOOK_SUBTITLE + " LIKE ? OR " +
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

            if (borrowedToMe != ANY) {
                selectionString += " AND " + Contract.Books.BOOK_BORROWED_TO_ME + " = ?";
                selectionArguments.add(getFieldStateStringValue(borrowedToMe));
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
