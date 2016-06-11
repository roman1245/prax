package xyz.kandrac.library.flow.importwizard;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import xyz.kandrac.library.R;

/**
 * Created by Jan Kandrac on 11.6.2016.
 */
public class ImportFormatting extends Fragment {

    public static final String FILE_URI_ARGUMENT = "file_uri";

    public static ImportFormatting getInstance(Uri fileUri) {
        Bundle arguments = new Bundle();
        arguments.putString(FILE_URI_ARGUMENT, fileUri.toString());
        ImportFormatting result = new ImportFormatting();
        result.setArguments(arguments);
        return result;
    }

    private ImportFlowHandler handler;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        handler = (ImportFlowHandler) activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            handler = (ImportFlowHandler) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_import_formatting, container, false);
        ButterKnife.bind(result);
        return result;
    }

}
