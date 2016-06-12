package xyz.kandrac.library.flow.importwizard;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.ButterKnife;
import xyz.kandrac.library.R;
import xyz.kandrac.library.utils.BackupUtils;

/**
 * Activity that helps users import data to application properly
 * <p/>
 * Created by kandrac on 22/02/16.
 */
public class ImportWizardActivity extends AppCompatActivity implements ImportFlowHandler {

    private Uri mFileUri;
    private String mFormatting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        ButterKnife.bind(this);

        // setup first fragment
        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new ImportAboutFragment())
                .commit();
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
    public void importCsv(ArrayList<BackupUtils.CsvColumn> columns) {
        new ImportAsyncTask(columns.toArray(new BackupUtils.CsvColumn[columns.size()])).execute();
    }

    private class ImportAsyncTask extends AsyncTask<Void, Void, Integer> {

        private BackupUtils.CsvColumn[] mColumns;

        public ImportAsyncTask(BackupUtils.CsvColumn[] columns) {
            mColumns = columns;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                return BackupUtils.importCSV(ImportWizardActivity.this, mFileUri, mColumns, mFormatting);
            } catch (IOException exception) {
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer imported) {
            Toast.makeText(ImportWizardActivity.this, getString(R.string.import_imported, imported), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
