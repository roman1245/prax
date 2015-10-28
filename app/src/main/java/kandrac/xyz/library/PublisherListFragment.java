package kandrac.xyz.library;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import kandrac.xyz.library.databinding.PublisherListItemBinding;
import kandrac.xyz.library.model.Contract;
import kandrac.xyz.library.model.obj.Publisher;

/**
 * Created by kandrac on 22/10/15.
 */
public class PublisherListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Searchable {

    PublishCursorAdapter adapter;

    String searchQuery;

    @Bind(R.id.list)
    RecyclerView list;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.book_list_fragment, container, false);
        ButterKnife.bind(this, result);

        adapter = new PublishCursorAdapter();
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(adapter);

        // Init database loading
        getActivity().getSupportLoaderManager().initLoader(MainActivity.PUBLISHER_LIST_LOADER, null, this);

        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == MainActivity.PUBLISHER_LIST_LOADER) {

            String selection = null;
            String[] selectionArgs = null;

            if (searchQuery != null && searchQuery.length() > 1) {
                selection = Contract.Publishers.PUBLISHER_NAME + " LIKE ?";
                selectionArgs = new String[]{
                        "%" + searchQuery + "%"
                };
            }

            return new CursorLoader(
                    getActivity(),
                    Contract.Publishers.CONTENT_URI,
                    new String[]{Contract.Publishers.PUBLISHER_ID, Contract.Publishers.PUBLISHER_NAME},
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
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.PUBLISHER_LIST_LOADER, null, this);
        return true;
    }

    private class PublishCursorAdapter extends RecyclerView.Adapter<PublishCursorAdapter.BindingHolder> {

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
            return new BindingHolder(PublisherListItemBinding.inflate(inflater, parent, false).getRoot());
        }

        @Override
        public void onBindViewHolder(BindingHolder holder, int position) {
            mCursor.moveToPosition(position);
            final Publisher publisher = new Publisher(mCursor);

            PublisherListItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
            binding.setPublisher(publisher);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), PublisherDetailActivity.class);
                    intent.putExtra(PublisherDetailActivity.EXTRA_PUBLISHER_ID, publisher.id);
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
