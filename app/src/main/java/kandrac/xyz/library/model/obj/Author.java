package kandrac.xyz.library.model.obj;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Representation of author
 * Created by VizGhar on 9.8.2015.
 */
public class Author {

    public static final String TABLE_NAME = "author";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_IMAGE_FILE = "file";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT NOT NULL," +
                    COLUMN_DESCRIPTION + " TEXT," +
                    COLUMN_IMAGE_FILE + " TEXT," +
                    "UNIQUE (" + COLUMN_NAME + ") ON CONFLICT REPLACE)";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public final String name;
    public final String description;
    public final String imageFilePath;

    public Author(Cursor cursor) {
        if (cursor.getPosition() < 0) {
            cursor.moveToFirst();
        }
        name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
        description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
        imageFilePath = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_FILE));
    }

    private Author(Builder builder) {
        name = builder.name;
        description = builder.description;
        imageFilePath = builder.imageFilePath;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_DESCRIPTION, description);
        contentValues.put(COLUMN_IMAGE_FILE, imageFilePath);
        return contentValues;
    }

    public static class Builder {

        private long id;
        private String name;
        private String description;
        private String imageFilePath;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setImageFilePath(String filePath) {
            this.imageFilePath = filePath;
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
                ", description='" + description + '\'' +
                ", imageFilePath='" + imageFilePath + '\'' +
                '}';
    }
}
