package xyz.kandrac.library;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.utils.BookCursorAdapter;

/**
 * Fragment with list of all books without any pre scripted selection. This fragment also contains
 * {@link FloatingActionButton} for basic actions.
 * <p/>
 * Created by kandrac on 20/10/15.
 */
public class BookListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Searchable {

    private static final String EXTRA_WISH_LIST = "wish_list";
    private static final String EXTRA_BORROWED = "borrowed";
    private static final String EXTRA_NO_FILTER = "no_filter";
    private static final String EXTRA_ADD_BUTTON = "button";
    private static final String EXTRA_LOADER_ID = "loader_id";

    private static final String SELECTION = Contract.Books.BOOK_BORROWED + " = ? AND " +
            Contract.Books.BOOK_WISH_LIST + " = ? AND " +
            " ( " +
            Contract.Books.BOOK_TITLE + " LIKE ? OR " +
            Contract.Books.BOOK_AUTHORS_READ + " LIKE ? OR " +
            Contract.Books.BOOK_DESCRIPTION + " LIKE ? OR " +
            Contract.Books.BOOK_ISBN + " LIKE ?" +
            ") ";

    private static final String NO_FILTER_SELECTION = Contract.Books.BOOK_TITLE + " LIKE ? OR " +
            Contract.Books.BOOK_AUTHORS_READ + " LIKE ? OR " +
            Contract.Books.BOOK_DESCRIPTION + " LIKE ? OR " +
            Contract.Books.BOOK_ISBN + " LIKE ?";

    private int mLoaderId;
    private boolean mAddButton;

    private boolean mWishList;
    private boolean mBorrowed;
    private boolean mNoFilter;

    @Bind(R.id.list)
    public RecyclerView list;

    @Bind(R.id.fab)
    public FloatingActionButton mFab;

    @Bind(R.id.list_empty)
    public TextView mEmpty;

    @OnClick(R.id.fab)
    public void addItem(View view) {
        Intent intent = new Intent(getActivity(), EditBookActivity.class);
        intent.putExtra(EditBookActivity.EXTRA_WISH_LIST, mWishList);
        startActivity(intent);
    }

    private BookCursorAdapter adapter;
    private String searchQuery = "";

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getInstance() {
        BookListFragment result = new BookListFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(EXTRA_ADD_BUTTON, true);
        arguments.putBoolean(EXTRA_NO_FILTER, true);
        arguments.putInt(EXTRA_LOADER_ID, MainActivity.BOOK_LIST_LOADER);
        result.setArguments(arguments);
        return result;
    }

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getBorrowedBooksInstance() {
        return getInstance(false, true, false, MainActivity.BORROWED_BOOK_LIST_LOADER);
    }

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getWishListBooksInstance() {
        return getInstance(true, false, true, MainActivity.WISH_LIST_BOOK_LIST_LOADER);
    }

    /**
     * Get instance of {@link BookListFragment} for setting all custom fields. {@code title} hold
     * the title representing this fragments content (for example: old books, borrowed books, all
     * books etc.)
     * <p/>
     * {@code filter} should be closely related to {@code title} because it contains
     * string that will be added to search selection String and specifies which books exactly will
     * be shown. Always use column names from {@link xyz.kandrac.library.model.Contract.BorrowInfo}
     * or {@link xyz.kandrac.library.model.Contract.Books}
     * <p/>
     * {@code addButtonVisible} determines whether button for adding new books is visible or not
     * <p/>
     * {@code loaderId} is unique id that will be used with this instance only. It should not be
     * same for 2 or more fragments inside one activity.
     *
     * @param wishList         if items from wish-list should be displayed
     * @param borrowed         if borrowed items should be displayed
     * @param addButtonVisible add button visibility
     * @return instance
     */
    public static BookListFragment getInstance(boolean wishList, boolean borrowed, boolean addButtonVisible, int loaderId) {
        BookListFragment result = new BookListFragment();
        Bundle arguments = new Bundle();

        arguments.putBoolean(EXTRA_WISH_LIST, wishList);
        arguments.putBoolean(EXTRA_BORROWED, borrowed);
        arguments.putBoolean(EXTRA_ADD_BUTTON, addButtonVisible);
        arguments.putBoolean(EXTRA_NO_FILTER, false);
        arguments.putInt(EXTRA_LOADER_ID, loaderId);

        result.setArguments(arguments);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();

        mWishList = arguments.getBoolean(EXTRA_WISH_LIST);
        mBorrowed = arguments.getBoolean(EXTRA_BORROWED);
        mAddButton = arguments.getBoolean(EXTRA_ADD_BUTTON);
        mNoFilter = arguments.getBoolean(EXTRA_NO_FILTER);
        mLoaderId = arguments.getInt(EXTRA_LOADER_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.book_list_fragment, container, false);
        ButterKnife.bind(this, result);

        adapter = new BookCursorAdapter(getActivity());
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(adapter);
        mFab.setVisibility(mAddButton ? View.VISIBLE : View.GONE);
        mEmpty.setText(mAddButton ? R.string.book_list_empty : R.string.book_borrowed_list_empty);

        // Init database loading
        getActivity().getLoaderManager().initLoader(mLoaderId, null, this);
        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == mLoaderId) {
            return new CursorLoader(
                    getActivity(),
                    Contract.Books.CONTENT_URI,
                    new String[]{
                            Contract.Books.BOOK_ID,
                            Contract.Books.BOOK_TITLE,
                            Contract.Books.BOOK_IMAGE_FILE,
                            Contract.Books.BOOK_AUTHORS_READ},
                    mNoFilter ? NO_FILTER_SELECTION : SELECTION,
                    getSelectionArguments(),
                    null);
        } else {
            return null;
        }
    }

    /**
     * @return selection arguments
     */
    private String[] getSelectionArguments() {
        return mNoFilter ? new String[]{
                "%" + searchQuery + "%",
                "%" + searchQuery + "%",
                "%" + searchQuery + "%",
                "%" + searchQuery + "%"
        } : new String[]{
                mBorrowed ? "1" : "0",
                mWishList ? "1" : "0",
                "%" + searchQuery + "%",
                "%" + searchQuery + "%",
                "%" + searchQuery + "%",
                "%" + searchQuery + "%"
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            adapter.setCursor(data);
            adapter.notifyDataSetChanged();
            list.setVisibility(View.VISIBLE);
            mEmpty.setVisibility(View.GONE);
        } else {
            list.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public boolean requestSearch(String query) {
        searchQuery = query;
        getActivity().getLoaderManager().restartLoader(mLoaderId, null, this);
        return true;
    }
}
