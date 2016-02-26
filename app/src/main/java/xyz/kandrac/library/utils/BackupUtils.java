package xyz.kandrac.library.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
        Log.d("Jano", "title: " + line[0] + "; authors: " + line[1] + "; publisher: " + line[2] + "; isbn: " + line[3]);
    }
}
