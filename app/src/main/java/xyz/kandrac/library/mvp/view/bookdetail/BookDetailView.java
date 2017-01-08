package xyz.kandrac.library.mvp.view.bookdetail;

import android.app.Activity;
import android.database.Cursor;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * View definition for book detail, which is supposed to just display book data and do simple
 * operations about it.
 * <p>
 * Created by jan on 26.12.2016.
 */
public interface BookDetailView {

    int LOADER_BASIC = 10;
    int LOADER_BASIC_LIBRARY = 11;
    int LOADER_BASIC_AUTHORS = 12;
    int LOADER_BASIC_PUBLISHER = 13;
    int LOADER_BASIC_BORROW = 14;
    int LOADER_BASIC_BORROW_ME = 15;
    int LOADER_OTHER = 16;

    @Retention(SOURCE)
    @IntDef({LOADER_BASIC,
            LOADER_BASIC_LIBRARY,
            LOADER_BASIC_AUTHORS,
            LOADER_BASIC_PUBLISHER,
            LOADER_BASIC_BORROW,
            LOADER_BASIC_BORROW_ME,
            LOADER_OTHER})
    @interface BookLoader {
    }

    Activity getActivity();

    void onBasicDataLoaded(Cursor cursor);

    void onPersonalDataLoaded(Cursor cursor);

    void onAuthorsDataLoaded(String[] authors);

    void onPublisherLoaded(String pubName);

    void onLibraryLoaded(String libName);

    void onBorrowDataLoaded(long borrowedId, long borrowedFrom, long borrowedTo, String borrowedName);

    void onBorrowMeDataLoaded(long borrowMeId, String borrowMeName, long borrowMeDate);

    void onDataLoadEmpty(@BookLoader int loaderId);
}
