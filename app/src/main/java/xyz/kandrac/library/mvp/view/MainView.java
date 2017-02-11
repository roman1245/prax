package xyz.kandrac.library.mvp.view;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by jan on 31.10.2016.
 */
public interface MainView {

    int ERROR_TYPE_GOOGLE_API_CONNECTION = 1;
    int ERROR_TYPE_GOOGLE_SIGNIN = 2;
    int INFO_PRESS_AGAIN_TO_LEAVE = 3;

    AppCompatActivity getActivity();

    /**
     * @param count of items in wish-list
     */
    void onWishListItemsCount(int count);

    /**
     * @param count of items in my books
     */
    void onMyBooksCount(int count);

    /**
     * @param count of items in borrowed books
     */
    void onBorrowedBooksCount(int count);

    /**
     * @param count of items in books borrowed from friends
     */
    void onBooksFromFriendsCount(int count);

    /**
     * Invoked when menu item for displaying libraries should be enabled
     */
    void setLibraryItemVisibility(boolean visible);

    void setDriveVisibility(boolean visible);

    void showUserDetail(String displayName, String email, Uri photoUrl);

    void interact(int type, String message);

    void displayNews();
}
