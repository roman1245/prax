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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import xyz.kandrac.library.LibraryDetailActivity;
import xyz.kandrac.library.R;
import xyz.kandrac.library.Searchable;
import xyz.kandrac.library.fragments.SettingsFragment;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.obj.Library;
import xyz.kandrac.library.mvp.view.MainActivity;

/**
 * Created by kandrac on 22/10/15.
 */
public class LibraryBooksListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Searchable {

    LibraryAdapter adapter;

    String searchQuery;

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

    @Override
    public void clearFilter(String field) {

    }

    @Override
    public void requestFilter(String field, String[] arguments) {

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

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean conservative = sharedPref.getBoolean(SettingsFragment.KEY_PREF_CONSERVATIVE_ENABLED, true);
            if (!conservative) {
                int[] colors = getResources().getIntArray(R.array.md_colors_300);
                holder.image.setBackgroundColor(colors[(int) (library.id % colors.length)]);
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
