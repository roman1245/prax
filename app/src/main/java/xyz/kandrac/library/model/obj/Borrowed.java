package xyz.kandrac.library.model.obj;

/**
 * Created by Jan Kandrac on 25.3.2016.
 */
public class Borrowed {

    public long id;
    public long bookId;
    public String contactId;
    public long from;
    public long to;
    public long nextNotify;
    public String mail;
    public String name;
    public String phone;

    public Borrowed(Builder builder) {
        this.id = builder.id;
        this.bookId = builder.bookId;
        this.contactId = builder.contactId;
        this.from = builder.from;
        this.to = builder.to;
        this.nextNotify = builder.nextNotify;
        this.mail = builder.mail;
        this.name = builder.name;
        this.phone = builder.phone;
    }

    public static class Builder {
        private long id;
        private long bookId;
        private String contactId;
        private long from;
        private long to;
        private long nextNotify;
        private String mail;
        private String name;
        private String phone;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setBookId(long bookId) {
            this.bookId = bookId;
            return this;
        }

        public Builder setContactId(String contactId) {
            this.contactId = contactId;
            return this;
        }

        public Builder setFrom(long from) {
            this.from = from;
            return this;
        }

        public Builder setTo(long to) {
            this.to = to;
            return this;
        }

        public Builder setNextNotify(long nextNotify) {
            this.nextNotify = nextNotify;
            return this;
        }

        public Builder setMail(String mail) {
            this.mail = mail;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Borrowed build() {
            return new Borrowed(this);
        }
    }
}
