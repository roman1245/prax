package xyz.kandrac.library;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.kandrac.library.model.obj.Author;
import xyz.kandrac.library.utils.BookCursorAdapter;

/**
 * Displays details about given author.
 * <p/>
 * Created by kandrac on 24/12/15.
 */
public class AuthorDetailFragment extends Fragment {

    private BookCursorAdapter mAuthorBooksAdapter;
    private AuthorFragmentCallbacks mAuthorDelete;

    @Bind(R.id.list)
    RecyclerView recyclerView;

    public static AuthorDetailFragment getInstance() {
        return new AuthorDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AuthorFragmentCallbacks) {
            mAuthorDelete = (AuthorFragmentCallbacks) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement AuthorFragmentCallbacks");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.author_detail, container, false);

        ButterKnife.bind(this, result);

        mAuthorBooksAdapter = new BookCursorAdapter(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAuthorBooksAdapter);

        return result;
    }

    public void processCursor(Cursor data) {
        Author author = new Author(data);
        mAuthorDelete.onChangeAuthorName(author.name);
        mAuthorBooksAdapter.setCursor(data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.author_detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_author_delete_title)
                        .setMessage(R.string.dialog_author_delete_message)
                        .setCancelable(true)
                        .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAuthorDelete.deleteAuthor();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public interface AuthorFragmentCallbacks {
        void deleteAuthor();

        void onChangeAuthorName(String name);
    }
}
