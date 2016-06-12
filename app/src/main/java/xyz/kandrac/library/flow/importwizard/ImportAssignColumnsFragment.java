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
 * Created by Jan Kandrac on 12.6.2016.
 */
public class ImportAssignColumnsFragment extends Fragment {

    public static final String ARGUMENT_FILE_URI = "file_uri_arg";
    public static final String ARGUMENT_FORMATTING = "format_arg";

    private ImportFlowHandler handler;
    private Uri mFileUri;
    private String mFormatting;

    public static Fragment getInstance(Uri fileUri, String formatting) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_FILE_URI, fileUri.toString());
        arguments.putString(ARGUMENT_FORMATTING, formatting);
        ImportAssignColumnsFragment result = new ImportAssignColumnsFragment();
        result.setArguments(arguments);
        return result;
    }

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileUri = Uri.parse(getArguments().getString(ARGUMENT_FILE_URI));
        mFormatting = getArguments().getString(ARGUMENT_FORMATTING);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_import_formatting, container, false);
        ButterKnife.bind(this, result);
        return result;
    }
}
