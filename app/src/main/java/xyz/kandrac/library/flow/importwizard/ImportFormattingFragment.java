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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import xyz.kandrac.library.R;
import xyz.kandrac.library.utils.BackupUtils;

/**
 * Created by Jan Kandrac on 11.6.2016.
 */
public class ImportFormattingFragment extends Fragment {

    public static final String FILE_URI_ARGUMENT = "file_uri";

    public static ImportFormattingFragment getInstance(Uri fileUri) {
        Bundle arguments = new Bundle();
        arguments.putString(FILE_URI_ARGUMENT, fileUri.toString());
        ImportFormattingFragment result = new ImportFormattingFragment();
        result.setArguments(arguments);
        return result;
    }

    private ImportFlowHandler handler;
    private Uri mFileUri;

    @Bind(R.id.import_formatting)
    public Spinner mSpinner;

    @Bind(R.id.import_samples)
    public TextView mSample;

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
        mFileUri = Uri.parse(getArguments().getString(FILE_URI_ARGUMENT));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_import_formatting, container, false);
        ButterKnife.bind(this, result);
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        show();
    }

    @OnItemSelected(R.id.import_formatting)
    public void spinnerSelected(Spinner spinner, int position) {
        show();
    }

    private void show() {
        String[] values = BackupUtils.getSampleRow(getActivity(), mFileUri, mSpinner.getSelectedItem().toString());

        if (values == null || values.length == 0) {
            Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length - 1; i++) {
            builder.append(values[i]).append(", ");
        }
        builder.append(values[values.length - 1]);

        mSample.setText(builder.toString());
    }

    @OnClick(R.id.import_continue)
    public void onContinue(View view) {
        handler.formattingSelected(mSpinner.getSelectedItem().toString());
    }
}
