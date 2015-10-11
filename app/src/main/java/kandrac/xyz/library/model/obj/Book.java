package kandrac.xyz.library.model.obj;

import android.content.ContentValues;
import android.provider.BaseColumns;

/**
 * Representation of concrete book from your home library
 * Created by VizGhar on 9.8.2015.
 */
public class Book {

    public static final String TABLE_NAME = "books";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SUBTITLE = "subtitle";
    public static final String COLUMN_DESCRIPTION = "birth";
    public static final String COLUMN_ISBN = "death";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_STARS = "description";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_SUBTITLE + " TEXT," +
                    COLUMN_DESCRIPTION + " TEXT," +
                    COLUMN_ISBN + " TEXT," +
                    COLUMN_AUTHOR + " TEXT," +
                    COLUMN_STARS + " INTEGER)";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public int id;
    public String title;
    public String subtitle;
    public String description;
    public String isbn;
    public String author;
    public int stars;

    public Book() {

    }

    private Book(Builder builder) {
        id = builder.id;
        title = builder.title;
        subtitle = builder.subtitle;
        description = builder.description;
        isbn = builder.isbn;
        author = builder.author;
        stars = builder.stars;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, title);
        contentValues.put(COLUMN_SUBTITLE, subtitle);
        contentValues.put(COLUMN_DESCRIPTION, description);
        contentValues.put(COLUMN_ISBN, isbn);
        contentValues.put(COLUMN_AUTHOR, author);
        contentValues.put(COLUMN_STARS, stars);
        return contentValues;
    }

    public static class Builder {

        private int id;
        private String title;
        private String subtitle;
        private String description;
        private String isbn;
        private int stars;
        private String author;

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setIsbn(String isbn) {
            this.isbn = isbn;
            return this;
        }
        public Builder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public Builder setStars(int stars) {
            this.stars = stars;
            return this;
        }

        public Book build() {
            return new Book(this);
        }
    }
}
