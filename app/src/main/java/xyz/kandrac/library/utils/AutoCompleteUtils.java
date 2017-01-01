package xyz.kandrac.library.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;

/**
 * Utilities class for {@link AutoCompleteTextView} class
 * <p>
 * Created by jan on 1.1.2017.
 */
public class AutoCompleteUtils {

    /**
     * Sets adapter for given autocomplete text view
     *
     * @param context       linked
     * @param uri           to get data from
     * @param displayColumn identifier
     * @param target        AutoCompleteTextView
     */
    public static void setAdapter(final Context context, final Uri uri, final String displayColumn, final AutoCompleteTextView target) {

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                context,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{displayColumn},
                new int[]{android.R.id.text1},
                0);

        target.setAdapter(adapter);

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                String select = displayColumn + " LIKE ? ";
                String[] selectArgs = {"%" + str + "%"};
                String[] projection = new String[]{BaseColumns._ID, displayColumn};
                return context.getContentResolver().query(uri, projection, select, selectArgs, null);
            }
        });

        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(displayColumn);
                return cur.getString(index);
            }
        });
    }
}
