package kandrac.xyz.library;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import kandrac.xyz.library.databinding.AuthorListItemBinding;
import kandrac.xyz.library.model.Contract;
import kandrac.xyz.library.model.obj.Author;

/**
 * Created by kandrac on 22/10/15.
 */
public class AuthorListFragment extends SubtitledFragment implements LoaderManager.LoaderCallbacks<Cursor>, Searchable {

    AuthorCursorAdapter adapter;

    String searchQuery;

    @Bind(R.id.list)
    RecyclerView list;

    @Bind(R.id.fab)
    FloatingActionButton mFab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.book_list_fragment, container, false);
        ButterKnife.bind(this, result);

        mFab.setVisibility(View.GONE);

        adapter = new AuthorCursorAdapter();
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(adapter);

        // Init database loading
        getActivity().getSupportLoaderManager().initLoader(MainActivity.AUTHOR_LIST_LOADER, null, this);

        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == MainActivity.AUTHOR_LIST_LOADER) {

            String selection = null;
            String[] selectionArgs = null;

            if (searchQuery != null && searchQuery.length() > 1) {
                selection = Contract.Authors.AUTHOR_NAME + " LIKE ?";
                selectionArgs = new String[]{
                        "%" + searchQuery + "%"
                };
            }

            return new CursorLoader(
                    getActivity(),
                    Contract.Authors.CONTENT_URI,
                    new String[]{Contract.Authors.AUTHOR_ID, Contract.Authors.AUTHOR_NAME},
                    selection,
                    selectionArgs,
                    null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.setCursor(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public boolean requestSearch(String query) {
        searchQuery = query;
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.AUTHOR_LIST_LOADER, null, this);
        return true;
    }

    @Override
    public int getTitle() {
        return R.string.menu_authors;
    }

    private class AuthorCursorAdapter extends RecyclerView.Adapter<AuthorCursorAdapter.BindingHolder> {

        public class BindingHolder extends RecyclerView.ViewHolder {
            public BindingHolder(View rowView) {
                super(rowView);
            }
        }

        Cursor mCursor;

        public void setCursor(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return new BindingHolder(AuthorListItemBinding.inflate(inflater, parent, false).getRoot());
        }

        @Override
        public void onBindViewHolder(BindingHolder holder, int position) {
            mCursor.moveToPosition(position);
            final Author author = new Author(mCursor);

            AuthorListItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
            binding.setAuthor(author);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), AuthorDetailActivity.class);
                    intent.putExtra(AuthorDetailActivity.EXTRA_AUTHOR_ID, author.id);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (mCursor != null) {
                return mCursor.getCount();
            } else return 0;
        }
    }
}
