package xyz.kandrac.library;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.obj.Author;
import xyz.kandrac.library.model.obj.Book;
import xyz.kandrac.library.model.obj.Borrowed;
import xyz.kandrac.library.model.obj.BorrowedToMe;
import xyz.kandrac.library.model.obj.Library;
import xyz.kandrac.library.model.obj.Publisher;
import xyz.kandrac.library.utils.DateUtils;

/**
 * Created by Jan Kandrac on 19.3.2016.
 */
public class BookDetailBasicFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, BookDetailActivity.BookDatabaseCallbacks {

    public static final String BOOK_ID_EXTRA = "book_id_extra";

    private long mBookId;

    @Bind(R.id.book_detail_isbn_image)
    ImageView isbnImage;

    @Bind(R.id.book_detail_description_image)
    ImageView descriptionImage;

    @Bind(R.id.book_detail_full_book_name)
    TextView fullTitle;

    @Bind(R.id.book_detail_author)
    TextView author;

    @Bind(R.id.book_detail_isbn)
    TextView isbnText;

    @Bind(R.id.book_detail_isbn_title)
    TextView isbnTitle;

    @Bind(R.id.book_detail_description)
    TextView descriptionText;

    @Bind(R.id.book_detail_publisher)
    TextView publisher;

    @Bind(R.id.book_detail_library)
    TextView library;

    @Bind(R.id.book_detail_library_heading)
    TextView libraryHead;

    @Bind(R.id.book_detail_borrow_image)
    ImageView borrowImage;

    @Bind(R.id.book_detail_library_icon)
    ImageView libraryImage;

    @Bind(R.id.book_detail_borrow)
    Button borrowButton;

    @Bind(R.id.book_detail_borrow_me_image)
    ImageView borrowMeImage;

    @Bind(R.id.book_detail_borrow_me)
    Button borrowMeButton;

    @Bind(R.id.book_detail_published)
    TextView publishedText;

    @Bind(R.id.book_detail_published_title)
    TextView publishedTitle;

    public static BookDetailBasicFragment newInstance(long bookId) {
        BookDetailBasicFragment result = new BookDetailBasicFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(BOOK_ID_EXTRA, bookId);
        result.setArguments(bundle);
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
        View result = inflater.inflate(R.layout.fragment_book_detail_basic, container, false);
        ButterKnife.bind(this, result);
        return result;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkLibrariesPreferences();
    }

