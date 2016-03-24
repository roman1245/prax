package xyz.kandrac.library;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.utils.DateUtils;

/**
 * Created by Jan Kandrac on 19.3.2016.
 */
public class BookDetailBasicFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String BOOK_ID_EXTRA = "book_id_extra";

    private long mBookId;

    private boolean mInWishList;
    private boolean mBorrowed;
    private boolean mBorrowedToMe;
    private Uri contactUri;

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
        getActivity().getSupportLoaderManager().initLoader(BookDetailActivity.LOADER_BOOK, null, this);
        getActivity().getSupportLoaderManager().initLoader(BookDetailActivity.LOADER_AUTHOR, null, this);
        getActivity().getSupportLoaderManager().initLoader(BookDetailActivity.LOADER_PUBLISHER, null, this);
        getActivity().getSupportLoaderManager().initLoader(BookDetailActivity.LOADER_BORROW_DETAIL, null, this);
        getActivity().getSupportLoaderManager().initLoader(BookDetailActivity.LOADER_BORROW_ME_DETAIL, null, this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem borrowItem = menu.findItem(R.id.action_borow);
        MenuItem moveItem = menu.findItem(R.id.action_move);

        borrowItem.setVisible(!mInWishList && !mBorrowed && !mBorrowedToMe);
        moveItem.setVisible(mInWishList);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        switch (reqCode) {
            case (BookDetailActivity.PICK_CONTACT_ACTION): {
                if (resultCode == Activity.RESULT_OK) {
                    contactUri = data.getData();
                    getActivity().getSupportLoaderManager().restartLoader(BookDetailActivity.LOADER_CONTACT, null, this);
                }
                break;
            }
            default:
                super.onActivityResult(reqCode, resultCode, data);
        }
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

    /**
     * Binds book to content
     */
    private void bindBook(Cursor bookCursor) {

        if (!bookCursor.moveToFirst()) {
            return;
        }

        String title = bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_TITLE));
        String subtitle = bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_SUBTITLE));
        String isbn = bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_ISBN));
        String description = bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_DESCRIPTION));
        mInWishList = bookCursor.getInt(bookCursor.getColumnIndex(Contract.Books.BOOK_WISH_LIST)) == 1;
        mBorrowedToMe = bookCursor.getInt(bookCursor.getColumnIndex(Contract.Books.BOOK_BORROWED_TO_ME)) == 1;
        String filePath = bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_IMAGE_FILE));

//  TODO: move to activity
//        collapsingToolbarLayout.setTitle(title);

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

        if (TextUtils.isEmpty(description)) {
            descriptionText.setVisibility(View.GONE);
            descriptionImage.setVisibility(View.GONE);
        } else {
            descriptionText.setText(description);
            descriptionText.setVisibility(View.VISIBLE);
            descriptionImage.setVisibility(View.VISIBLE);
        }

        if (mInWishList) {
            getActivity().invalidateOptionsMenu();
        }

        // TODO: move to Activity
