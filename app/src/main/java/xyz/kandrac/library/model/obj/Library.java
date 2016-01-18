package xyz.kandrac.library.model.obj;

import android.content.ContentValues;
import android.database.Cursor;

import xyz.kandrac.library.model.Contract;

/**
 * Created by kandrac on 18/01/16.
 */
public class Library {

    public long id;
    public final String name;

    public Library(Cursor cursor) {
        if (cursor.getPosition() < 0) {
            cursor.moveToFirst();
        }
        id = cursor.getLong(cursor.getColumnIndex(Contract.Libraries.LIBRARY_ID));
        name = cursor.getString(cursor.getColumnIndex(Contract.Libraries.LIBRARY_NAME));
    }

    private Library(Builder builder) {
        id = builder.id;
        name = builder.name;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Libraries.LIBRARY_NAME, name);
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

        public Library build() {
            return new Library(this);
        }
    }

    @Override
    public String toString() {
        return "Library{" +
                "name='" + name + '\'' +
                '}';
    }
}
