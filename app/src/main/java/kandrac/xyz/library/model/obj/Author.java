package kandrac.xyz.library.model.obj;

import android.content.ContentValues;
import android.database.Cursor;

import kandrac.xyz.library.model.Contract;

/**
 * Representation of author
 * Created by VizGhar on 9.8.2015.
 */
public class Author {

    public long id;
    public final String name;

    public Author(Cursor cursor) {
        if (cursor.getPosition() < 0) {
            cursor.moveToFirst();
        }
        id = cursor.getLong(cursor.getColumnIndex(Contract.Authors.AUTHOR_ID));
        name = cursor.getString(cursor.getColumnIndex(Contract.Authors.AUTHOR_NAME));
    }

    private Author(Builder builder) {
        id = builder.id;
        name = builder.name;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Authors.AUTHOR_NAME, name);
        return contentValues;
    }

    public static class Builder {

        private long id;
        private String name;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Author build() {
            return new Author(this);
        }
    }

    @Override
    public String toString() {
        return "Author{" +
                "name='" + name + '\'' +
                '}';
    }
}
