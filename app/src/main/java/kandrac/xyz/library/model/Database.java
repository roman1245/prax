package kandrac.xyz.library.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database representation for this application
 * Created by VizGhar on 9.8.2015.
 */
public class Database extends SQLiteOpenHelper {

    public static final int ADDED_BORROW_INFO_TABLE = 1;

    public static final String DATABASE_NAME = "library.db";
    public static final int DATABASE_VERSION = ADDED_BORROW_INFO_TABLE;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public interface Tables {
        String BOOKS = "books";
        String AUTHORS = "authors";
        String PUBLISHERS = "publishers";
        String BORROW_INFO = "borrow_info";

        // m-n connections
        String BOOKS_AUTHORS = "book_author";

        // everything from connected authors and books
        String BOOKS_JOIN_AUTHORS = "books JOIN book_author ON books._id = book_author.book_id JOIN authors ON authors._id = book_author.author_id";
        String BOOKS_JOIN_PUBLISHERS = "books JOIN publishers ON books.book_publisher_id = publishers._id";
    }

    interface References {
        String AUTHORS_ID = "REFERENCES " + Tables.AUTHORS + "(" + Contract.Authors.AUTHOR_ID + ")";
        String BOOKS_ID = "REFERENCES " + Tables.BOOKS + "(" + Contract.Books.BOOK_ID + ")";
        String PUBLISHERS_ID = "REFERENCES " + Tables.PUBLISHERS + "(" + Contract.Publishers.PUBLISHER_ID + ")";
    }

    public static final String BOOKS_CREATE_TABLE =
            "CREATE TABLE " + Tables.BOOKS + " (" +
                    Contract.Books.BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Books.BOOK_TITLE + " TEXT NOT NULL," +
                    Contract.Books.BOOK_SUBTITLE + " TEXT," +
                    Contract.Books.BOOK_DESCRIPTION + " TEXT," +
                    Contract.Books.BOOK_ISBN + " TEXT," +
                    Contract.Books.BOOK_IMAGE_FILE + " TEXT," +
                    Contract.Books.BOOK_IMAGE_URL + " TEXT," +
                    Contract.Books.BOOK_AUTHORS_READ + " TEXT," +
                    Contract.Books.BOOK_PUBLISHER_ID + " INTEGER " + References.PUBLISHERS_ID + ")";

    public static final String AUTHORS_CREATE_TABLE =
            "CREATE TABLE " + Tables.AUTHORS + " (" +
                    Contract.Authors.AUTHOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Authors.AUTHOR_NAME + " TEXT NOT NULL," +
                    "UNIQUE (" + Contract.Authors.AUTHOR_NAME + ") ON CONFLICT REPLACE)";

    public static final String BOOKS_AUTHORS_CREATE_TABLE =
            "CREATE TABLE " + Tables.BOOKS_AUTHORS + "(" +
                    Contract.BookAuthorsColumns.BOOK_ID + " INTEGER " + References.BOOKS_ID + " ON DELETE CASCADE, " +
                    Contract.BookAuthorsColumns.AUTHOR_ID + " INTEGER " + References.AUTHORS_ID + " ON DELETE CASCADE, " +
                    "PRIMARY KEY (" + Contract.BookAuthorsColumns.BOOK_ID + " , " + Contract.BookAuthorsColumns.AUTHOR_ID + "))";

    public static final String PUBLISHERS_CREATE_TABLE =
            "CREATE TABLE " + Tables.PUBLISHERS + " (" +
                    Contract.Publishers.PUBLISHER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Publishers.PUBLISHER_NAME + " TEXT NOT NULL," +
                    "UNIQUE (" + Contract.Publishers.PUBLISHER_NAME + ") ON CONFLICT REPLACE)";

    public static final String BORROW_INFO_CREATE_TABLE =
            "CREATE TABLE " + Tables.BORROW_INFO + " (" +
                    Contract.BorrowInfoColumns.BORROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.BorrowInfoColumns.BORROW_BOOK_ID + " INTEGER " + References.BOOKS_ID + " ON DELETE CASCADE, " +
                    Contract.BorrowInfoColumns.BORROW_TO + " TEXT, " +
                    Contract.BorrowInfoColumns.BORROW_DATE_BORROWED + " INTEGER, " +
                    Contract.BorrowInfoColumns.BORROW_DATE_RETURNED + " INTEGER, " +
                    Contract.BorrowInfoColumns.BORROW_MAIL + " TEXT, " +
                    Contract.BorrowInfoColumns.BORROW_NAME + " TEXT, " +
                    Contract.BorrowInfoColumns.BORROW_PHONE + " TEXT)";

