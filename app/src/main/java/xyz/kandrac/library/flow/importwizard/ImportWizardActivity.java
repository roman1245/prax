package xyz.kandrac.library.flow.importwizard;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import butterknife.ButterKnife;
import xyz.kandrac.library.R;

/**
 * Activity that helps users import data to application properly
 * <p/>
 * Created by kandrac on 22/02/16.
 */
public class ImportWizardActivity extends AppCompatActivity implements ImportFlowHandler {

    private Uri mFileUri;

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
                .replace(R.id.fragment_container, ImportFormatting.getInstance(data))
                .addToBackStack(null)
                .commit();
    }
}
