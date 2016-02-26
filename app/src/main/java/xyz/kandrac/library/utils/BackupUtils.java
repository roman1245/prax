package xyz.kandrac.library.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
    public static boolean importCSV(Context context, Uri uri) throws IOException {

        // uri check
        if (uri == null) {
            throw new NullPointerException("File Uri should not be null");
        }

        // open input stream of file based on its uri
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        try {
            if (inputStream == null) {
                return false;
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                return importCsv(context, reader);
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
    public static boolean importCsv(Context context, Reader reader) throws IOException {

        // reader check
        if (reader == null) {
            throw new NullPointerException("Reader should not be null");
        }

        // create csv reader, get database and invoke reading line by line
        CSVReader csvReader = new CSVReader(reader);
        ContentResolver contentResolver = context.getContentResolver();
        String[] nextLine;
        while ((nextLine = csvReader.readNext()) != null) {
            importCsvLine(contentResolver, nextLine);
        }
        return true;
    }

    private static void importCsvLine(ContentResolver contentResolver, String[] line) {

        Book book = new Book.Builder()
                .setTitle(line[0])
                .setAuthors(new Author[]{new Author.Builder().setName(line[1]).build()})
                .setPublisher(new Publisher.Builder().setName(line[2]).build())
                .setIsbn(line[3])
                .setLibrary(new Library.Builder().setName("").build())
                .build();

        DatabaseStoreUtils.saveBook(contentResolver, book);
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
}
