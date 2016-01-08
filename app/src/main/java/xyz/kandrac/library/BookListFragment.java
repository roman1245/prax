package xyz.kandrac.library;

import android.app.Fragment;
import android.content.Intent;
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
import xyz.kandrac.library.utils.BookCursorAdapter;

/**
 * Fragment with list of all books without any pre scripted selection. This fragment also contains
 * {@link FloatingActionButton} for basic actions.
 * <p>
 * Created by kandrac on 20/10/15.
 */
public class BookListFragment extends Fragment implements Searchable, BookCursorAdapter.CursorSizeChangedListener {

    private static final String EXTRA_WISH_LIST = "wish_list";
    private static final String EXTRA_BORROWED = "borrowed";
    private static final String EXTRA_NO_FILTER = "no_filter";
    private static final String EXTRA_ADD_BUTTON = "button";
    private static final String EXTRA_LOADER_ID = "loader_id";

    private int mLoaderId;
    private boolean mAddButton;
    private @BookCursorAdapter.FieldState int mWishList;
    private @BookCursorAdapter.FieldState int mBorrowed;

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

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getInstance() {
        return getInstance(BookCursorAdapter.FALSE, BookCursorAdapter.FALSE, true, MainActivity.BOOK_LIST_LOADER);
    }

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getBorrowedBooksInstance() {
        return getInstance(BookCursorAdapter.ANY, BookCursorAdapter.TRUE, false, MainActivity.BORROWED_BOOK_LIST_LOADER);
    }

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getWishListBooksInstance() {
        return getInstance(BookCursorAdapter.TRUE, BookCursorAdapter.ANY, true, MainActivity.WISH_LIST_BOOK_LIST_LOADER);
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
     * @param wishList         if items from wish-list should be displayed
     * @param borrowed         if borrowed items should be displayed
     * @param addButtonVisible add button visibility
     * @return instance
     */
    private static BookListFragment getInstance(@BookCursorAdapter.FieldState int wishList, @BookCursorAdapter.FieldState int borrowed, boolean addButtonVisible, int loaderId) {
        BookListFragment result = new BookListFragment();
        Bundle arguments = new Bundle();

        arguments.putInt(EXTRA_WISH_LIST, wishList);
        arguments.putInt(EXTRA_BORROWED, borrowed);
        arguments.putBoolean(EXTRA_ADD_BUTTON, addButtonVisible);
        arguments.putBoolean(EXTRA_NO_FILTER, false);
        arguments.putInt(EXTRA_LOADER_ID, loaderId);

        result.setArguments(arguments);
        return result;
    }

    @Override
    @SuppressWarnings("ResourceType")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();

        mWishList = arguments.getInt(EXTRA_WISH_LIST);
        mBorrowed = arguments.getInt(EXTRA_BORROWED);
        mAddButton = arguments.getBoolean(EXTRA_ADD_BUTTON);
        mLoaderId = arguments.getInt(EXTRA_LOADER_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.book_list_fragment, container, false);
        ButterKnife.bind(this, result);

        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFab.setVisibility(mAddButton ? View.VISIBLE : View.GONE);
        mEmpty.setText(mAddButton ? R.string.book_list_empty : R.string.book_borrowed_list_empty);

        adapter = new BookCursorAdapter.Builder()
                .setLoaderId(mLoaderId)
                .setActivity(getActivity())
                .setListener(this)
                .setWishList(mWishList)
                .setBorrowed(mBorrowed)
                .build();

        list.setAdapter(adapter);

        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean requestSearch(String query) {
        adapter.setFilter(query);
        return true;
    }

    @Override
    public void onCountChanged(int newCount) {
        if (newCount == 0) {
            list.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
        } else {
            list.setVisibility(View.VISIBLE);
            mEmpty.setVisibility(View.GONE);
        }
    }
}