    public static final String BOOKS_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.BOOKS;
    public static final String AUTHORS_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.AUTHORS;
    public static final String PUBLISHERS_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.PUBLISHERS;
    public static final String BOOKS_AUTHORS_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.BOOKS_AUTHORS;
    public static final String BORROW_INFO_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.BORROW_INFO;

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        super.onConfigure(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AUTHORS_CREATE_TABLE);
        db.execSQL(PUBLISHERS_CREATE_TABLE);
        db.execSQL(BOOKS_CREATE_TABLE);
        db.execSQL(BOOKS_AUTHORS_CREATE_TABLE);
        db.execSQL(BORROW_INFO_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(BORROW_INFO_DROP_TABLE);
        db.execSQL(BOOKS_AUTHORS_DROP_TABLE);
        db.execSQL(BOOKS_DROP_TABLE);
        db.execSQL(PUBLISHERS_DROP_TABLE);
        db.execSQL(AUTHORS_DROP_TABLE);
        onCreate(db);
    }


    /**
     * Remove columns from given table based on list of columns that have to persist.
     *
     * @param tableName     name of table to apply changes to
     * @param persistColumns columns that will remain unchanged
     */
    private static void RemoveTableColumnsSql(SQLiteDatabase database, String tableName, String[] persistColumns, String createNewTableScript, boolean removeBackupDatabase) {

        // rename table that will be removed
        database.execSQL(getRenameTableToBackupSql(tableName));

        // create new table with removed columns
        database.execSQL(createNewTableScript);

        // copy values from old table to new one
        database.execSQL(getCopySql(getBackupTableName(tableName), tableName, persistColumns, persistColumns));

        // remove backup database if needed
        if (removeBackupDatabase) {
            database.execSQL("DROP TABLE " + getBackupTableName(tableName));
        }
    }

    /**
     * Get name of database for backup purposes
     *
     * @param tableName to get backup name from
     * @return backup table name
     */
    private static String getBackupTableName(String tableName) {
        return tableName + "_backup";
    }

    /**
     * Get standard ALTER TABLE command, that will rename table to given name
     *
     * @param tableName    table to be renamed
     * @param newTableName new name of table
     * @return rename table SQL
     */
    private static String getRenameTableSql(String tableName, String newTableName) {
        return "ALTER TABLE " + tableName + " RENAME TO " + newTableName;
    }

    /**
     * Rename table to be backup table
     *
     * @param tableName to be renamed
     * @return rename backup table SQL
     */
    private static String getRenameTableToBackupSql(String tableName) {
        return getRenameTableSql(tableName, getBackupTableName(tableName));
    }

    /**
     * Script for copying data based on column names from source to destination table. This can be
     * used for example in case you are making database migration and you need to pass those data
     * to another table which will be copy of source table with some columns removed or if you
     * creating new table which has those columns required.
     *
     * @param tableFrom   source table
     * @param tableTo     destination table
     * @param columnsFrom columns from source table to copy
     * @param columnsTo   columns from destination table to copy into
     * @return SQL for copying data from table to table
     */
    private static String getCopySql(String tableFrom, String tableTo, String[] columnsFrom, String[] columnsTo) {
        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("INSERT INTO ")
                .append(tableTo);

        sqlBuilder.append("(");

        for (int i = 0; i < columnsTo.length; i++) {
            if (i > 0) {
                sqlBuilder.append(", ");
            }
            sqlBuilder.append(columnsTo[i]);
        }

        sqlBuilder.append(") SELECT ");

        for (int i = 0; i < columnsFrom.length; i++) {
            if (i > 0) {
                sqlBuilder.append(", ");
            }
            sqlBuilder.append(columnsFrom[i]);
        }

        sqlBuilder.append(" FROM ")
                .append(tableFrom);

        return sqlBuilder.toString();
    }

    /**
     * Script for copying data based on column names from source to destination table. It will copy
     * those values into columns with same name as in source table as specified in {@code columns}
     * parameter.
     *
     * @param tableFrom source table
     * @param tableTo   destination table
     * @param columns   columns of tables to copy
     * @return SQL for copying data from table to table
     * @see #getCopySql(String, String, String[], String[])
     */
    private static String getCopySql(String tableFrom, String tableTo, String[] columns) {
        return getCopySql(tableFrom, tableTo, columns, columns);
    }
}
