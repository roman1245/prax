package xyz.kandrac.library.flow.importwizard;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import xyz.kandrac.library.R;
import xyz.kandrac.library.utils.BackupUtils;

/**
 * Actual screen for importing data from CSV files into database.
 * <p/>
 * Created by kandrac on 22/02/16.
 */
public class ImportFragment extends Fragment {

    private static final int CHOOSE_FILE_REQUEST = 100;
    private static final int CHOOSE_FILE_PERMISSION = 200;

    private Uri mImportFileUri;
    // TODO: replace this charset with something selected
    private String mCharset = "UTF-8";

    @Bind(R.id.import_file_name)
    public TextView fileNameText;

    @Bind(R.id.import_columns)
    public LinearLayout columnsView;

    @Bind(R.id.import_import)
    public Button mImport;

    /**
     * Based on {@link #mImportFileUri} obtained via file provider try to run importing from that
     * file into local database.
     *
     * @param view clicked
     */
    @OnClick(R.id.import_import)
    public void importCsv(View view) {
        if (mImportFileUri == null) {
            return;
        }

        ArrayList<BackupUtils.CsvColumn> columns = new ArrayList<>();

        for (int i = 0; i < columnsView.getChildCount(); i++) {
            int selectedPosition = ((Spinner) columnsView.findViewWithTag(i)).getSelectedItemPosition();
            switch (selectedPosition) {
                case 0:
                    break;
                case 1:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_TITLE));
                    break;
                case 2:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_AUTHOR));
                    break;
                case 3:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_PUBLISHER));
                    break;
                case 4:
                    columns.add(new BackupUtils.CsvColumn(i, BackupUtils.CsvColumn.COLUMN_ISBN));
                    break;
            }
        }
        new ImportAsyncTask(columns.toArray(new BackupUtils.CsvColumn[columns.size()])).execute();
    }

    private class ImportAsyncTask extends AsyncTask<Void, Void, Integer> {

        private BackupUtils.CsvColumn[] mColumns;

        public ImportAsyncTask(BackupUtils.CsvColumn[] columns) {
            mColumns = columns;
        }

        @Override
        protected void onPreExecute() {
            mImport.setEnabled(false);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                return BackupUtils.importCSV(getActivity(), mImportFileUri, mColumns, mCharset);
            } catch (IOException exception) {
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer imported) {
            Toast.makeText(getActivity(), getString(R.string.import_imported, imported), Toast.LENGTH_SHORT).show();
            mImport.setEnabled(true);
            getActivity().finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_FILE_REQUEST:

                if (resultCode == Activity.RESULT_OK && data.getData() != null) {
                    mImportFileUri = data.getData();
                    fileNameText.setText(BackupUtils.getFileName(getActivity(), mImportFileUri));

                    columnsView.removeAllViews();

                    String[] columnValues = BackupUtils.getSampleRow(getActivity(), mImportFileUri, mCharset);

                    if (columnValues == null) {
                        return;
                    }

                    for (int i = 0; i < columnValues.length; i++) {
                        View row = getActivity().getLayoutInflater().inflate(R.layout.list_item_import_column, columnsView, true);
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

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
