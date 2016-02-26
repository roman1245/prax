package xyz.kandrac.library.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

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

    @Bind(R.id.import_file_name)
    public TextView fileNameText;

    @Bind(R.id.import_columns)
    public RecyclerView columnsView;

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

        try {
            BackupUtils.importCSV(getActivity(), mImportFileUri);
        } catch (IOException ex) {
            Toast.makeText(getActivity(), "errror : " + mImportFileUri.getPath(), Toast.LENGTH_LONG).show();
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
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            startActivityForResult(chooserIntent, CHOOSE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity().getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_FILE_REQUEST:

                if (resultCode == Activity.RESULT_OK && data.getData() != null) {
                    mImportFileUri = data.getData();
                    fileNameText.setText(BackupUtils.getFileName(getActivity(), mImportFileUri));
                } else {
                    Toast.makeText(getActivity(), "File not choosen", Toast.LENGTH_SHORT).show();
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
