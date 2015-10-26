package kandrac.xyz.library.model.obj;

import android.content.ContentValues;
import android.database.Cursor;

import kandrac.xyz.library.model.Contract;

/**
 * Representation of concrete book from your home library
 * Created by VizGhar on 9.8.2015.
 */
public class Book {

    public long id;
    public final String title;
    public final String description;
    public final String isbn;
    public final Author author;
    public final String imageFilePath;

    public Book(Cursor cursor) {
        if (cursor.getPosition() < 0) {
            cursor.moveToFirst();
        }
        id = getInt(cursor, Contract.Books.BOOK_ID);
        title = getString(cursor, Contract.Books.BOOK_TITLE);
        description = getString(cursor, Contract.Books.BOOK_DESCRIPTION);
        isbn = getString(cursor, Contract.Books.BOOK_ISBN);
        author = getAuthor(cursor, Contract.Authors.AUTHOR_NAME);
        imageFilePath = cursor.getString(cursor.getColumnIndex(Contract.Books.BOOK_IMAGE_FILE));
    }

    private static int getInt(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index == -1 ? 0 : cursor.getInt(index);
    }

    private static String getString(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index == -1 ? null : cursor.getString(index);
    }

    private static Author getAuthor(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index == -1 ? null : new Author(cursor);
    }

    private Book(Builder builder) {
        id = builder.id;
        title = builder.title;
        description = builder.description;
        isbn = builder.isbn;
        author = builder.author;
        imageFilePath = builder.imageFilePath;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Books.BOOK_TITLE, title);
        contentValues.put(Contract.Books.BOOK_DESCRIPTION, description);
        contentValues.put(Contract.Books.BOOK_ISBN, isbn);
        contentValues.put(Contract.Books.BOOK_AUTHOR_ID, author.id);
        contentValues.put(Contract.Books.BOOK_IMAGE_FILE, imageFilePath);
        return contentValues;
    }

    public static class Builder {

        private long id;
        private String title;
        private String description;
        private String isbn;
        private Author author;
        private String imageFilePath;

        public Builder setId(long id) {
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

        public Builder setAuthor(Author author) {
            this.author = author;
            return this;
        }

        public Builder setImageFilePath(String filePath) {
            this.imageFilePath = filePath;
            return this;
        }

        public Book build() {
            return new Book(this);
        }
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", isbn='" + isbn + '\'' +
                ", author='" + author + '\'' +
                ", imageFilePath='" + imageFilePath + '\'' +
                '}';
    }
}
