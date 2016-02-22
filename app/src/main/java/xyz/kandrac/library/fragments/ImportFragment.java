package xyz.kandrac.library.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.kandrac.library.R;

/**
 * Actual screen for importing data from CSV files into database.
 * <p/>
 * Created by kandrac on 22/02/16.
 */
public class ImportFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_import, container, false);
    }
}
