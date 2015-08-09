package kandrac.xyz.library.model.obj;

import android.provider.BaseColumns;

import java.util.Date;

/**
 * Representation of author of books
 * Created by VizGhar on 9.8.2015.
 */
public class Author {

    public static final String TABLE_NAME = "authors";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BIRTH = "birth";
    public static final String COLUMN_DEATH = "death";
    public static final String COLUMN_DESCRIPTION = "description";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_BIRTH + " INTEGER," +
                    COLUMN_DEATH + " INTEGER," +
                    COLUMN_DESCRIPTION + " TEXT)";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public int id;
    public String name;
    public Date birth;
    public Date death;
    public String description;

    public Author() {

    }

    private Author(Builder builder) {
        id = builder.id;
        name = builder.name;
        birth = builder.birth;
        death = builder.death;
        description = builder.description;
    }

    public static class Builder {

        private int id;
        private String name;
        private Date birth;
        private Date death;
        private String description;

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setBirth(Date birth) {
            this.birth = birth;
            return this;
        }

        public Builder setDeath(Date death) {
            this.death = death;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Author build() {
            return new Author(this);
        }
    }
}
