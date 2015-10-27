package kandrac.xyz.library.model.obj;

import android.content.ContentValues;
import android.database.Cursor;

import kandrac.xyz.library.model.Contract;

/**
 * Representation of publisher
 * Created by VizGhar on 27.10.2015.
 */
public class Publisher {

    public long id;
    public final String name;

    public Publisher(Cursor cursor) {
        if (cursor.getPosition() < 0) {
            cursor.moveToFirst();
        }
        id = cursor.getLong(cursor.getColumnIndex(Contract.Publishers.PUBLISHER_ID));
        name = cursor.getString(cursor.getColumnIndex(Contract.Publishers.PUBLISHER_NAME));
    }

    private Publisher(Builder builder) {
        id = builder.id;
        name = builder.name;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Publishers.PUBLISHER_NAME, name);
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

        public Publisher build() {
            return new Publisher(this);
        }
    }

    @Override
    public String toString() {
        return "Publisher{" +
                "name='" + name + '\'' +
                '}';
    }
}