//        File imageFile = filePath == null ? null : new File(filePath);
//
//        // TODO: do not use getMeasuredXXX
//        int width = cover.getMeasuredWidth();
//        int height = cover.getMeasuredHeight();
//        if (width == 0 && height == 0) {
//            Picasso.with(this)
//                    .load(R.drawable.navigation_back)
//                    .into(cover);
//        } else {
//            Picasso.with(this)
//                    .load(imageFile != null && imageFile.exists() ? imageFile : null)
//                    .placeholder(R.drawable.navigation_back)
//                    .resize(cover.getMeasuredWidth(), cover.getMeasuredHeight())
//                    .centerInside()
//                    .into(cover);
//        }
    }

    private void bindAuthors(Cursor authorsCursor) {
        if (authorsCursor == null || authorsCursor.getCount() == 0 || !authorsCursor.moveToFirst()) {
            author.setText(getString(R.string.author_unknown));
            return;
        }

        String result = "";
        result += authorsCursor.getString(authorsCursor.getColumnIndex(Contract.Authors.AUTHOR_NAME));

        while (authorsCursor.moveToNext()) {
            result += ", " + authorsCursor.getString(authorsCursor.getColumnIndex(Contract.Authors.AUTHOR_NAME));
        }

        author.setText(TextUtils.isEmpty(result) ? getString(R.string.author_unknown) : result);
    }


    private void bindPublisher(Cursor publisherCursor) {
        if (publisherCursor == null || publisherCursor.getCount() == 0 || !publisherCursor.moveToFirst()) {
            publisher.setText(getString(R.string.publisher_unknown));
            return;
        }

        String result = publisherCursor.getString(publisherCursor.getColumnIndex(Contract.Publishers.PUBLISHER_NAME));

        publisher.setText(TextUtils.isEmpty(result) ? getString(R.string.publisher_unknown) : result);
    }

    private void bindLibrary(Cursor libraryCursor) {
        if (libraryCursor == null || libraryCursor.getCount() == 0 || !libraryCursor.moveToFirst()) {
            library.setText(getString(R.string.library_unknown));
            return;
        }

        String result = libraryCursor.getString(libraryCursor.getColumnIndex(Contract.Libraries.LIBRARY_NAME));

        library.setText(TextUtils.isEmpty(result) ? getString(R.string.library_unknown) : result);
    }


    /**
     * Binds borrowed info to content. If {@link BorrowDetails}
     * is null,
     *
     * @param details to bind
     */
    private void bindBorrowDetails(final BorrowDetails details) {
        if (details != null) {
            mBorrowed = true;
            borrowImage.setVisibility(View.VISIBLE);
            borrowButton.setVisibility(View.VISIBLE);
            borrowButton.setText(details.name);
            borrowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.dialog_return_book_title)
                            .setMessage(getString(R.string.dialog_return_book_message, details.name, DateUtils.dateFormat.format(details.dateFrom)))
                            .setPositiveButton(R.string.dialog_return_book_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ContentValues cv = new ContentValues();
                                    cv.put(Contract.BorrowInfo.BORROW_DATE_RETURNED, new Date(System.currentTimeMillis()).getTime());
                                    cv.put(Contract.BorrowInfo.BORROW_NEXT_NOTIFICATION, 0);
                                    getActivity().getContentResolver().update(Contract.BorrowInfo.buildUri(details.id), cv, null, null);

                                    ContentValues bookContentValues = new ContentValues();
                                    bookContentValues.put(Contract.Books.BOOK_BORROWED, false);
                                    getActivity().getContentResolver().update(Contract.Books.buildBookUri(mBookId), bookContentValues, null, null);

                                    NotificationReceiver.cancelNotification(getActivity(), mBookId);
                                    mBorrowed = false;
                                    getActivity().invalidateOptionsMenu();
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
            mBorrowed = false;
            borrowImage.setVisibility(View.GONE);
            borrowButton.setVisibility(View.GONE);
        }

        getActivity().invalidateOptionsMenu();
    }

    /**
     * Binds borrowed info to content. If {@link BorrowDetails}
     * is null,
     *
     * @param details to bind
     */
    private void bindBorrowMeDetails(final BorrowDetails details) {
        if (details != null) {
            mBorrowedToMe = true;
            borrowMeImage.setVisibility(View.VISIBLE);
            borrowMeButton.setVisibility(View.VISIBLE);
            borrowMeButton.setText(details.name);
            borrowMeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.dialog_return_book_title)
                            .setMessage(getString(R.string.dialog_return_borrowed_book_message, details.name, DateUtils.dateFormat.format(details.dateFrom)))
                            .setPositiveButton(R.string.dialog_return_book_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().getContentResolver().delete(Contract.BorrowMeInfo.buildUri(details.id), null, null);
                                    getActivity().getContentResolver().delete(Contract.Books.buildBookUri(mBookId), null, null);
                                    mBorrowedToMe = false;
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
            mBorrowedToMe = false;
            borrowMeImage.setVisibility(View.GONE);
            borrowMeButton.setVisibility(View.GONE);
        }

        getActivity().invalidateOptionsMenu();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {

            case BookDetailActivity.LOADER_AUTHOR:

                return new CursorLoader(
                        getActivity(),
                        Contract.Books.buildBookWithAuthorUri(mBookId),
                        new String[]{
                                Contract.Authors.AUTHOR_NAME
                        },
                        null,
                        null,
                        null);

            case BookDetailActivity.LOADER_PUBLISHER:

                return new CursorLoader(
                        getActivity(),
                        Contract.Books.buildBookPublisherUri(mBookId),
                        new String[]{
                                Contract.Publishers.PUBLISHER_NAME
                        },
                        null,
                        null,
                        null);

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

            case BookDetailActivity.LOADER_BOOK:

                return new CursorLoader(
                        getActivity(),
                        Contract.Books.buildBookUri(mBookId),
                        new String[]{
                                Contract.Books.BOOK_ID,
                                Contract.Books.BOOK_TITLE,
                                Contract.Books.BOOK_SUBTITLE,
                                Contract.Books.BOOK_ISBN,
                                Contract.Books.BOOK_DESCRIPTION,
                                Contract.Books.BOOK_WISH_LIST,
                                Contract.Books.BOOK_BORROWED_TO_ME,
                                Contract.Books.BOOK_IMAGE_FILE
                        },
                        null,
                        null,
                        null);

            case BookDetailActivity.LOADER_CONTACT:
                // invoked after result came from Contacts
                // TODO: check ContactsContract.CommonDataKinds.Email.CONTENT_URI to get Email or
                // developer.android.com/reference/android/provider/ContactsContract.Data.html and MIME types

                String contactId = contactUri.getLastPathSegment();
                return new CursorLoader(
                        getActivity(),
                        ContactsContract.Data.CONTENT_URI,
                        ContactRequest.GENERAL_COLUMNS,
                        ContactRequest.GENERAL_SELECTION,
                        new String[]{contactId},
                        null);

            case BookDetailActivity.LOADER_BORROW_DETAIL:
                return new CursorLoader(
                        getActivity(),
                        Contract.Books.buildBorrowInfoUri(mBookId),
                        null,
                        Contract.BorrowInfo.BORROW_DATE_RETURNED + " = 0",
                        null,
                        null);

            case BookDetailActivity.LOADER_BORROW_ME_DETAIL:
                return new CursorLoader(
                        getActivity(),
                        Contract.Books.buildBorrowedToMeInfoUri(mBookId),
                        null,
                        null,
                        null,
                        null);

        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        switch (loader.getId()) {

            case BookDetailActivity.LOADER_AUTHOR:
                bindAuthors(data);
                break;

            case BookDetailActivity.LOADER_PUBLISHER:
                bindPublisher(data);
                break;

            case BookDetailActivity.LOADER_LIBRARY:
                bindLibrary(data);
                break;


            case BookDetailActivity.LOADER_BOOK:
                bindBook(data);
                break;

            case BookDetailActivity.LOADER_CONTACT: {
                if (!data.moveToFirst()) {
                    Toast.makeText(getActivity(), R.string.unexpected_error_occurs, Toast.LENGTH_SHORT).show();
                    break;
                }

                final View content = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_borrow, null);

                new AlertDialog.Builder(getActivity())
                        .setView(content)
                        .setMessage(R.string.borrow_message)
                        .setTitle(R.string.borrow_title)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText input = (EditText) content.findViewById(R.id.borrow_input);
                                if (TextUtils.isEmpty(input.getText())) {
                                    Toast.makeText(getActivity(), R.string.borrow_wrong_input, Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                final long dateFrom = new Date(System.currentTimeMillis()).getTime();

                                final String name = data.getString(ContactRequest.NAME_COLUMN);

                                int notifyInDays = Integer.parseInt(input.getText().toString());

                                Long timeToNotify =
                                        BuildConfig.DEBUG
                                                ? System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(notifyInDays)
                                                : DateUtils.getTodayHourTime(18) + TimeUnit.DAYS.toMillis(notifyInDays);

                                ContentValues borrowContentValues = new ContentValues();
                                borrowContentValues.put(Contract.BorrowInfo.BORROW_TO, contactUri.getLastPathSegment());
                                borrowContentValues.put(Contract.BorrowInfo.BORROW_DATE_BORROWED, dateFrom);
                                borrowContentValues.put(Contract.BorrowInfo.BORROW_NAME, name);
                                borrowContentValues.put(Contract.BorrowInfo.BORROW_NEXT_NOTIFICATION, timeToNotify);

                                getActivity().getContentResolver().insert(Contract.Books.buildBorrowInfoUri(mBookId), borrowContentValues);

                                ContentValues bookContentValues = new ContentValues();
                                bookContentValues.put(Contract.Books.BOOK_BORROWED, true);
                                getActivity().getContentResolver().update(Contract.Books.buildBookUri(mBookId), bookContentValues, null, null);

                                NotificationReceiver.prepareNotification(getActivity(), timeToNotify, mBookId);

                                dialog.dismiss();

                                mBorrowed = true;
                                getActivity().invalidateOptionsMenu();
                            }
                        })
                        .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

                break;
            }
            case BookDetailActivity.LOADER_BORROW_DETAIL: {
                if (data.moveToFirst() && data.getCount() != 0) {
                    BorrowDetails borrowDetails = new BorrowDetails(
                            data.getLong(data.getColumnIndex(Contract.BorrowInfo.BORROW_ID)),
                            data.getString(data.getColumnIndex(Contract.BorrowInfo.BORROW_NAME)),
                            data.getLong(data.getColumnIndex(Contract.BorrowInfo.BORROW_DATE_BORROWED)),
                            data.getLong(data.getColumnIndex(Contract.BorrowInfo.BORROW_DATE_RETURNED))
                    );
                    bindBorrowDetails(borrowDetails);
                } else {
                    bindBorrowDetails(null);
                }
                break;
            }
            case BookDetailActivity.LOADER_BORROW_ME_DETAIL: {
                if (data.moveToFirst() && data.getCount() != 0) {
                    BorrowDetails borrowDetails = new BorrowDetails(
                            data.getLong(data.getColumnIndex(Contract.BorrowMeInfo.BORROW_ID)),
                            data.getString(data.getColumnIndex(Contract.BorrowMeInfo.BORROW_NAME)),
                            data.getLong(data.getColumnIndex(Contract.BorrowMeInfo.BORROW_DATE_BORROWED)),
                            0
                    );
                    bindBorrowMeDetails(borrowDetails);
                } else {
                    bindBorrowMeDetails(null);
                }
                break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private class BorrowDetails {
        long id;
        String name;
        long dateFrom;
        long dateTo;

        public BorrowDetails(long id, String name, long dateFrom, long dateTo) {
            this.id = id;
            this.name = name;
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
        }

        @Override
        public String toString() {
            return "BorrowDetails{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", dateFrom=" + dateFrom +
                    ", dateTo=" + dateTo +
                    '}';
        }
    }


    /**
     * Helper interface for creating Contact Requests
     */
    @SuppressWarnings("unused")
    public interface ContactRequest {

        // WHERE Statements
        String GENERAL_SELECTION = ContactsContract.Data.CONTACT_ID + " = ?";
        String PHONE_SELECTION = GENERAL_SELECTION + " AND " + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'";

        // selection columns
        String[] GENERAL_COLUMNS = new String[]{ContactsContract.Data.DISPLAY_NAME};
        String[] PHONE_COLUMNS = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};

        int NAME_COLUMN = 0;
    }
}
