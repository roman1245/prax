package xyz.kandrac.library.model.firebase;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by jan on 23.10.2016.
 */
@IgnoreExtraProperties
public class FirebaseBook {

    public long updatedAt;
    public String title;
    public long id;
    public String isbn;
    public String description;
    public String subtitle;
    public String published;
    public String authors;
    public String publisher;

    public FirebaseBook() {

    }

    public FirebaseBook(String title, long id, String isbn, String description, String subtitle, String published, String authors, String publisher, long updatedAt) {
        this.title = title;
        this.id = id;
        this.isbn = isbn;
        this.published = published;
        this.authors = authors;
        this.description = description;
        this.subtitle = subtitle;
        this.publisher = publisher;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "FirebaseBook{" +
                "updatedAt=" + updatedAt +
                ", title='" + title + '\'' +
                ", id=" + id +
                ", isbn='" + isbn + '\'' +
                ", description='" + description + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", published='" + published + '\'' +
                ", authors='" + authors + '\'' +
                ", publisher='" + publisher + '\'' +
                '}';
    }
}
