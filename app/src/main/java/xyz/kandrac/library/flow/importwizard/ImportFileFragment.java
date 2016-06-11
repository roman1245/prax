package xyz.kandrac.library.flow.importwizard;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.kandrac.library.R;

/**
 * Created by Jan Kandrac on 11.6.2016.
 */
public class ImportFileFragment extends Fragment {

    private static final int CHOOSE_FILE_REQUEST = 100;
    private static final int CHOOSE_FILE_PERMISSION = 200;

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
        View result = inflater.inflate(R.layout.fragment_import_file, container, false);
        ButterKnife.bind(this, result);
        return result;
    }

    @OnClick({R.id.import_continue, R.id.import_excel, R.id.import_open_office})
    public void continueClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.import_continue:
                importClick();
                break;
            case R.id.import_excel:
                break;
            case R.id.import_open_office:
                break;
        }
    }

    /**
     * Import clicked - check permissions and request them or directly invoke chooser
     */
    private void importClick() {
        int permission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            invokeChooser();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CHOOSE_FILE_PERMISSION);
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

    /**
     * All permissions granted - invoke chooser
     */
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_FILE_REQUEST:
                if (resultCode == Activity.RESULT_OK && data.getData() != null) {
                    handler.fileSelected(data.getData());
                } else {
                    Toast.makeText(getActivity(), "File not selected", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
