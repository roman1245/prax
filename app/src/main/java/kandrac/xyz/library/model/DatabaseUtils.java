package kandrac.xyz.library.model;

import android.database.sqlite.SQLiteDatabase;

/**
 * Database Utils used mostly for Database Migrations
 * <p/>
 * Created by kandrac on 30/11/15.
 */
public final class DatabaseUtils {

    private DatabaseUtils() {

    }

    public static String[] getFullColumnNames(String table, String[] columnNames) {
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = table + "." + columnNames[i];
        }
        return columnNames;
    }

    /**
     * Remove columns from given table based on list of columns that have to persist.
     *
     * @param tableName      name of table to apply changes to
     * @param persistColumns columns that will remain unchanged
     */
    public static void RemoveTableColumnsSql(SQLiteDatabase database, String tableName, String[] persistColumns, String createNewTableScript, boolean removeBackupDatabase) {

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
    public static String getRenameTableSql(String tableName, String newTableName) {
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
     * <p/>
     * Script is formed as follows: <br/>
     * {@code INSERT INTO 'tableTo' ('columnsTo') SELECT 'columnsFrom' from 'tableFrom'}<br/>
     * for example: <br/>
     * {@code INSERT INTO tableTo (ct1, ct2) SELECT cf1, cf2 FROM tableFrom}
     *
     * @param tableFrom   source table
     * @param tableTo     destination table
     * @param columnsFrom columns from source table to copy
     * @param columnsTo   columns from destination table to copy into
     * @return SQL for copying data from table to table
     */
    public static String getCopySql(String tableFrom, String tableTo, String[] columnsFrom, String[] columnsTo) {
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
    public static String getCopySql(String tableFrom, String tableTo, String[] columns) {
        return getCopySql(tableFrom, tableTo, columns, columns);
    }

}
