package xyz.kandrac.library.model.firebase;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import xyz.kandrac.library.model.Contract;

/**
 * Created by jan on 23.10.2016.
 */
@IgnoreExtraProperties
public class FirebaseBook {

    public static final String KEY_UPDATED_AT = Contract.BooksColumns.BOOK_UPDATED_AT;
    public static final String KEY_TITLE = Contract.BooksColumns.BOOK_TITLE;
    public static final String KEY_ISBN = Contract.BooksColumns.BOOK_ISBN;
    public static final String KEY_DESCRIPTION = Contract.BooksColumns.BOOK_DESCRIPTION;
    public static final String KEY_SUBTITLE = Contract.BooksColumns.BOOK_SUBTITLE;
    public static final String KEY_PUBLISHED = Contract.BooksColumns.BOOK_PUBLISHED;
    public static final String KEY_AUTHORS = "authors";
    public static final String KEY_PUBLISHER = "publisher";
    public static final String KEY_LIBRARY = "library";
    public static final String KEY_WISH_LIST = Contract.BooksColumns.BOOK_WISH_LIST;
    public static final String KEY_BORROWED_TO_NAME = "borrowedToName";
    public static final String KEY_BORROWED_WHEN = "borrowedWhen";
    public static final String KEY_BORROW_NOTIFY = "borrowNotify";
    public static final String KEY_BORROW_ME_NAME = "borrowedToMeName";
    public static final String KEY_BORROW_ME_WHEN = "borrowedToMeWhen";

    @PropertyName(KEY_UPDATED_AT)
    public long updatedAt;

    @PropertyName(KEY_TITLE)
    public String title;

    @PropertyName(KEY_ISBN)
    public String isbn;

    @PropertyName(KEY_DESCRIPTION)
    public String description;

    @PropertyName(KEY_SUBTITLE)
    public String subtitle;

    @PropertyName(KEY_PUBLISHED)
    public String published;

    @PropertyName(KEY_AUTHORS)
    public String authors;

    @PropertyName(KEY_PUBLISHER)
    public String publisher;

    @PropertyName(KEY_LIBRARY)
    public String library;

    @PropertyName(KEY_WISH_LIST)
    public boolean wishlist;

    @PropertyName(KEY_BORROWED_TO_NAME)
    public String borrowedToName;

    @PropertyName(KEY_BORROWED_WHEN)
    public long borrowedWhen;

    @PropertyName(KEY_BORROW_NOTIFY)
    public long borrowNotify;

    @PropertyName(KEY_BORROW_ME_NAME)
    public String borrowedToMeName;

    @PropertyName(KEY_BORROW_ME_WHEN)
    public long borrowedToMeWhen;

    public FirebaseBook() {

    }

    public FirebaseBook(String title, String isbn, String description, String subtitle, String published, String authors, String publisher, long updatedAt, String library, boolean wishlist, String borrowedToName, long borrowedWhen, long borrowNotify, String borrowedToMeName, long borrowedToMeWhen) {
        this.title = title;
        this.isbn = isbn;
        this.published = published;
        this.authors = authors;
        this.description = description;
        this.subtitle = subtitle;
        this.publisher = publisher;
        this.updatedAt = updatedAt;
        this.library = library;
        this.wishlist = wishlist;
        this.borrowedToName = borrowedToName;
        this.borrowedWhen = borrowedWhen;
        this.borrowNotify = borrowNotify;
        this.borrowedToMeName = borrowedToMeName;
        this.borrowedToMeWhen = borrowedToMeWhen;
    }

    @Override
    public String toString() {
        return "FirebaseBook{" +
                "updatedAt=" + updatedAt +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", description='" + description + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", published='" + published + '\'' +
                ", authors='" + authors + '\'' +
                ", publisher='" + publisher + '\'' +
                ", library='" + library + '\'' +
                ", wishlist=" + wishlist +
                ", borrowedToName='" + borrowedToName + '\'' +
                ", borrowedWhen=" + borrowedWhen +
                ", borrowNotify=" + borrowNotify +
                ", borrowedToMeName='" + borrowedToMeName + '\'' +
                ", borrowedToMeWhen=" + borrowedToMeWhen +
                '}';
    }
}
