package xyz.kandrac.library.mvp.view.bookdetail;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import xyz.kandrac.library.LibraryApplication;
import xyz.kandrac.library.NotificationReceiver;
import xyz.kandrac.library.R;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.mvp.presenter.BookBasicDetailPresenter;
import xyz.kandrac.library.utils.DateUtils;

/**
 * Basic details about book showed
 * <p>
 * Created by Jan Kandrac on 19.3.2016.
 */
public class BookDetailBasicFragment extends Fragment implements BookDetailView {

    public static final String BOOK_ID_EXTRA = "book_id_extra";

    @Inject
    BookBasicDetailPresenter presenter;

    private ImageView isbnImage;
    private ImageView descriptionImage;
    private TextView fullTitle;
    private TextView author;
    private TextView isbnText;
    private TextView isbnTitle;
    private TextView descriptionText;
    private TextView publisher;
    private TextView genreText;
    private TextView library;
    private TextView libraryHead;
    private ImageView borrowImage;
    private ImageView libraryImage;
    private Button borrowButton;
    private ImageView borrowMeImage;
    private Button borrowMeButton;
    private TextView publishedText;
    private TextView publishedTitle;

    private boolean showShare;

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
        LibraryApplication.getNetComponent(getActivity()).inject(this);

        presenter.setView(this);
        presenter.setBookId(getArguments().getLong(BOOK_ID_EXTRA));

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_book_detail_basic, container, false);
        isbnImage = (ImageView) result.findViewById(R.id.book_detail_isbn_image);
        descriptionImage = (ImageView) result.findViewById(R.id.book_detail_description_image);
        fullTitle = (TextView) result.findViewById(R.id.book_detail_full_book_name);
        genreText = (TextView) result.findViewById(R.id.book_detail_genre);
        author = (TextView) result.findViewById(R.id.book_detail_author);
        isbnText = (TextView) result.findViewById(R.id.book_detail_isbn);
        isbnTitle = (TextView) result.findViewById(R.id.book_detail_isbn_title);
        descriptionText = (TextView) result.findViewById(R.id.book_detail_description);
        publisher = (TextView) result.findViewById(R.id.book_detail_publisher);
        library = (TextView) result.findViewById(R.id.book_detail_library);
        libraryHead = (TextView) result.findViewById(R.id.book_detail_library_heading);
        borrowImage = (ImageView) result.findViewById(R.id.book_detail_borrow_image);
        libraryImage = (ImageView) result.findViewById(R.id.book_detail_library_icon);
        borrowButton = (Button) result.findViewById(R.id.book_detail_borrow);
        borrowMeImage = (ImageView) result.findViewById(R.id.book_detail_borrow_me_image);
        borrowMeButton = (Button) result.findViewById(R.id.book_detail_borrow_me);
        publishedText = (TextView) result.findViewById(R.id.book_detail_published);
        publishedTitle = (TextView) result.findViewById(R.id.book_detail_published_title);
        return result;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkLibrariesPreferences();
        presenter.loadBasicBookData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail_menu_basic, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share: {
                startActivity(Intent.createChooser(presenter.getShareIntent(), getResources().getText(R.string.book_detail_share)));
                return true;
            }
            default:
                return false;
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_share)
                .setVisible(showShare);
    }

    public void checkLibrariesPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean enabled = sharedPref.getBoolean(SettingsFragment.KEY_PREF_LIBRARY_ENABLED, true);
        if (!enabled) {
            library.setVisibility(View.GONE);
            libraryImage.setVisibility(View.GONE);
            libraryHead.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBasicDataLoaded(Cursor cursor) {
        if (!isAdded()) return;
        String subtitle = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_SUBTITLE));
        String title = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_TITLE));
        String isbn = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_ISBN));
        int published = cursor.getInt(cursor.getColumnIndex(Contract.Books.BOOK_PUBLISHED));
        String description = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_DESCRIPTION));

        if (TextUtils.isEmpty(subtitle)) {
            fullTitle.setText(title);
        } else {
            fullTitle.setText(getString(R.string.format_book_title_subtitle, title, subtitle));
        }

        if (TextUtils.isEmpty(isbn)) {
            isbnText.setVisibility(View.GONE);
            isbnTitle.setVisibility(View.GONE);
            isbnImage.setVisibility(View.GONE);
        } else {
            isbnText.setText(isbn);
            isbnText.setVisibility(View.VISIBLE);
            isbnTitle.setVisibility(View.VISIBLE);
            isbnImage.setVisibility(View.VISIBLE);
        }

        if (published <= 0) {
            publishedText.setVisibility(View.GONE);
            publishedTitle.setVisibility(View.GONE);
        } else {
            publishedText.setVisibility(View.VISIBLE);
            publishedTitle.setVisibility(View.VISIBLE);
            publishedText.setText(getString(R.string.format_book_published_year, published));
        }

        if (TextUtils.isEmpty(description)) {
            descriptionText.setVisibility(View.GONE);
            descriptionImage.setVisibility(View.GONE);
        } else {
            descriptionText.setText(description);
            descriptionText.setVisibility(View.VISIBLE);
            descriptionImage.setVisibility(View.VISIBLE);
        }

        showShare = true;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onPersonalDataLoaded(Cursor cursor) {

    }

    @Override
    public void onAuthorsDataLoaded(String[] authors) {
        if (!isAdded()) return;
        if (authors == null || authors.length == 0) {
            author.setText(getString(R.string.author_unknown));
            return;
        }

        String result = "";
        result += authors[0];

        for (int i = 1; i < authors.length; i++) {
            result += ", " + authors[i];
        }

        author.setText(TextUtils.isEmpty(result) ? getString(R.string.author_unknown) : result);
    }

    @Override
    public void onPublisherLoaded(String pubName) {
        if (!isAdded()) return;
        publisher.setText(TextUtils.isEmpty(pubName) ? getString(R.string.publisher_unknown) : pubName);
    }

    @Override
    public void onGenreLoaded(String genreName) {
        genreText.setText(TextUtils.isEmpty(genreName)? getString(R.string.genre_unknown): genreName);
    }

    @Override
    public void onLibraryLoaded(String libName) {
        if (!isAdded()) return;
        library.setText(TextUtils.isEmpty(libName) ? getString(R.string.library_unknown) : libName);
    }

    @Override
    public void onBorrowDataLoaded(final long borrowedId, final long borrowedFrom, final long borrowedTo, final String borrowedName) {
        if (!isAdded()) return;

        borrowImage.setVisibility(View.VISIBLE);
        borrowButton.setVisibility(View.VISIBLE);
        borrowButton.setText(borrowedName);
        borrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_return_book_title)
                        .setMessage(getString(R.string.dialog_return_book_message, borrowedName, DateUtils.dateFormat.format(borrowedFrom)))
                        .setPositiveButton(R.string.dialog_return_book_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().getContentResolver().delete(Contract.BorrowInfo.buildUri(borrowedId), null, null);

                                ContentValues bookContentValues = new ContentValues();
                                bookContentValues.put(Contract.Books.BOOK_BORROWED, false);
                                bookContentValues.put(Contract.Books.BOOK_UPDATED_AT, System.currentTimeMillis());
                                getActivity().getContentResolver().update(Contract.Books.buildBookUri(presenter.getBookId()), bookContentValues, null, null);

                                NotificationReceiver.cancelNotification(getActivity(), presenter.getBookId());
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
    }

    @Override
    public void onBorrowMeDataLoaded(final long borrowMeId, final String borrowMeName, final long borrowMeDate) {
        if (!isAdded()) return;

        borrowMeImage.setVisibility(View.VISIBLE);
        borrowMeButton.setVisibility(View.VISIBLE);
        borrowMeButton.setText(borrowMeName);
        borrowMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_return_book_title)
                        .setMessage(getString(R.string.dialog_return_borrowed_book_message, borrowMeName, DateUtils.dateFormat.format(borrowMeDate)))
                        .setPositiveButton(R.string.dialog_return_book_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().getContentResolver().delete(Contract.Books.buildBookUri(presenter.getBookId()), null, null);
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
    }

    @SuppressLint("SwitchIntDef")
    @Override
    public void onDataLoadEmpty(@BookLoader int loaderId) {
        switch (loaderId) {
            case LOADER_BASIC_BORROW:
                borrowImage.setVisibility(View.GONE);
                borrowButton.setVisibility(View.GONE);
                break;
            case LOADER_BASIC_BORROW_ME:
                borrowMeImage.setVisibility(View.GONE);
                borrowMeButton.setVisibility(View.GONE);
                break;
        }
    }
}
