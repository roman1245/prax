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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

    private Spinner mSpinner;
    private TextView mSample;
    private Button mContinue;

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
        mSpinner = (Spinner) result.findViewById(R.id.import_formatting);
        mSample = (TextView) result.findViewById(R.id.import_samples);
        mContinue = (Button) result.findViewById(R.id.import_continue);
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.formattingSelected(mSpinner.getSelectedItem().toString());
            }
        });
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
}
