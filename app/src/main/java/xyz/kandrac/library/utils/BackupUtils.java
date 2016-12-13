package xyz.kandrac.library.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.support.annotation.IntDef;
import android.text.TextUtils;

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
    public static int importCSV(Context context, Uri uri, CsvColumn[] csvColumns, String charset, boolean importFirst) throws IOException {

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
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
                return importCsv(context, reader, csvColumns, importFirst);
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
    public static int importCsv(Context context, Reader reader, CsvColumn[] csvColumns, boolean importFirst) throws IOException {

        int count = 0;
        // reader check
        if (reader == null) {
            throw new NullPointerException("Reader should not be null");
        }

        // create csv reader, get database and invoke reading line by line
        CSVReader csvReader = new CSVReader(reader);
        ContentResolver contentResolver = context.getContentResolver();
        String[] nextLine;

        if (!importFirst) {
            csvReader.readNext();
        }

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

            String content = TextUtils.htmlEncode(line[column.columnId]);

            if (TextUtils.isEmpty(content)) {
                continue;
            }

            switch (column.representation) {
                case CsvColumn.COLUMN_TITLE:
                    bookBuilder.setTitle(content);
                    break;
                case CsvColumn.COLUMN_SUBTITLE:
                    bookBuilder.setSubtitle(content);
                    break;
                case CsvColumn.COLUMN_DESCRIPTION:
                    bookBuilder.setDescription(content);
                    break;
                case CsvColumn.COLUMN_AUTHOR:
                    String[] authorNames = content.split(",");
                    Author[] authors = new Author[authorNames.length];
                    for (int i = 0; i < authorNames.length; i++) {
                        authors[i] = new Author.Builder().setName(authorNames[i].trim()).build();
                    }
                    bookBuilder.setAuthors(authors);
                    break;
                case CsvColumn.COLUMN_PUBLISHER:
                    bookBuilder.setPublisher(new Publisher.Builder().setName(content).build());
                    break;
                case CsvColumn.COLUMN_ISBN:
                    bookBuilder.setIsbn(content);
                    break;
                case CsvColumn.COLUMN_BORROWED_TO:
                    bookBuilder.setBorrowed(true);
                    bookBuilder.setBorrowedToWhen(0);
                    bookBuilder.setBorrowedToNotify(0);
                    bookBuilder.setBorrowedTo(content);
                    break;
                case CsvColumn.COLUMN_BORROWED_FROM:
                    bookBuilder.setBorrowedToMe(true);
                    bookBuilder.setBorrowedToMeWhen(0);
                    bookBuilder.setBorrowedToMeName(content);
                    break;
                case CsvColumn.COLUMN_WISH:
                    bookBuilder.setWish(true);
                    break;
                case CsvColumn.COLUMN_PUBLISHED:
                    bookBuilder.setPublished(content);
                    break;
            }
        }

        bookBuilder.setUpdatedAt(System.currentTimeMillis());

        return (DatabaseStoreUtils.saveBook(contentResolver, bookBuilder.build()) > 0);
    }

    public static class CsvColumn implements Parcelable {

        public static final int COLUMN_TITLE = 0;
        public static final int COLUMN_SUBTITLE = 1;
        public static final int COLUMN_DESCRIPTION = 2;
        public static final int COLUMN_AUTHOR = 3;
        public static final int COLUMN_PUBLISHER = 4;
        public static final int COLUMN_ISBN = 5;
        public static final int COLUMN_BORROWED_TO = 6;
        public static final int COLUMN_BORROWED_FROM = 7;
        public static final int COLUMN_WISH = 8;
        public static final int COLUMN_PUBLISHED = 9;

        protected CsvColumn(Parcel in) {
            representation = in.readInt();
            columnId = in.readInt();
        }

        public static final Creator<CsvColumn> CREATOR = new Creator<CsvColumn>() {
            @Override
            public CsvColumn createFromParcel(Parcel in) {
                return new CsvColumn(in);
            }

            @Override
            public CsvColumn[] newArray(int size) {
                return new CsvColumn[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(representation);
            dest.writeInt(columnId);
        }

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

    public static String[] getSampleRow(Context context, Uri uri, String charset) {
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
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
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
