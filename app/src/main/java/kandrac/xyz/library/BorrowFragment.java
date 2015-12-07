package kandrac.xyz.library;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import kandrac.xyz.library.model.Contract;

/**
 * Created by kandrac on 03/12/15.
 */
public class BorrowFragment extends DialogFragment {

    private static final String EXTRA_ID = "id";
    private static final String EXTRA_FROM = "from";
    private static final String EXTRA_TO = "to";
    private static final String EXTRA_NAME = "name";

    private long mId;
    private long mFrom;
    private long mTo;
    private String mName;

    @Bind(R.id.borrow_date_from)
    TextView dateFrom;

    @Bind(R.id.borrow_date_to)
    TextView dateTo;

    @Bind(R.id.borrow_name)
    TextView name;

    @Bind(R.id.borrow_return)
    Button returnButton;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd. MM. yyyy", Locale.getDefault());

    public static BorrowFragment getInstance(String name, long from, long to, long id) {
        BorrowFragment result = new BorrowFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(EXTRA_ID, id);
        arguments.putLong(EXTRA_FROM, from);
        arguments.putLong(EXTRA_TO, to);
        arguments.putString(EXTRA_NAME, name);
        result.setArguments(arguments);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getArguments().getLong(EXTRA_ID);
        mFrom = getArguments().getLong(EXTRA_FROM);
        mTo = getArguments().getLong(EXTRA_TO);
        mName = getArguments().getString(EXTRA_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.borrow_dialog, container, false);
        ButterKnife.bind(this, result);
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dateFrom.setText("Borrowed: " + dateFormat.format(new Date(mFrom)));
        dateTo.setText("Returned:" + dateFormat.format(new Date(mTo)));
        name.setText(mName);

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cv = new ContentValues();
                cv.put(Contract.BorrowInfo.BORROW_DATE_RETURNED, new Date(System.currentTimeMillis()).getTime());
                getActivity().getContentResolver().update(Contract.BorrowInfo.buildUri(mId), cv, null, null);
                dismiss();
            }
        });
    }
}
