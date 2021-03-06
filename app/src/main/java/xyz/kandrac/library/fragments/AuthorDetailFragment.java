package xyz.kandrac.library.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import xyz.kandrac.library.AuthorDetailActivity;
import xyz.kandrac.library.R;
import xyz.kandrac.library.utils.BookCursorAdapter;

/**
 * Displays details about given author.
 * <p>
 * Created by kandrac on 24/12/15.
 */
public class AuthorDetailFragment extends Fragment {

    private static final String EXTRA_AUTHOR_ID = "author_id_extra";

    private AuthorFragmentCallbacks mAuthorDelete;
    private long mAuthorId;

    private RecyclerView recyclerView;

    public static AuthorDetailFragment getInstance(long authorId) {
        AuthorDetailFragment result = new AuthorDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(EXTRA_AUTHOR_ID, authorId);
        result.setArguments(arguments);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuthorId = getArguments().getLong(EXTRA_AUTHOR_ID);
        setHasOptionsMenu(true);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    private void onAttachToContext(Context context) {
        if (context instanceof AuthorFragmentCallbacks) {
            mAuthorDelete = (AuthorFragmentCallbacks) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement AuthorFragmentCallbacks");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_author_detail, container, false);

        recyclerView = (RecyclerView) result.findViewById(R.id.list);

        BookCursorAdapter adapter = new BookCursorAdapter.Builder().setActivity(getActivity()).setLoaderId(AuthorDetailActivity.LOADER_AUTHOR_DETAILS).setAuthor(mAuthorId).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        return result;
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
