package xyz.kandrac.library.fragments.lists;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import xyz.kandrac.library.mviewp.MainActivity;
import xyz.kandrac.library.PublisherDetailActivity;
import xyz.kandrac.library.R;
import xyz.kandrac.library.Searchable;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.obj.Publisher;

/**
 * Created by kandrac on 22/10/15.
 */
public class PublisherBooksListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Searchable {

    PublishCursorAdapter adapter;

    String searchQuery;

    private RecyclerView list;
    private FloatingActionButton mFab;
    private TextView mEmpty;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.book_list_fragment, container, false);
        list = (RecyclerView) result.findViewById(R.id.list);
        mFab = (FloatingActionButton) result.findViewById(R.id.fab);
        mEmpty = (TextView) result.findViewById(R.id.list_empty);

        mFab.setVisibility(View.GONE);
        mEmpty.setText(R.string.publisher_list_empty);

        adapter = new PublishCursorAdapter();
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(adapter);

        // Init database loading
        getActivity().getLoaderManager().initLoader(MainActivity.PUBLISHER_LIST_LOADER, null, this);

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
        getActivity().getLoaderManager().restartLoader(MainActivity.PUBLISHER_LIST_LOADER, null, this);
        return true;
    }

    private class PublishCursorAdapter extends RecyclerView.Adapter<PublishCursorAdapter.BindingHolder> {

        public class BindingHolder extends RecyclerView.ViewHolder {

            ImageView image;
            TextView text;

            public BindingHolder(View rowView) {
                super(rowView);
                image = (ImageView) rowView.findViewById(R.id.list_item_publisher_image);
                text = (TextView) rowView.findViewById(R.id.list_item_publisher_name);
            }
        }

        Cursor mCursor;

        public void setCursor(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return new BindingHolder(inflater.inflate(R.layout.list_item_publisher, parent, false));
        }

        @Override
        public void onBindViewHolder(BindingHolder holder, int position) {
            mCursor.moveToPosition(position);
            final Publisher publisher = new Publisher(mCursor);

            holder.text.setText(TextUtils.isEmpty(publisher.name) ? getString(R.string.publisher_unknown) : publisher.name);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), PublisherDetailActivity.class);
                    intent.putExtra(PublisherDetailActivity.EXTRA_PUBLISHER_ID, publisher.id);
                    startActivity(intent);
                }
            });

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean conservative = sharedPref.getBoolean(SettingsFragment.KEY_PREF_CONSERVATIVE_ENABLED, true);
            if (!conservative) {
                int[] colors = getResources().getIntArray(R.array.md_colors_300);
                holder.image.setBackgroundColor(colors[(int) (publisher.id % colors.length)]);
            }
        }

        @Override
        public int getItemCount() {
            if (mCursor != null) {
                return mCursor.getCount();
            } else return 0;
        }
    }
}
