package kandrac.xyz.library.model.obj;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Representation of concrete book from your home library
 * Created by VizGhar on 9.8.2015.
 */
public class Book {

    public static final String TABLE_NAME = "books";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "birth";
    public static final String COLUMN_ISBN = "death";
    public static final String COLUMN_AUTHOR = "author";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_TITLE + " TEXT NOT NULL," +
                    COLUMN_DESCRIPTION + " TEXT," +
                    COLUMN_ISBN + " TEXT," +
                    COLUMN_AUTHOR + " TEXT)";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public final int id;
    public final String title;
    public final String description;
    public final String isbn;
    public final String author;

    public Book(Cursor cursor) {
        id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
        description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
        isbn = cursor.getString(cursor.getColumnIndex(COLUMN_ISBN));
        author = cursor.getString(cursor.getColumnIndex(COLUMN_AUTHOR));
    }

    private Book(Builder builder) {
        id = builder.id;
        title = builder.title;
        description = builder.description;
        isbn = builder.isbn;
        author = builder.author;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, title);
        contentValues.put(COLUMN_DESCRIPTION, description);
        contentValues.put(COLUMN_ISBN, isbn);
        contentValues.put(COLUMN_AUTHOR, author);
        return contentValues;
    }

    public static class Builder {

        private int id;
        private String title;
        private String description;
        private String isbn;
        private String author;

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
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

        public Book build() {
            return new Book(this);
        }
    }
}
