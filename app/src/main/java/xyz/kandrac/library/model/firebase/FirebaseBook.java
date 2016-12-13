package xyz.kandrac.library.model.firebase;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by jan on 23.10.2016.
 */
@IgnoreExtraProperties
public class FirebaseBook {

    public long updatedAt;
    public String title;
    public String isbn;
    public String description;
    public String subtitle;
    public String published;
    public String authors;
    public String publisher;
    public String library;
    public boolean wishlist;
    public String borrowedToName;
    public long borrowedWhen;
    public long borrowNotify;
    public String borrowedToMeName;
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
