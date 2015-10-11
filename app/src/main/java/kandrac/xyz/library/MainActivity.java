package kandrac.xyz.library;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kandrac.xyz.library.model.DatabaseProvider;
import kandrac.xyz.library.model.obj.Book;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @Bind(R.id.fab)
    public FloatingActionButton mAdd;

    @Bind(R.id.list)
    ListView list;

    BookCursorAdapter adapter;

    @OnClick(R.id.fab)
    public void addItem(View view) {
        startActivity(new Intent(this, EditBookActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_list);
        ButterKnife.bind(this);
        getSupportLoaderManager().initLoader(1, null, this);
        adapter = new BookCursorAdapter(this);
        list.setAdapter(adapter);
    }


    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            Toast.makeText(this, scanResult.getContents(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DatabaseProvider.getUri(DatabaseProvider.BOOKS), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class BookCursorAdapter extends CursorAdapter {

        public BookCursorAdapter(Context context) {
            super(context, null, 0);
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView author = (TextView) view.findViewById(R.id.line2);
            TextView title = (TextView) view.findViewById(R.id.line1);
            // Extract properties from cursor
            String authorA = cursor.getString(cursor.getColumnIndexOrThrow(Book.COLUMN_AUTHOR));
            String titleA = cursor.getString(cursor.getColumnIndexOrThrow(Book.COLUMN_TITLE));
            // Populate fields with extracted properties
            author.setText(authorA);
            title.setText(String.valueOf(titleA));
        }

    }
}