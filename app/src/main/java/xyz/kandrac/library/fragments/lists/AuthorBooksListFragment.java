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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import xyz.kandrac.library.AuthorDetailActivity;
import xyz.kandrac.library.R;
import xyz.kandrac.library.Searchable;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.obj.Author;
import xyz.kandrac.library.mvp.view.MainActivity;

/**
 * List of authors displayed in this fragment is based on data stored in
 * {@link xyz.kandrac.library.model.Contract.Authors} table. List contains all
 * non-deleted authors and on clicking author line {@link AuthorDetailActivity}
 * starts. To see details about the UI please refer to
 * {@link AuthorBooksListFragment.AuthorCursorAdapter}
 * <p/>
 * Created by kandrac on 22/10/15.
 */
public class AuthorBooksListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Searchable {

    private AuthorCursorAdapter mAdapter;

    private String mSearchQuery;

    private RecyclerView list;
    private FabSpeedDial mFab;
    private TextView mEmpty;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.list_fragment, container, false);
        list = (RecyclerView) result.findViewById(R.id.list);
        mFab = (FabSpeedDial) result.findViewById(R.id.fab_speed_dial);
        mEmpty = (TextView) result.findViewById(R.id.list_empty);

        mFab.setVisibility(View.GONE);
        mEmpty.setText(R.string.author_list_empty);

        mAdapter = new AuthorCursorAdapter();
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(mAdapter);

        getActivity().getLoaderManager().initLoader(MainActivity.AUTHOR_LIST_LOADER, null, this);

        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == MainActivity.AUTHOR_LIST_LOADER) {

            String selection = null;
            String[] selectionArgs = null;

            if (mSearchQuery != null && mSearchQuery.length() > 1) {
                selection = Contract.Authors.AUTHOR_NAME + " LIKE ?";
                selectionArgs = new String[]{
                        "%" + mSearchQuery + "%"
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
            mAdapter.setCursor(data);
            mAdapter.notifyDataSetChanged();
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
        mSearchQuery = query;
        getActivity().getLoaderManager().restartLoader(MainActivity.AUTHOR_LIST_LOADER, null, this);
        return true;
    }

    @Override
    public void clearFilter(String field) {

    }

    @Override
    public void requestFilter(String field, String[] arguments) {

    }

    /**
     * Adapter for getting views based on {@link Cursor} provided and transformed to {@link Author}
     * object. The view displayed is based on @layout/list_item_author
     */
    private class AuthorCursorAdapter extends RecyclerView.Adapter<AuthorCursorAdapter.BindingHolder> {

        /**
         * ViewHolder for {@link AuthorBooksListFragment.AuthorCursorAdapter}'s
         * list items.
         */
        public class BindingHolder extends RecyclerView.ViewHolder {

            private ImageView image;
            private TextView text;

            public BindingHolder(View rowView) {
                super(rowView);
                image = (ImageView) rowView.findViewById(R.id.list_item_author_image);
                text = (TextView) rowView.findViewById(R.id.list_item_author_name);
            }
        }

        private Cursor mCursor;

        public void setCursor(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return new BindingHolder(inflater.inflate(R.layout.list_item_author, parent, false));
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
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean conservative = sharedPref.getBoolean(SettingsFragment.KEY_PREF_CONSERVATIVE_ENABLED, true);
            if (!conservative) {
                int[] colors = getResources().getIntArray(R.array.md_colors_300);
                holder.image.setBackgroundColor(colors[(int) (author.id % colors.length)]);
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
