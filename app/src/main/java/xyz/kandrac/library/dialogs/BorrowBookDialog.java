package xyz.kandrac.library.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import xyz.kandrac.library.NotificationReceiver;
import xyz.kandrac.library.R;
import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.utils.AutoCompleteContactAdapter;
import xyz.kandrac.library.utils.DateUtils;

/**
 * Created by Jan Kandrac on 5.6.2016.
 */
public class BorrowBookDialog extends DialogFragment {

    public static final String ARGUMENT_BOOK_ID = "book_id_argument";

    private long mBookId;

    public static BorrowBookDialog getInstance(long bookId) {
        BorrowBookDialog result = new BorrowBookDialog();
        Bundle arguments = new Bundle();
        arguments.putLong(ARGUMENT_BOOK_ID, bookId);
        result.setArguments(arguments);
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBookId = getArguments().getLong(ARGUMENT_BOOK_ID);
    }

    @Override
    public void onStart() {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        AlertDialog d = (AlertDialog) getDialog();
        final View content = d.findViewById(R.id.dialog_content);
        Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AutoCompleteTextView to = (AutoCompleteTextView) content.findViewById(R.id.borrow_to);
                EditText date = (EditText) content.findViewById(R.id.borrow_date_text);

                final long dateBorrowed = new Date(System.currentTimeMillis()).getTime();
                final String name = to.getText().toString().trim();
                long timeToNotify;

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getActivity(), R.string.empty_name, Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    long untilDate = DateFormat.getDateInstance().parse(date.getText().toString()).getTime();
                    timeToNotify = DateUtils.getHourTime(untilDate, 18);
                } catch (ParseException ex) {
                    Toast.makeText(getActivity(), R.string.incorrect_date_format, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (timeToNotify < dateBorrowed) {
                    Toast.makeText(getActivity(), R.string.incorrect_borrow_date, Toast.LENGTH_SHORT).show();
                    return;
                }

                ContentValues borrowContentValues = new ContentValues();
                borrowContentValues.put(Contract.BorrowInfo.BORROW_TO, name);
                borrowContentValues.put(Contract.BorrowInfo.BORROW_DATE_BORROWED, dateBorrowed);
                borrowContentValues.put(Contract.BorrowInfo.BORROW_NAME, name);
                borrowContentValues.put(Contract.BorrowInfo.BORROW_NEXT_NOTIFICATION, timeToNotify);

                getActivity().getContentResolver().insert(Contract.Books.buildBorrowInfoUri(mBookId), borrowContentValues);

                ContentValues bookContentValues = new ContentValues();
                bookContentValues.put(Contract.Books.BOOK_BORROWED, true);
                bookContentValues.put(Contract.Books.BOOK_UPDATED_AT, System.currentTimeMillis());
                getActivity().getContentResolver().update(Contract.Books.buildBookUri(mBookId), bookContentValues, null, null);

                NotificationReceiver.prepareNotification(getActivity(), timeToNotify, mBookId);
                dismiss();
            }
        });
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View content = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_borrow, null);
        setContent(content);

        return new AlertDialog.Builder(getActivity())
                .setView(content)
                .setMessage(R.string.borrow_message)
                .setTitle(R.string.borrow_title)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
    }

    private void setContent(View content) {
        final AutoCompleteTextView to = (AutoCompleteTextView) content.findViewById(R.id.borrow_to);
        final EditText date = (EditText) content.findViewById(R.id.borrow_date_text);
        final ImageButton dateButton = (ImageButton) content.findViewById(R.id.borrow_date_button);

        AutoCompleteContactAdapter.createAutocompleteTextViewAdapter(to);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar c = Calendar.getInstance();
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, monthOfYear);
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        date.setText(DateFormat.getDateInstance().format(c.getTime()));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }
}
