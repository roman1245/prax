package xyz.kandrac.library.fragments.lists;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.mvp.view.EditBookActivity;
import xyz.kandrac.library.R;
import xyz.kandrac.library.Searchable;
import xyz.kandrac.library.flow.importwizard.ImportWizardActivity;
import xyz.kandrac.library.mvp.view.MainActivity;
import xyz.kandrac.library.utils.BookCursorAdapter;
import xyz.kandrac.library.utils.EditTextDialog;

import static xyz.kandrac.library.R.id.action_import_add;
import static xyz.kandrac.library.utils.BookCursorAdapter.TRUE;

/**
 * Fragment with list of all books without any pre scripted selection. This fragment also contains
 * {@link FloatingActionButton} for basic actions.
 * <p/>
 * Created by kandrac on 20/10/15.
 */
public class BookListFragment extends Fragment implements Searchable, BookCursorAdapter.AdapterChangedListener {

    private static final String EXTRA_WISH_LIST = "wish_list";
    private static final String EXTRA_BORROWED = "borrowed";
    private static final String EXTRA_BORROWED_TO_ME = "borrowed_to_me";
    private static final String EXTRA_NO_FILTER = "no_filter";
    private static final String EXTRA_ADD_BUTTON = "button";
    private static final String EXTRA_LOADER_ID = "loader_id";

    private int mLoaderId;
    private boolean mAddButton;

    @BookCursorAdapter.FieldState
    private int mWishList;
    @BookCursorAdapter.FieldState
    private int mBorrowed;
    @BookCursorAdapter.FieldState
    private int mBorrowedToMe;

    private RecyclerView list;
    private FabSpeedDial mFab;
    private TextView mEmpty;

    private BookCursorAdapter adapter;

    public ActionMode mActionMode;

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getInstance() {
        return getInstance(BookCursorAdapter.FALSE, BookCursorAdapter.FALSE, BookCursorAdapter.FALSE, true, MainActivity.BOOK_LIST_LOADER);
    }

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getBorrowedBooksInstance() {
        return getInstance(BookCursorAdapter.ANY, TRUE, BookCursorAdapter.FALSE, false, MainActivity.BORROWED_BOOK_LIST_LOADER);
    }

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getBorrowedToMeBooksInstance() {
        return getInstance(BookCursorAdapter.ANY, BookCursorAdapter.FALSE, TRUE, true, MainActivity.BORROWED_TO_ME_LIST_LOADER);
    }

