package xyz.kandrac.library.fragments;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import xyz.kandrac.library.R;
import xyz.kandrac.library.model.Contract;

/**
 * Created by jan on 25.02.17.
 */

public class GenreSpinnerAdapter extends ArrayAdapter<String> implements LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<String> items;
    private ArrayList<Long> ids;

    public GenreSpinnerAdapter(@NonNull Activity activity) {
        super(activity, android.R.layout.simple_list_item_1);
        activity.getLoaderManager().initLoader(412, null, this);
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ids.get(position);
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == 412) {
            return new CursorLoader(getContext(), Contract.Genres.USED_GENRES_URI, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == 412) {
            if (data != null && data.moveToFirst()) {
                items = new ArrayList<>();
                ids = new ArrayList<>();
                items.add(getContext().getString(R.string.genre_select));
                ids.add(0L);
                do {
                    items.add(data.getString(data.getColumnIndex(Contract.Genres.GENRE_NAME)));
                    ids.add(data.getLong(data.getColumnIndex(Contract.Genres.GENRE_ID)));
                } while (data.moveToNext());
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
