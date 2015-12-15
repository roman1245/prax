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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.Database;
import xyz.kandrac.library.model.DatabaseUtils;
import xyz.kandrac.library.utils.BookCursorAdapter;

/**
 * Fragment with list of all books without any pre scripted selection. This fragment also contains
 * {@link FloatingActionButton} for basic actions.
 * <p>
 * Created by kandrac on 20/10/15.
 */
public class BookListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Searchable {

    private static final String EXTRA_FILTER = "filter";
    private static final String EXTRA_ADD_BUTTON = "button";
    private static final String EXTRA_LOADER_ID = "loader_id";

    private int mLoaderId;
    private String mFilter;
    private boolean mAddButton;

    @Bind(R.id.list)
    public RecyclerView list;

    @Bind(R.id.fab)
    public FloatingActionButton mFab;

    @Bind(R.id.list_empty)
    public TextView mEmpty;

    @OnClick(R.id.fab)
    public void addItem(View view) {
        startActivity(new Intent(getActivity(), EditBookActivity.class));
    }

    private BookCursorAdapter adapter;
    private String searchQuery;

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getInstance() {
        return getInstance(null, true, MainActivity.BOOK_LIST_LOADER);
    }

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getBorrowedBooksInstance() {
        return getInstance(Contract.Books.BOOK_BORROWED + " = 1", false, MainActivity.BORROWED_BOOK_LIST_LOADER);
    }

    /**
     * Get instance of {@link BookListFragment} for setting all custom fields. {@code title} hold
     * the title representing this fragments content (for example: old books, borrowed books, all
     * books etc.)
     * <p>
     * {@code filter} should be closely related to {@code title} because it contains
     * string that will be added to search selection String and specifies which books exactly will
     * be shown. Always use column names from {@link xyz.kandrac.library.model.Contract.BorrowInfo}
     * or {@link xyz.kandrac.library.model.Contract.Books}
     * <p>
     * {@code addButtonVisible} determines whether button for adding new books is visible or not
     * <p>
     * {@code loaderId} is unique id that will be used with this instance only. It should not be
     * same for 2 or more fragments inside one activity.
     *
     * @param filter           to be added to selection
     * @param addButtonVisible add button visibility
     * @return instance
     */
    public static BookListFragment getInstance(String filter, boolean addButtonVisible, int loaderId) {
        BookListFragment result = new BookListFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(EXTRA_LOADER_ID, loaderId);
        arguments.putString(EXTRA_FILTER, filter);
        arguments.putBoolean(EXTRA_ADD_BUTTON, addButtonVisible);
        result.setArguments(arguments);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();

        mLoaderId = arguments.getInt(EXTRA_LOADER_ID);
        mFilter = arguments.getString(EXTRA_FILTER);
        mAddButton = arguments.getBoolean(EXTRA_ADD_BUTTON);
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

            String selection = getSelection();
            String[] selectionArgs = getSelectionArguments();

            return new CursorLoader(
                    getActivity(),
                    Contract.Books.CONTENT_URI,
                    DatabaseUtils.getFullColumnNames(
                            Database.Tables.BOOKS, new String[]{
                                    Contract.Books.BOOK_ID,
                                    Contract.Books.BOOK_TITLE,
                                    Contract.Books.BOOK_IMAGE_FILE,
                                    Contract.Books.BOOK_AUTHORS_READ}),
                    selection,
                    selectionArgs,
                    null);
        } else {
            return null;
        }
    }

    /**
     * @return selection String
     */
    protected String getSelection() {

        String searchSelection = null;

        if (searchQuery != null && searchQuery.length() > 1) {
            searchSelection = Contract.Books.BOOK_TITLE + " LIKE ?" +
                    " OR " + Contract.Books.BOOK_AUTHORS_READ + " LIKE ?" +
                    " OR " + Contract.Books.BOOK_DESCRIPTION + " LIKE ? " +
                    " OR " + Contract.Books.BOOK_ISBN + " LIKE ? ";
        }

        if (TextUtils.isEmpty(mFilter)) {
            // search selection or null (everything)
            return searchSelection;
        } else if (TextUtils.isEmpty(searchSelection)) {
            // filter
            return mFilter;
        } else {
            // search selection and filter combined
            return "(" + searchSelection + ") AND " + mFilter;
        }
    }

    /**
     * @return selection arguments
     */
    protected String[] getSelectionArguments() {
        if (searchQuery != null && searchQuery.length() > 1) {
            return new String[]{
                    "%" + searchQuery + "%",
                    "%" + searchQuery + "%",
                    "%" + searchQuery + "%",
                    "%" + searchQuery + "%"
            };
        } else {
            return null;
        }
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
