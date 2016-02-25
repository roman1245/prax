package xyz.kandrac.library.utils;

import android.content.Context;
import android.util.Log;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Import and export database with this class. See especially methods {@link #importCsv(Context, File)}
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
     * Based on parameters import CSV file into Database
     *
     * @param context to get database from
     * @param file    to import data from
     */
    public static void importCsv(Context context, File file) {
        if (file == null) {
            return;
        }
        try {

            CSVReader reader = new CSVReader(new FileReader(file));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                Log.d("Jano", "title: " + nextLine[0] + "; authors: " + nextLine[1] + "; publisher: " + nextLine[2] + "; isbn: " + nextLine[3]);
            }

        } catch (FileNotFoundException exception) {
            LogUtils.e(LOG, "Cannot find file", exception);
        } catch (IOException exception) {
            LogUtils.e(LOG, "Error reading file", exception);
        }
    }

    /**
     * Based on parameters import CSV file into Database
     *
     * @param context to get database from
     * @param file    to import data from
     */
    public static void importCsv(Context context, Reader file) {
        if (file == null) {
            return;
        }
        try {

            CSVReader reader = new CSVReader(file);
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                Log.d("Jano", "title: " + nextLine[0] + "; authors: " + nextLine[1] + "; publisher: " + nextLine[2] + "; isbn: " + nextLine[3]);
            }

        } catch (FileNotFoundException exception) {
            LogUtils.e(LOG, "Cannot find file", exception);
        } catch (IOException exception) {
            LogUtils.e(LOG, "Error reading file", exception);
        }
    }

    /**
     * Based on parameters export database to CSV file
     *
     * @param context to get database from
     * @param file    to export data to
     */
    public static void exportCsv(Context context, File file) {

    }
}