    /**
     * Get instance of {@link BookListFragment}
     *
     * @return instance
     */
    public static BookListFragment getWishListBooksInstance() {
        return getInstance(TRUE, BookCursorAdapter.ANY, BookCursorAdapter.ANY, true, MainActivity.WISH_LIST_BOOK_LIST_LOADER);
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
     * @param borrowedToMe     if items borrowed to me should be displayed
     * @param addButtonVisible add button visibility
     * @return instance
     */
    private static BookListFragment getInstance(
            @BookCursorAdapter.FieldState int wishList,
            @BookCursorAdapter.FieldState int borrowed,
            @BookCursorAdapter.FieldState int borrowedToMe,
            boolean addButtonVisible,
            int loaderId) {
        BookListFragment result = new BookListFragment();
        Bundle arguments = new Bundle();

        arguments.putInt(EXTRA_WISH_LIST, wishList);
        arguments.putInt(EXTRA_BORROWED, borrowed);
        arguments.putInt(EXTRA_BORROWED_TO_ME, borrowedToMe);
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
        mBorrowedToMe = arguments.getInt(EXTRA_BORROWED_TO_ME);
        mAddButton = arguments.getBoolean(EXTRA_ADD_BUTTON);
        mLoaderId = arguments.getInt(EXTRA_LOADER_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.list_fragment, container, false);

        list = (RecyclerView) result.findViewById(R.id.list);
        mFab = (FabSpeedDial) result.findViewById(R.id.fab_speed_dial);
        mEmpty = (TextView) result.findViewById(R.id.list_empty);

        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFab.setVisibility(mAddButton ? View.VISIBLE : View.GONE);
        mEmpty.setText(mAddButton ? R.string.book_list_empty : R.string.book_borrowed_list_empty);

        adapter = new BookCursorAdapter.Builder()
                .setLoaderId(mLoaderId)
                .setActivity(getActivity())
                .setListener(this)
                .setWishList(mWishList)
                .setBorrowed(mBorrowed)
                .setBorrowedToMe(mBorrowedToMe)
                .build();

        list.setAdapter(adapter);

        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFab.setMenuListener(new SimpleMenuListenerAdapter() {

            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                if (mWishList == TRUE || mBorrowedToMe == TRUE) {
                    navigationMenu.findItem(R.id.action_import_add).setVisible(false);
                    navigationMenu.findItem(R.id.action_bulk_add).setVisible(false);
                }
                return super.onPrepareMenu(navigationMenu);
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case action_import_add: {
                        startActivity(new Intent(getActivity(), ImportWizardActivity.class));
                        break;
                    }
                    case R.id.action_bulk_add: {
                        Intent intent = new Intent(getActivity(), EditBookActivity.class);
                        intent.putExtra(EditBookActivity.EXTRA_BULK, true);
                        startActivity(intent);
                        break;
                    }
                    case R.id.action_scan_add: {
                        Intent intent = new Intent(getActivity(), EditBookActivity.class);
                        intent.putExtra(EditBookActivity.EXTRA_WISH_LIST, mWishList);
                        intent.putExtra(EditBookActivity.EXTRA_BORROWED_TO_ME, mBorrowedToMe);
                        intent.putExtra(EditBookActivity.EXTRA_SCAN, true);
                        startActivity(intent);
                        break;
                    }
                    case R.id.action_standard_add: {
                        Intent intent = new Intent(getActivity(), EditBookActivity.class);
                        intent.putExtra(EditBookActivity.EXTRA_WISH_LIST, mWishList);
                        intent.putExtra(EditBookActivity.EXTRA_BORROWED_TO_ME, mBorrowedToMe);
                        startActivity(intent);
                        break;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean requestSearch(String query) {
        adapter.setFilter(query);
        return true;
    }

    @Override
    public void clearFilter(String field) {
        adapter.clearFilter(field + " = ? ");
    }

    @Override
    public void requestFilter(String field, String[] arguments) {
        adapter.addFilter(field + " = ? ", arguments);
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

    @Override
    public void onMultiSelectStart() {
        getActivity().startActionMode(mActionModeCallback);
    }

    @Override
    public void onMultiSelectEnd() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    public ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            mActionMode = mode;
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.multi_select_menu, menu);

            boolean enabled = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(SettingsFragment.KEY_PREF_LIBRARY_ENABLED, true);
            menu.findItem(R.id.action_change_library).setVisible(enabled);

            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    int bookCount = adapter.getSelectedItemCount();

                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.book_list_delete_title)
                            .setMessage(getResources().getQuantityString(R.plurals.book_list_delete_message, bookCount, bookCount))
                            .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    adapter.deleteSelectedBooks(getActivity());
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.action_cancel, null)
                            .show();
                    return true;
                case R.id.action_change_author:
                    new EditTextDialog.Builder(getActivity())
                            .setAutocompleteUri(Contract.Authors.CONTENT_URI)
                            .setAutocompleteColumn(Contract.Authors.AUTHOR_NAME)
                            .setPositiveButton( getString(R.string.action_change), new EditTextDialog.OnPositiveActionListener() {
                                @Override
                                public void onPositiveAction(DialogInterface dialogInterface, String text) {
                                    adapter.changeSelectedBooksAuthor(getActivity(), text);
                                }
                            })
                            .setTitle(R.string.action_change_author)
                            .setNegativeButton(R.string.action_cancel, null)
                            .show();
                    return true;
                case R.id.action_change_library:
                    new EditTextDialog.Builder(getActivity())
                            .setAutocompleteUri(Contract.Libraries.CONTENT_URI)
                            .setAutocompleteColumn(Contract.Libraries.LIBRARY_NAME)
                            .setPositiveButton(getString(R.string.action_change), new EditTextDialog.OnPositiveActionListener() {
                                @Override
                                public void onPositiveAction(DialogInterface dialogInterface, String text) {
                                    adapter.changeSelectedBooksLibrary(getActivity(), text);
                                }
                            })
                            .setTitle(R.string.action_change_library)
                            .setNegativeButton(R.string.action_cancel, null)
                            .show();
                    return true;
                case R.id.action_change_publisher:
                    new EditTextDialog.Builder(getActivity())
                            .setAutocompleteUri(Contract.Publishers.CONTENT_URI)
                            .setAutocompleteColumn(Contract.Publishers.PUBLISHER_NAME)
                            .setPositiveButton(getString(R.string.action_change), new EditTextDialog.OnPositiveActionListener() {
                                @Override
                                public void onPositiveAction(DialogInterface dialogInterface, String text) {
                                    adapter.changeSelectedBooksPublisher(getActivity(), text);
                                }
                            })
                            .setTitle(R.string.action_change_publisher)
                            .setNegativeButton(R.string.action_cancel, null)
                            .show();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user leaves the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.closeMultiSelect();
            mActionMode = null;
        }
    };
}
