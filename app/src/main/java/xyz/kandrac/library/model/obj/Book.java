package xyz.kandrac.library.model.obj;

import android.content.ContentValues;

import xyz.kandrac.library.model.Contract;


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
    public final String imageFilePath;
    public final String imageUrlPath;
    public final boolean wish;

    public Author[] authors;
    public Publisher publisher;
    public Library library;

    private Book(Builder builder) {
        id = builder.id;
        title = builder.title;
        subtitle = builder.subtitle;
        description = builder.description;
        isbn = builder.isbn;
        publisher = builder.publisher;
        library = builder.library;
        imageFilePath = builder.imageFilePath;
        imageUrlPath = builder.imageUrlPath;
        authors = builder.authors;
        wish = builder.wish;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Books.BOOK_TITLE, title);
        contentValues.put(Contract.Books.BOOK_SUBTITLE, subtitle);
        contentValues.put(Contract.Books.BOOK_DESCRIPTION, description);
        contentValues.put(Contract.Books.BOOK_ISBN, isbn);
        contentValues.put(Contract.Books.BOOK_IMAGE_FILE, imageFilePath);
        contentValues.put(Contract.Books.BOOK_IMAGE_URL, imageUrlPath);
        return contentValues;
    }

    public static class Builder {

        private long id;
        private String title;
        private String description;
        private String isbn;
        private String imageFilePath;
        private String imageUrlPath;
        private String subtitle;
        private boolean wish;

        private Publisher publisher;
        private Author[] authors;
        private Library library;

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

        public Builder setPublisher(Publisher publisher) {
            this.publisher = publisher;
            return this;
        }

        public Builder setLibrary(Library library) {
            this.library = library;
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

        public Builder setAuthors(Author[] authors) {
            this.authors = authors;
            return this;
        }

        public Builder setWish(boolean wish) {
            this.wish = wish;
            return this;
        }

        public Book build() {
            return new Book(this);
        }
    }
}
