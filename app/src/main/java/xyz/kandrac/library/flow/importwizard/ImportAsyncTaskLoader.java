package xyz.kandrac.library.flow.importwizard;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;

import java.io.IOException;

import xyz.kandrac.library.utils.BackupUtils;
import xyz.kandrac.library.utils.LogUtils;

/**
 * Created by Jan Kandrac on 18.6.2016.
 */
public class ImportAsyncTaskLoader extends AsyncTaskLoader<Object> {

    private BackupUtils.CsvColumn[] mColumns;
    private boolean mImportFirst;
    private Uri mFileUri;
    private String mFormatting;

    public static class Builder {

        private BackupUtils.CsvColumn[] mColumns;
        private boolean mImportFirst;
        private Uri mFileUri;
        private String mFormatting;

        public Builder setColumns(BackupUtils.CsvColumn[] columns) {
            mColumns = columns;
            return this;
        }

        public Builder setImportFirst(boolean importFirst) {
            mImportFirst = importFirst;
            return this;
        }

        public Builder setFileUri(Uri fileUri) {
            mFileUri = fileUri;
            return this;
        }

        public Builder setFormatting(String formatting) {
            mFormatting = formatting;
            return this;
        }

        public ImportAsyncTaskLoader build(Context context) {
            ImportAsyncTaskLoader result = new ImportAsyncTaskLoader(context);
            result.mColumns = mColumns;
            result.mImportFirst = mImportFirst;
            result.mFileUri = mFileUri;
            result.mFormatting = mFormatting;
            return result;
        }
    }

    public ImportAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public Integer loadInBackground() {
        try {
            LogUtils.d("import", "loading");
            return BackupUtils.importCSV(getContext(), mFileUri, mColumns, mFormatting, mImportFirst);
        } catch (IOException exception) {
            LogUtils.d("import", "loading - 0 ");
            return 0;
        }
    }
}
