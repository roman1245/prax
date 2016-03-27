package xyz.kandrac.library.model.obj;

/**
 * Created by Jan Kandrac on 25.3.2016.
 */
public class BorrowedToMe {

    public long id;
    public long bookId;
    public long dateBorrowed;
    public String name;

    private BorrowedToMe(Builder builder) {
        id = builder.id;
        bookId = builder.bookId;
        dateBorrowed = builder.dateBorrowed;
        name = builder.name;
    }

    public static class Builder {

        private long id;
        private long bookId;
        private long dateBorrowed;
        private String name;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setBookId(long bookId) {
            this.bookId = bookId;
            return this;
        }

        public Builder setDateBorrowed(long dateBorrowed) {
            this.dateBorrowed = dateBorrowed;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public BorrowedToMe build() {
            return new BorrowedToMe(this);
        }
    }
}
