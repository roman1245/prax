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
    public final String subtitle;
    public final String description;
    public final String isbn;
    public final Author author;
    public final Publisher publisher;
    public final String imageFilePath;
    public final String imageUrlPath;
    public final String borrowedTo;

    public Book(Cursor cursor) {
        if (cursor.getPosition() < 0) {
            cursor.moveToFirst();
        }
        id = getInt(cursor, Contract.Books.BOOK_ID);
        title = getString(cursor, Contract.Books.BOOK_TITLE);
        subtitle = getString(cursor, Contract.Books.BOOK_SUBTITLE);
        description = getString(cursor, Contract.Books.BOOK_DESCRIPTION);
        isbn = getString(cursor, Contract.Books.BOOK_ISBN);
        author = getAuthor(cursor, Contract.Authors.AUTHOR_NAME);
        publisher = getPublisher(cursor, Contract.Publishers.PUBLISHER_NAME);
        imageFilePath = getString(cursor, Contract.Books.BOOK_IMAGE_FILE);
        imageUrlPath = getString(cursor, Contract.Books.BOOK_IMAGE_URL);
        borrowedTo = getString(cursor, Contract.Books.BOOK_BORROWED_TO);
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

    private static Publisher getPublisher(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index == -1 ? null : new Publisher(cursor);
    }

    private Book(Builder builder) {
        id = builder.id;
        title = builder.title;
        subtitle = builder.subtitle;
        description = builder.description;
        isbn = builder.isbn;
        author = builder.author;
        publisher = builder.publisher;
        imageFilePath = builder.imageFilePath;
        imageUrlPath = builder.imageUrlPath;
        borrowedTo = builder.borrowedTo;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Books.BOOK_TITLE, title);
        contentValues.put(Contract.Books.BOOK_SUBTITLE, subtitle);
        contentValues.put(Contract.Books.BOOK_DESCRIPTION, description);
        contentValues.put(Contract.Books.BOOK_ISBN, isbn);
//        contentValues.put(Contract.Books.BOOK_AUTHOR_ID, author.id);
        contentValues.put(Contract.Books.BOOK_IMAGE_FILE, imageFilePath);
        contentValues.put(Contract.Books.BOOK_IMAGE_URL, imageUrlPath);
        contentValues.put(Contract.Books.BOOK_BORROWED_TO, borrowedTo);
        return contentValues;
    }

    public static class Builder {

        private long id;
        private String title;
        private String description;
        private String isbn;
        private Author author;
        private Publisher publisher;
        private String imageFilePath;
        private String imageUrlPath;
        private String borrowedTo;
        private String subtitle;

        public Builder setId(long id) {
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

        public Builder setAuthor(Author author) {
            this.author = author;
            return this;
        }

        public Builder setPublisher(Publisher publisher) {
            this.publisher = publisher;
            return this;
        }

        public Builder setPublisher(long publisherId) {
            if (publisher != null) {
                publisher.id = publisherId;
            } else {
                publisher = new Publisher.Builder().setId(publisherId).build();
            }
            return this;
        }

        public Builder setImageFilePath(String filePath) {
            this.imageFilePath = filePath;
            return this;
        }

        public Builder setImageUrlPath(String url) {
            this.imageUrlPath = url;
            return this;
        }

        public Builder setBorrowedTo(String borrowedTo) {
            this.borrowedTo = borrowedTo;
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
                ", subtitle='" + subtitle + '\'' +
                ", description='" + description + '\'' +
                ", isbn='" + isbn + '\'' +
                ", author='" + author + '\'' +
                ", imageFilePath='" + imageFilePath + '\'' +
                ", borrowedTo='" + borrowedTo + '\'' +
                '}';
    }
}
