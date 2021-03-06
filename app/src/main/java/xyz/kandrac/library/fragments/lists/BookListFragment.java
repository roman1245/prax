package xyz.kandrac.library.fragments.lists;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.NavigationMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import xyz.kandrac.library.BackPressable;
import xyz.kandrac.library.R;
import xyz.kandrac.library.Searchable;
import xyz.kandrac.library.flow.importwizard.ImportWizardActivity;
import xyz.kandrac.library.fragments.GenreSpinnerAdapter;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.mvp.view.EditBookActivity;
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
public class BookListFragment extends Fragment implements Searchable, BookCursorAdapter.AdapterChangedListener, BackPressable {

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

    private LinearLayout mContentHolder;
    private boolean searchOpened = false;
    private MenuItem mSearchItem;

    private EditText searchView;

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

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.list_fragment, container, false);

        mContentHolder = (LinearLayout) result.findViewById(R.id.content_holder);

        list = (RecyclerView) result.findViewById(R.id.list);
        mFab = (FabSpeedDial) result.findViewById(R.id.fab_speed_dial);
        mEmpty = (TextView) result.findViewById(R.id.list_empty);

        if (getActivity().getResources().getBoolean(R.bool.use_grid)) {
            list.setLayoutManager(new GridLayoutManager(getActivity(), getActivity().getResources().getInteger(R.integer.list_columns)));
        } else {
            list.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_book_list, menu);
        mSearchItem = menu.findItem(R.id.search);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                if (searchOpened) {
                    break;
                }

                item.setVisible(false);
                View searchView2 = getActivity().getLayoutInflater().inflate(R.layout.search, mContentHolder, false);
                searchView = (EditText) searchView2.findViewById(R.id.search_text);
                searchView.requestFocus();
                searchView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        requestSearch(searchView.getText().toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                final Spinner searchRating = (Spinner) searchView2.findViewById(R.id.search_rating);
                final Spinner searchReadingProgress = (Spinner) searchView2.findViewById(R.id.search_reading_progress);
                final Spinner searchGenre = (Spinner) searchView2.findViewById(R.id.search_genre);

                searchRating.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, getActivity().getResources().getStringArray(R.array.book_rating)));
                searchReadingProgress.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, getActivity().getResources().getStringArray(R.array.reading_progress)));
                searchGenre.setAdapter(new GenreSpinnerAdapter(getActivity()));

                searchView2.findViewById(R.id.search_hide).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideSearch();
                    }
                });

                searchView2.findViewById(R.id.search_restore).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchRating.setSelection(0);
                        searchReadingProgress.setSelection(0);
                        searchGenre.setSelection(0);

                        clearFilter(Contract.Books.BOOK_MY_SCORE);
                        clearFilter(Contract.Books.BOOK_PROGRESS);

                        searchView.setText("");
                        requestSearch("");
                    }
                });

                searchRating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (i == 0) {
                            clearFilter(Contract.Books.BOOK_MY_SCORE);
                        } else {
                            requestFilter(Contract.Books.BOOK_MY_SCORE, new String[]{Integer.toString(i)});
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                searchReadingProgress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (i == 0) {
                            clearFilter(Contract.Books.BOOK_PROGRESS);
                        } else {
                            requestFilter(Contract.Books.BOOK_PROGRESS, new String[]{Integer.toString(i)});
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                searchGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (i == 0) {
                            clearFilter(Contract.Books.BOOK_GENRE_ID);
                        } else {
                            requestFilter(Contract.Books.BOOK_GENRE_ID, new String[]{Long.toString(l)});
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                mContentHolder.addView(searchView2, 0);
                searchOpened = true;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void hideSearch() {
        mContentHolder.removeViewAt(0);
        searchOpened = false;
        mSearchItem.setVisible(true);
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
                            .setPositiveButton(getString(R.string.action_change), new EditTextDialog.OnPositiveActionListener() {
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

    @Override
    public boolean onBackPressed() {
        if (searchOpened) {
            hideSearch();
            return true;
        } else {
            return false;
        }
    }
}
