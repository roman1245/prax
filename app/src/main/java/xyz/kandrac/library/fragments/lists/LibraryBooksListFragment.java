package xyz.kandrac.library.fragments.lists;

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
import xyz.kandrac.library.LibraryDetailActivity;
import xyz.kandrac.library.MainActivity;
import xyz.kandrac.library.R;
import xyz.kandrac.library.Searchable;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.obj.Library;

/**
 * Created by kandrac on 22/10/15.
 */
public class LibraryBooksListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Searchable {

    LibraryAdapter adapter;

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
        mEmpty.setText(R.string.library_list_empty);

        adapter = new LibraryAdapter();
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(adapter);

        // Init database loading
        getActivity().getLoaderManager().initLoader(MainActivity.LIBRARY_LIST_LOADER, null, this);

        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == MainActivity.LIBRARY_LIST_LOADER) {

            String selection = null;
            String[] selectionArgs = null;

            if (searchQuery != null && searchQuery.length() > 1) {
                selection = Contract.Libraries.LIBRARY_NAME + " LIKE ?";
                selectionArgs = new String[]{
                        "%" + searchQuery + "%"
                };
            }

            return new CursorLoader(
                    getActivity(),
                    Contract.Libraries.CONTENT_URI,
                    new String[]{Contract.Libraries.LIBRARY_ID, Contract.Libraries.LIBRARY_NAME},
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
        getActivity().getLoaderManager().restartLoader(MainActivity.LIBRARY_LIST_LOADER, null, this);
        return true;
    }

    private class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.BindingHolder> {

        public class BindingHolder extends RecyclerView.ViewHolder {

            ImageView image;
            TextView text;

            public BindingHolder(View rowView) {
                super(rowView);
                image = (ImageView) rowView.findViewById(R.id.list_item_library_image);
                text = (TextView) rowView.findViewById(R.id.list_item_library_name);
            }
        }

        Cursor mCursor;

        public void setCursor(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return new BindingHolder(inflater.inflate(R.layout.list_item_library, parent, false));
        }

        @Override
        public void onBindViewHolder(BindingHolder holder, int position) {
            mCursor.moveToPosition(position);
            final Library library = new Library(mCursor);

            holder.text.setText(TextUtils.isEmpty(library.name) ? getString(R.string.library_unknown) : library.name);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LibraryDetailActivity.class);
                    intent.putExtra(LibraryDetailActivity.EXTRA_LIBRARY_ID, library.id);
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
