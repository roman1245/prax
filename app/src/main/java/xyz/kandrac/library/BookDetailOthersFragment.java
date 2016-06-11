package xyz.kandrac.library;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.kandrac.library.model.obj.Author;
import xyz.kandrac.library.model.obj.Book;
import xyz.kandrac.library.model.obj.Borrowed;
import xyz.kandrac.library.model.obj.BorrowedToMe;
import xyz.kandrac.library.model.obj.Library;
import xyz.kandrac.library.model.obj.Publisher;

/**
 * Other book details, like read progress etc.
 * <p/>
 * Created by Jan Kandrac on 19.3.2016.
 */
public class BookDetailOthersFragment extends Fragment implements BookDetailActivity.BookDatabaseCallbacks {

    public static final String BOOK_ID_EXTRA = "book_id_extra";

    private long mBookId;

    public static BookDetailOthersFragment newInstance(long bookId) {
        BookDetailOthersFragment result = new BookDetailOthersFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(BOOK_ID_EXTRA, bookId);
        result.setArguments(arguments);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBookId = getArguments().getLong(BOOK_ID_EXTRA);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_book_detail, container, false);
        return result;
    }

    @Override
    public void onBookGet(Book book) {

    }

    @Override
    public void onAuthorsGet(Author[] author) {

    }

    @Override
    public void onPublisherGet(Publisher publisher) {

    }

    @Override
    public void onLibraryGet(Library library) {

    }

    @Override
    public void onBorrowedGet(Borrowed borrowed) {

    }

    @Override
    public void onBorrowedToMeGet(BorrowedToMe borrowedToMe) {

    }
}
