package kandrac.xyz.library;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kandrac.xyz.library.model.DatabaseProvider;
import kandrac.xyz.library.model.obj.Book;

/**
 * Created by VizGhar on 11.10.2015.
 */
public class EditBookActivity extends AppCompatActivity {

    @Bind(R.id.book_input_author_edit)
    EditText author;

    @Bind(R.id.book_input_title_edit)
    EditText title;

    @Bind(R.id.book_input_publisher_edit)
    EditText publisher;

    @Bind(R.id.book_input_isbn_edit)
    EditText isbn;

    @Bind(R.id.book_input_image)
    ImageView image;

    @OnClick(R.id.book_input_save)
    public void save(View view) {
        Book book = new Book.Builder()
                .setAuthor(author.getText().toString())
                .setTitle(title.getText().toString())
                .setIsbn(isbn.getText().toString()).build();
        getContentResolver().insert(
                DatabaseProvider.getUri(DatabaseProvider.BOOKS),
                book.getContentValues());
        finish();
    }

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
}
