package xyz.kandrac.library.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import xyz.kandrac.library.R;

/**
 * Expansion of alert dialog to provide single {@link AutoCompleteTextView} or
 * {@link android.widget.EditText}.
 * <p>
 * Created by jan on 1.1.2017.
 */
public class EditTextDialog {

    public interface OnPositiveActionListener {
        void onPositiveAction(DialogInterface dialogInterface, String text);
    }

    public static class Builder extends AlertDialog.Builder {

        private String positiveButtonText;
        private OnPositiveActionListener listener;
        private Uri autocompleteUri;
        private String autocompleteColumn;

        public Builder(@NonNull Context context) {
            super(context);
        }

        public Builder setPositiveButton(String positiveButtonText, OnPositiveActionListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.listener = listener;
            return this;
        }

        public Builder setAutocompleteUri(Uri autocompleteUri) {
            this.autocompleteUri = autocompleteUri;
            return this;
        }

        public Builder setAutocompleteColumn(String autocompleteColumn) {
            this.autocompleteColumn = autocompleteColumn;
            return this;
        }

        public AlertDialog create() {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View layout = inflater.inflate(R.layout.dialog_edit_text, null);
            setView(layout);
            final AutoCompleteTextView name = (AutoCompleteTextView) layout.findViewById(R.id.dialog_content);

            if (autocompleteUri != null && !TextUtils.isEmpty(autocompleteColumn))
                AutoCompleteUtils.setAdapter(getContext(), autocompleteUri, autocompleteColumn, name);

            setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    listener.onPositiveAction(dialogInterface, name.getText().toString());
                }
            });

            return super.create();
        }
    }
}