    public void checkLibrariesPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean enabled = sharedPref.getBoolean(SettingsFragment.KEY_PREF_LIBRARY_ENABLED, true);
        if (!enabled) {
            library.setVisibility(View.GONE);
            libraryImage.setVisibility(View.GONE);
            libraryHead.setVisibility(View.GONE);
        } else {
            getActivity().getSupportLoaderManager().initLoader(BookDetailActivity.LOADER_LIBRARY, null, this);
        }
    }

    private void bindLibrary(Cursor libraryCursor) {
        if (libraryCursor == null || libraryCursor.getCount() == 0 || !libraryCursor.moveToFirst()) {
            library.setText(getString(R.string.library_unknown));
            return;
        }

        String result = libraryCursor.getString(libraryCursor.getColumnIndex(Contract.Libraries.LIBRARY_NAME));

        library.setText(TextUtils.isEmpty(result) ? getString(R.string.library_unknown) : result);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {

            case BookDetailActivity.LOADER_LIBRARY:

                return new CursorLoader(
                        getActivity(),
                        Contract.Books.buildBookLibraryUri(mBookId),
                        new String[]{
                                Contract.Libraries.LIBRARY_NAME
                        },
                        null,
                        null,
                        null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        switch (loader.getId()) {

            case BookDetailActivity.LOADER_LIBRARY:
                bindLibrary(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onBookGet(Book book) {

        if (TextUtils.isEmpty(book.subtitle)) {
            fullTitle.setText(book.title);
        } else {
            fullTitle.setText(getString(R.string.format_book_title_subtitle, book.title, book.subtitle));
        }

        if (TextUtils.isEmpty(book.isbn)) {
            isbnText.setVisibility(View.GONE);
            isbnTitle.setVisibility(View.GONE);
            isbnImage.setVisibility(View.GONE);
        } else {
            isbnText.setText(book.isbn);
            isbnText.setVisibility(View.VISIBLE);
            isbnTitle.setVisibility(View.VISIBLE);
            isbnImage.setVisibility(View.VISIBLE);
        }

        if (book.published <= 0) {
            publishedText.setVisibility(View.GONE);
            publishedTitle.setVisibility(View.GONE);
        } else {
            publishedText.setVisibility(View.VISIBLE);
            publishedTitle.setVisibility(View.VISIBLE);
            publishedText.setText("" + book.published);
        }

        if (TextUtils.isEmpty(book.description)) {
            descriptionText.setVisibility(View.GONE);
            descriptionImage.setVisibility(View.GONE);
        } else {
            descriptionText.setText(book.description);
            descriptionText.setVisibility(View.VISIBLE);
            descriptionImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAuthorsGet(Author[] authors) {
        if (authors == null || authors.length == 0) {
            author.setText(getString(R.string.author_unknown));
            return;
        }

        String result = "";
        result += authors[0].name;

        for (int i = 1; i < authors.length; i++) {
            result += ", " + authors[i].name;
        }

        author.setText(TextUtils.isEmpty(result) ? getString(R.string.author_unknown) : result);
    }

    @Override
    public void onPublisherGet(Publisher publisher) {
        this.publisher.setText(publisher == null ? getString(R.string.publisher_unknown) : publisher.name);
    }

    @Override
    public void onLibraryGet(Library library) {

    }


    @Override
    public void onBorrowedGet(final Borrowed borrowed) {

        if (borrowed != null) {
            borrowImage.setVisibility(View.VISIBLE);
            borrowButton.setVisibility(View.VISIBLE);
            borrowButton.setText(borrowed.name);
            borrowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.dialog_return_book_title)
                            .setMessage(getString(R.string.dialog_return_book_message, borrowed.name, DateUtils.dateFormat.format(borrowed.from)))
                            .setPositiveButton(R.string.dialog_return_book_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ContentValues cv = new ContentValues();
                                    cv.put(Contract.BorrowInfo.BORROW_DATE_RETURNED, new Date(System.currentTimeMillis()).getTime());
                                    cv.put(Contract.BorrowInfo.BORROW_NEXT_NOTIFICATION, 0);
                                    getActivity().getContentResolver().update(Contract.BorrowInfo.buildUri(borrowed.id), cv, null, null);

                                    ContentValues bookContentValues = new ContentValues();
                                    bookContentValues.put(Contract.Books.BOOK_BORROWED, false);
                                    getActivity().getContentResolver().update(Contract.Books.buildBookUri(mBookId), bookContentValues, null, null);

                                    NotificationReceiver.cancelNotification(getActivity(), mBookId);
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(true)
                            .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                }
            });
        } else {
            borrowImage.setVisibility(View.GONE);
            borrowButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBorrowedToMeGet(final BorrowedToMe borrowedToMe) {
        if (borrowedToMe != null) {
            borrowMeImage.setVisibility(View.VISIBLE);
            borrowMeButton.setVisibility(View.VISIBLE);
            borrowMeButton.setText(borrowedToMe.name);
            borrowMeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.dialog_return_book_title)
                            .setMessage(getString(R.string.dialog_return_borrowed_book_message, borrowedToMe.name, DateUtils.dateFormat.format(borrowedToMe.dateBorrowed)))
                            .setPositiveButton(R.string.dialog_return_book_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().getContentResolver().delete(Contract.BorrowMeInfo.buildUri(borrowedToMe.id), null, null);
                                    getActivity().getContentResolver().delete(Contract.Books.buildBookUri(mBookId), null, null);
                                    dialog.dismiss();
                                    getActivity().finish();
                                }
                            })
                            .setCancelable(true)
                            .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                }
            });
        } else {
            borrowMeImage.setVisibility(View.GONE);
            borrowMeButton.setVisibility(View.GONE);
        }
    }
}
