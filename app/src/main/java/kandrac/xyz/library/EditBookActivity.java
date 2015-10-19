package kandrac.xyz.library;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import butterknife.Bind;
import butterknife.ButterKnife;
import kandrac.xyz.library.model.DatabaseProvider;
import kandrac.xyz.library.model.obj.Book;

/**
 * Created by VizGhar on 11.10.2015.
 */
public class EditBookActivity extends AppCompatActivity {

    @Bind(R.id.book_input_author)
    EditText author;

    @Bind(R.id.book_input_title)
    EditText title;

    @Bind(R.id.book_input_isbn)
    EditText isbn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_input);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        ButterKnife.bind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                save();
                return true;
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {
        Book book = new Book.Builder()
                .setAuthor(author.getText().toString())
                .setTitle(title.getText().toString())
                .setIsbn(isbn.getText().toString()).build();
        getContentResolver().insert(
                DatabaseProvider.getUri(DatabaseProvider.BOOKS),
                book.getContentValues());
        finish();
    }

    public void click(View v) {
        startActivityForResult(new Intent(this, BarcodeActivity.class), 523);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 523) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeActivity.BARCODE_OBJECT);
                    isbn.setText(barcode.displayValue);
                } else {
                }
            } else {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
