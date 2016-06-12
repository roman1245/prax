package xyz.kandrac.library.flow.importwizard;

import android.net.Uri;

import java.util.ArrayList;

import xyz.kandrac.library.utils.BackupUtils;

/**
 * Created by Jan Kandrac on 11.6.2016.
 */
public interface ImportFlowHandler {
    void aboutContinue();
    void fileSelected(Uri data);
    void formattingSelected(String formatting);
    void importCsv(ArrayList<BackupUtils.CsvColumn> columns);
}
