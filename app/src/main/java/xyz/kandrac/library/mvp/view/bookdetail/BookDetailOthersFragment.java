package xyz.kandrac.library.mvp.view.bookdetail;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import javax.inject.Inject;

import xyz.kandrac.library.LibraryApplication;
import xyz.kandrac.library.R;
import xyz.kandrac.library.mvp.presenter.BookBasicDetailPresenter;

import static xyz.kandrac.library.model.Contract.Books.BOOK_MY_SCORE;
import static xyz.kandrac.library.model.Contract.Books.BOOK_NOTES;
import static xyz.kandrac.library.model.Contract.Books.BOOK_PROGRESS;
import static xyz.kandrac.library.model.Contract.Books.BOOK_QUOTE;

/**
 * Other book details, like read progress etc.
 * <p/>
 * Created by Jan Kandrac on 19.3.2016.
 */
public class BookDetailOthersFragment extends Fragment implements BookDetailView {

    public static final String BOOK_ID_EXTRA = "book_id_extra";

    @Inject
    BookBasicDetailPresenter presenter;

    private Spinner mBookProgress;
    private Spinner mBookRating;
    private EditText mBookQuote;
    private EditText mBookNotes;

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
        LibraryApplication.getNetComponent(getActivity()).inject(this);

        presenter.setView(this);
        presenter.setBookId(getArguments().getLong(BOOK_ID_EXTRA));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_book_detail, container, false);
        mBookProgress = (Spinner) result.findViewById(R.id.book_detail_progress);
        mBookRating = (Spinner) result.findViewById(R.id.book_detail_rating);
        mBookQuote = (EditText) result.findViewById(R.id.book_detail_quote);
        mBookNotes = (EditText) result.findViewById(R.id.book_detail_notes);
        presenter.loadPersonalBookData();
        return result;
    }

    @Override
    public void onPause() {
        super.onPause();
        ContentValues cv = new ContentValues();
        cv.put(BOOK_PROGRESS, mBookProgress.getSelectedItemPosition());
        cv.put(BOOK_MY_SCORE, mBookRating.getSelectedItemPosition());
        cv.put(BOOK_QUOTE, mBookQuote.getText().toString());
        cv.put(BOOK_NOTES, mBookNotes.getText().toString());
        presenter.save(cv);
    }
    @Override
    public void onPersonalDataLoaded(Cursor cursor) {
        mBookProgress.setSelection(cursor.getInt(cursor.getColumnIndex(BOOK_PROGRESS)));
        mBookRating.setSelection(cursor.getInt(cursor.getColumnIndex(BOOK_MY_SCORE)));
        mBookQuote.setText(cursor.getString(cursor.getColumnIndex(BOOK_QUOTE)));
        mBookNotes.setText(cursor.getString(cursor.getColumnIndex(BOOK_NOTES)));
    }

    @Override
    public void onBasicDataLoaded(Cursor cursor) {
        // no data required
    }

    @Override
    public void onAuthorsDataLoaded(String[] authors) {
        // not used
    }

    @Override
    public void onPublisherLoaded(String pubName) {
        // not used
    }

    @Override
    public void onLibraryLoaded(String libName) {
        // not used
    }

    @Override
    public void onBorrowDataLoaded(long borrowedId, long borrowedFrom, long borrowedTo, String borrowedName) {
        // not used
    }

    @Override
    public void onBorrowMeDataLoaded(long borrowMeId, String borrowMeName, long borrowMeDate) {
        // not used
    }

    @Override
    public void onDataLoadEmpty(@BookLoader int loaderId) {
        // not used
    }
}
