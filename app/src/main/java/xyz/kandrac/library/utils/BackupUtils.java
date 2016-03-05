package xyz.kandrac.library.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.IntDef;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import xyz.kandrac.library.model.DatabaseStoreUtils;
import xyz.kandrac.library.model.obj.Author;
import xyz.kandrac.library.model.obj.Book;
import xyz.kandrac.library.model.obj.Library;
import xyz.kandrac.library.model.obj.Publisher;

/**
 * Import and export database with this class
 * and
 * <p/>
 * Created by kandrac on 22/02/16.
 */
public final class BackupUtils {

    public static final String LOG = BackupUtils.class.getName();

    /**
     * Don't allow instantiation
     */
    private BackupUtils() {

    }

    /**
     * Try to import CSV file into database based on Uri. You can get such Uri via
     * {@link Intent#ACTION_GET_CONTENT} or {@link Intent#ACTION_OPEN_DOCUMENT}.
     *
     * @param context of database
     * @param uri     of file
     * @throws IOException
     */
    public static int importCSV(Context context, Uri uri, CsvColumn[] csvColumns) throws IOException {

        // uri check
        if (uri == null) {
            throw new NullPointerException("File Uri should not be null");
        }

        // open input stream of file based on its uri
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        try {
            if (inputStream == null) {
                return 0;
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                return importCsv(context, reader, csvColumns);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Based on parameters import CSV file into Database
     *
     * @param context to get database from
     * @param reader  to import data from
     */
    public static int importCsv(Context context, Reader reader, CsvColumn[] csvColumns) throws IOException {

        int count = 0;
        // reader check
        if (reader == null) {
            throw new NullPointerException("Reader should not be null");
        }

        // create csv reader, get database and invoke reading line by line
        CSVReader csvReader = new CSVReader(reader);
        ContentResolver contentResolver = context.getContentResolver();
        String[] nextLine;
        while ((nextLine = csvReader.readNext()) != null) {
            if (importCsvLine(contentResolver, nextLine, csvColumns)) {
                count++;
            }
        }
        return count;
    }

    private static boolean importCsvLine(ContentResolver contentResolver, String[] line, CsvColumn[] csvColumns) {

        Book.Builder bookBuilder = new Book.Builder();

        bookBuilder.setLibrary(new Library.Builder().setName("").build());
        for (CsvColumn column : csvColumns) {

            if (line.length <= column.columnId) {
                return false;
            }

            switch (column.representation) {
                case CsvColumn.COLUMN_TITLE:
                    bookBuilder.setTitle(line[column.columnId]);
                    break;
                case CsvColumn.COLUMN_AUTHOR:
                    bookBuilder.setAuthors(new Author[]{new Author.Builder().setName(line[column.columnId]).build()});
                    break;
                case CsvColumn.COLUMN_PUBLISHER:
                    bookBuilder.setPublisher(new Publisher.Builder().setName(line[column.columnId]).build());
                    break;
                case CsvColumn.COLUMN_ISBN:
                    bookBuilder.setIsbn(line[column.columnId]);
                    break;
            }
        }

        return (DatabaseStoreUtils.saveBook(contentResolver, bookBuilder.build()) > 0);
    }

    public static class CsvColumn {

        public static final int COLUMN_TITLE = 0;
        public static final int COLUMN_AUTHOR = 1;
        public static final int COLUMN_PUBLISHER = 2;
        public static final int COLUMN_ISBN = 3;

        @IntDef()
        @Retention(RetentionPolicy.SOURCE)
        @interface Column {
        }

        @Column
        int representation;
        int columnId;

        public CsvColumn(int columnId, @Column int columnName) {
            this.columnId = columnId;
            this.representation = columnName;
        }

    }

    /**
     * Get File name based on its Uri
     *
     * @param context to get ContentResolver from
     * @param uri     of file
     * @return file name
     */
    public static String getFileName(Context context, Uri uri) {
        Cursor fileData = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
        String result;
        if (fileData != null && fileData.moveToFirst()) {
            result = fileData.getString(fileData.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            fileData.close();
        } else {
            result = uri.getLastPathSegment();
        }

        return result;
    }

    public static String[] getSampleRow(Context context, Uri uri) {
        // uri check
        if (uri == null) {
            throw new NullPointerException("File Uri should not be null");
        }

        try {
            // open input stream of file based on its uri
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            try {
                if (inputStream == null) {
                    return null;
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    CSVReader csvReader = new CSVReader(reader);
                    return csvReader.readNext();
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException ex) {
            return null;
        }
    }
}
