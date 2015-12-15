package xyz.kandrac.library;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.obj.Author;

/**
 * Created by kandrac on 22/10/15.
 */
public class AuthorListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Searchable {

    AuthorCursorAdapter adapter;

    String searchQuery;

    @Bind(R.id.list)
    RecyclerView list;

    @Bind(R.id.fab)
    FloatingActionButton mFab;

    @Bind(R.id.list_empty)
    public TextView mEmpty;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.book_list_fragment, container, false);
        ButterKnife.bind(this, result);

        mFab.setVisibility(View.GONE);
        mEmpty.setText(R.string.author_list_empty);

        adapter = new AuthorCursorAdapter();
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(adapter);

        // Init database loading
        getActivity().getLoaderManager().initLoader(MainActivity.AUTHOR_LIST_LOADER, null, this);

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
        if (data.getCount() > 0) {
            adapter.setCursor(data);
            adapter.notifyDataSetChanged();
            list.setVisibility(View.VISIBLE);
            mEmpty.setVisibility(View.GONE);
        } else {
            list.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public boolean requestSearch(String query) {
        searchQuery = query;
        getActivity().getLoaderManager().restartLoader(MainActivity.AUTHOR_LIST_LOADER, null, this);
        return true;
    }

    private class AuthorCursorAdapter extends RecyclerView.Adapter<AuthorCursorAdapter.BindingHolder> {

        public class BindingHolder extends RecyclerView.ViewHolder {

            ImageView image;
            TextView text;

            public BindingHolder(View rowView) {
                super(rowView);
                image = (ImageView) rowView.findViewById(R.id.image);
                text = (TextView) rowView.findViewById(R.id.line1);
            }
        }

        Cursor mCursor;

        public void setCursor(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return new BindingHolder(inflater.inflate(R.layout.author_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(BindingHolder holder, int position) {
            mCursor.moveToPosition(position);
            final Author author = new Author(mCursor);

            holder.text.setText(TextUtils.isEmpty(author.name) ? getString(R.string.author_unknown) : author.name);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AuthorDetailActivity.class);
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
