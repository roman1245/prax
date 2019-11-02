package xyz.kandrac.library.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;

/**
 * Created by Jan Kandrac on 5.6.2016.
 */
public class AutoCompleteContactAdapter extends SimpleCursorAdapter {

    /**
     * If {@link Manifest.permission#READ_CONTACTS} is granted, provided {@link AutoCompleteTextView}
     * will get its new instance of {@link SimpleCursorAdapter} with simple textual lines of
     * contacts from users contact list.
     *
     * @param autoCompleteTextView to attach adapter to
     */
    public static void createAutocompleteTextViewAdapter(@NonNull AutoCompleteTextView autoCompleteTextView) {
        Context context = autoCompleteTextView.getContext();

        int readContactPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS);
        if (readContactPermission != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        AutoCompleteContactAdapter adapter = new AutoCompleteContactAdapter(context);
        autoCompleteTextView.setAdapter(adapter);
    }

    public AutoCompleteContactAdapter(final Context context) {
        super(context,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                new int[]{android.R.id.text1},
                0);

        setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getContactCursor(str, context);
            }
        });

        setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                return cur.getString(index);
            }
        });
    }

    public Cursor getContactCursor(CharSequence filter, Context context) {
        if (filter != null) {
            return context.getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI,
                    new String[]{
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts._ID,
                    },
                    ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?",
                    new String[]{"%" + filter.toString() + "%"},
                    ContactsContract.Contacts.DISPLAY_NAME);
        }
        return null;
    }
}
