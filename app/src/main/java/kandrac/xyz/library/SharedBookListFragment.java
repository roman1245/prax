package kandrac.xyz.library;

import android.os.Bundle;
import android.view.View;

import kandrac.xyz.library.model.Contract;

/**
 * Created by kandrac on 19/11/15.
 */
public class SharedBookListFragment extends BookListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFab.setVisibility(View.GONE);
    }

    @Override
    public int getTitle() {
        return R.string.menu_books_borrowed;
    }

    @Override
    protected String getSelection() {
        String superSelection = super.getSelection();
        if (superSelection == null) {
            return Contract.Books.BOOK_BORROWED_TO + " IS NOT NULL";
        } else {
            return "(" + superSelection + ") AND " + Contract.Books.BOOK_BORROWED_TO + " IS NOT NULL";
        }
    }

    @Override
    protected int getLoaderId() {
        return MainActivity.BORROWED_BOOK_LIST_LOADER;
    }
}
