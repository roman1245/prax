package kandrac.xyz.library;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kandrac.xyz.library.model.Contract;

/**
 * Created by kandrac on 19/11/15.
 */
public class SharedBookListFragment extends BookListFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        mFab.setVisibility(View.GONE);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getTitle() {
        return R.string.menu_books_borrowed;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = (CursorLoader) super.onCreateLoader(id, args);
        if (loader != null) {
            loader.setUri(Contract.BOOKS_BORROW_URI);
        }
        return loader;
    }

    @Override
    protected String getSelection() {
        String superSelection = super.getSelection();
        if (superSelection == null) {
            return Contract.BorrowInfo.BORROW_DATE_RETURNED + " = 0";
        } else {
            return "(" + superSelection + ") AND " + Contract.BorrowInfo.BORROW_DATE_RETURNED + " = 0";
        }
    }

    @Override
    protected int getLoaderId() {
        return MainActivity.BORROWED_BOOK_LIST_LOADER;
    }
}
