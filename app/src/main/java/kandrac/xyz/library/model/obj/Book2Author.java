package kandrac.xyz.library.model.obj;

/**
 * Many to Many relationship for Book-Author
 * Created by VizGhar on 9.8.2015.
 */
public class Book2Author {

    public static final String TABLE_NAME = "book2author";
    public static final String COLUMN_BOOK_ID = "book";
    public static final String COLUMN_AUTHOR_ID = "author";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_BOOK_ID + " INTEGER," +
                    COLUMN_AUTHOR_ID + " INTEGER," +
                    "UNIQUE(" + COLUMN_BOOK_ID + "," + COLUMN_AUTHOR_ID +
                    "))";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public int bookId;
    public int authorId;

}
