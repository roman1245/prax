package kandrac.xyz.library.model.obj;

import android.provider.BaseColumns;

/**
 * Representation of publisher of book
 * Created by VizGhar on 9.8.2015.
 */
public class Publisher {

    public static final String TABLE_NAME = "publishers";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_NAME = "name";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME + " TEXT)";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public int id;
    public String name;

    public Publisher() {

    }

    public Publisher(Builder builder) {
        id = builder.id;
        name = builder.name;
    }

    public static class Builder {

        private int id;
        private String name;

        public Builder setId(int id) {
            this.id = id;
            return this;
        }
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Publisher build() {
            return new Publisher(this);
        }
    }
}
