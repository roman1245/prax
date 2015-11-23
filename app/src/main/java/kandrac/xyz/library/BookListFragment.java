package kandrac.xyz.library;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kandrac.xyz.library.model.Contract;
import kandrac.xyz.library.utils.BookCursorAdapter;

/**
 * Created by kandrac on 20/10/15.
 */
public class BookListFragment extends SubtitledFragment implements LoaderManager.LoaderCallbacks<Cursor>, Searchable {

    @Bind(R.id.list)
    RecyclerView list;

    @Bind(R.id.fab)
    FloatingActionButton mFab;

    @OnClick(R.id.fab)
    public void addItem(View view) {
        startActivity(new Intent(getActivity(), EditBookActivity.class));
    }

    BookCursorAdapter adapter;
    String searchQuery;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.book_list_fragment, container, false);
        ButterKnife.bind(this, result);

        adapter = new BookCursorAdapter(getContext());
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(adapter);

        // Init database loading
        getActivity().getSupportLoaderManager().initLoader(getLoaderId(), null, this);

        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == getLoaderId()) {

            String selection = getSelection();
            String[] selectionArgs = getSelectionArguments();

            return new CursorLoader(
                    getActivity(),
                    Contract.Books.CONTENT_URI,
                    new String[]{Contract.Books.BOOK_ID, Contract.Books.BOOK_TITLE, Contract.Books.BOOK_IMAGE_FILE, Contract.Books.BOOK_AUTHORS_READ},
                    selection,
                    selectionArgs,
                    null);
        } else {
            return null;
        }
    }

    protected int getLoaderId() {
        return MainActivity.BOOK_LIST_LOADER;
    }

    protected String getSelection() {
        if (searchQuery != null && searchQuery.length() > 1) {
            return Contract.Books.BOOK_TITLE + " LIKE ?" +
                    " OR " + Contract.Books.BOOK_AUTHORS_READ + " LIKE ?" +
                    " OR " + Contract.Books.BOOK_DESCRIPTION + " LIKE ? " +
                    " OR " + Contract.Books.BOOK_ISBN + " LIKE ? ";
        } else {
            return null;
        }
    }

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
        adapter.setCursor(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public boolean requestSearch(String query) {
        searchQuery = query;
        getActivity().getSupportLoaderManager().restartLoader(getLoaderId(), null, this);
        return true;
    }

    @Override
    public int getTitle() {
        return R.string.menu_books_mine;
    }

}
