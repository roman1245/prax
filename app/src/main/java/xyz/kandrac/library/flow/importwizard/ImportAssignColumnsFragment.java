package xyz.kandrac.library.flow.importwizard;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import xyz.kandrac.library.R;
import xyz.kandrac.library.utils.BackupUtils;

/**
 * Created by Jan Kandrac on 12.6.2016.
 */
public class ImportAssignColumnsFragment extends Fragment implements View.OnClickListener {

    public static final String ARGUMENT_FILE_URI = "file_uri_arg";
    public static final String ARGUMENT_FORMATTING = "format_arg";

    private ImportFlowHandler handler;
    private Uri mFileUri;
    private String mFormatting;

    private LinearLayout mColumns;
    private Switch mImportFirst;
    private Button mContinue;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_import_assign_columns, container, false);
        mColumns = (LinearLayout) result.findViewById(R.id.import_columns);
        mImportFirst = (Switch) result.findViewById(R.id.import_first_row);
        mContinue = (Button) result.findViewById(R.id.import_continue);
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mColumns.removeAllViews();
        mContinue.setOnClickListener(this);

        String[] columnValues = BackupUtils.getSampleRow(getActivity(), mFileUri, mFormatting);

        if (columnValues == null) {
            return;
        }

        for (int i = 0; i < columnValues.length; i++) {
            View row = getActivity().getLayoutInflater().inflate(R.layout.list_item_import_column, mColumns, true);
            TextView rowId = (TextView) row.findViewById(R.id.column_id);
            TextView rowText = (TextView) row.findViewById(R.id.column_text);
            Spinner rowRepresent = (Spinner) row.findViewById(R.id.column_representation);
            rowId.setText(getString(R.string.format_order, i + 1));
            rowText.setText(columnValues[i]);
            // clear ids so that you can find by id newly added items
            rowId.setId(View.NO_ID);
            rowText.setId(View.NO_ID);
            rowRepresent.setId(View.NO_ID);
            // tag set for representation
            rowRepresent.setTag(i);
        }
    }

    public void continueClick() {

        ArrayList<BackupUtils.CsvColumn> columns = new ArrayList<>();

        for (int i = 0; i < mColumns.getChildCount(); i++) {
            int selectedPosition = ((Spinner) mColumns.findViewWithTag(i)).getSelectedItemPosition();
            switch (selectedPosition) {
                case 0:
                    break;
                case 1:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_TITLE));
                    break;
                case 2:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_SUBTITLE));
                    break;
                case 3:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_DESCRIPTION));
                    break;
                case 4:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_AUTHOR));
                    break;
                case 5:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_PUBLISHER));
                    break;
                case 6:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_ISBN));
                    break;
                case 7:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_BORROWED_TO));
                    break;
                case 8:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_BORROWED_FROM));
                    break;
                case 9:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_WISH));
                    break;
                case 10:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_PUBLISHED));
                    break;
            }
        }

        handler.importCsv(columns, mImportFirst.isChecked());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.import_continue:
                continueClick();
        }
    }
}
