package xyz.kandrac.library.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
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
     * Select file via file provider if permission {@link android.Manifest.permission#READ_EXTERNAL_STORAGE}
     * is granted. If permission is not already granted, run {@link ActivityCompat#requestPermissions(Activity, String[], int)}
     * with {@link #CHOOSE_FILE_PERMISSION} request code.
     *
     * @param view clicked
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    @OnClick(R.id.import_select_file)
    public void selectFile(View view) {

        int permission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            invokeChooser();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CHOOSE_FILE_PERMISSION);
        }

    }

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

    private void invokeChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        sIntent.putExtra("CONTENT_TYPE", "text/*");
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;

        if (getActivity().getPackageManager().resolveActivity(sIntent, 0) != null) {
            chooserIntent = Intent.createChooser(sIntent, getString(R.string.import_chooser));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, getString(R.string.import_chooser));
        }

        try {
            startActivityForResult(chooserIntent, CHOOSE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.import_no_file_manager, Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_import, container, false);
        ButterKnife.bind(this, result);
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CHOOSE_FILE_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    invokeChooser();
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
