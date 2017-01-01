package xyz.kandrac.library.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import xyz.kandrac.library.R;

/**
 * Created by jan on 1.1.2017.
 */
public class EditTextDialog {

    public interface OnPositiveActionListener {
        void onPositiveAction(DialogInterface dialogInterface, String text);
    }

    public static AlertDialog.Builder create(Context context, String positiveButtonText, final OnPositiveActionListener listener) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.dialog_edit_text, null);
        alert.setView(layout);
        final EditText name = (EditText) layout.findViewById(R.id.dialog_content);

        alert.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onPositiveAction(dialogInterface, name.getText().toString());
            }
        });

        return alert;
    }
}
