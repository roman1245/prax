package xyz.kandrac.library.model.firebase;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

/**
 * Created by jan on 23.10.2016.
 */
@IgnoreExtraProperties
public class FirebaseBook {

    @PropertyName("updatedAt")
    public long updatedAt;
    @PropertyName("title")
    public String title;
    @PropertyName("isbn")
    public String isbn;
    @PropertyName("description")
    public String description;
    @PropertyName("subtitle")
    public String subtitle;
    @PropertyName("published")
    public String published;
    @PropertyName("authors")
    public String authors;
    @PropertyName("publisher")
    public String publisher;
    @PropertyName("library")
    public String library;
    @PropertyName("wishlist")
    public boolean wishlist;
    @PropertyName("borrowedToName")
    public String borrowedToName;
    @PropertyName("borrowedWhen")
    public long borrowedWhen;
    @PropertyName("borrowNotify")
    public long borrowNotify;
    @PropertyName("borrowedToMeName")
    public String borrowedToMeName;
    @PropertyName("borrowedToMeWhen")
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
