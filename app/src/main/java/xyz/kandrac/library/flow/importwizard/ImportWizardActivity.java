package xyz.kandrac.library.flow.importwizard;

import android.app.LoaderManager;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import xyz.kandrac.library.R;
import xyz.kandrac.library.utils.BackupUtils;
import xyz.kandrac.library.utils.LogUtils;

/**
 * Activity that helps users import data to application properly
 * <p/>
 * Created by kandrac on 22/02/16.
 */
public class ImportWizardActivity extends AppCompatActivity implements ImportFlowHandler, LoaderManager.LoaderCallbacks<Object> {

    public static final String LOG_TAG = "Import";

    private static final int IMPORT_LOADER_ID = 132;

    private static final String FILE_URI_STATE = "file_uri_state";
    private static final String IMPORT_FIRST_STATE = "import_first_state";
    private static final String FORMATTING_STATE = "formatting_state";
    private static final String COLUMNS_STATE = "columns_state";

    private Uri mFileUri;
    private String mFormatting;
    private BackupUtils.CsvColumn[] mColumns;
    private boolean mImportFirst;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        // setup first fragment
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, new ImportAboutFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void aboutContinue() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ImportFileFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void fileSelected(Uri data) {
        mFileUri = data;

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, ImportFormattingFragment.getInstance(data))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void formattingSelected(String formatting) {
        mFormatting = formatting;

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, ImportAssignColumnsFragment.getInstance(mFileUri, formatting))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void importCsv(ArrayList<BackupUtils.CsvColumn> columns, boolean importFirst) {
        mColumns = columns.toArray(new BackupUtils.CsvColumn[columns.size()]);
        mImportFirst = importFirst;
        LogUtils.d(LOG_TAG, "initializing import from CSV");
        getLoaderManager().initLoader(IMPORT_LOADER_ID, null, this).forceLoad();
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        LogUtils.d(LOG_TAG, "Creating import loader");
        switch (id) {
            case IMPORT_LOADER_ID:
                return new ImportAsyncTaskLoader.Builder()
                        .setFileUri(mFileUri)
                        .setColumns(mColumns)
                        .setFormatting(mFormatting)
                        .setImportFirst(mImportFirst)
                        .build(this);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        LogUtils.d(LOG_TAG, "Import loader finished");
        switch (loader.getId()) {
            case IMPORT_LOADER_ID:
                Toast.makeText(ImportWizardActivity.this, getString(R.string.import_imported, data), Toast.LENGTH_SHORT).show();
                finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelableArray(COLUMNS_STATE, mColumns);
        outState.putParcelable(FILE_URI_STATE, mFileUri);
        outState.putString(FORMATTING_STATE, mFormatting);
        outState.putBoolean(IMPORT_FIRST_STATE, mImportFirst);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mColumns = (BackupUtils.CsvColumn[]) savedInstanceState.getParcelableArray(COLUMNS_STATE);
        mFileUri = savedInstanceState.getParcelable(FILE_URI_STATE);
        mFormatting = savedInstanceState.getString(FORMATTING_STATE);
        mImportFirst = savedInstanceState.getBoolean(IMPORT_FIRST_STATE);
    }
}
