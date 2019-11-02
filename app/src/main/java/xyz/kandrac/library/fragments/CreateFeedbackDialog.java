package xyz.kandrac.library.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import xyz.kandrac.library.R;
import xyz.kandrac.library.model.firebase.FirebaseFeedback;
import xyz.kandrac.library.model.firebase.References;

/**
 * Created by jan on 12.2.2017.
 */

public class CreateFeedbackDialog extends DialogFragment {

    private EditText author;
    private EditText title;
    private EditText text;
    private Button cancel;
    private Button send;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.dialog_create_feedback, container, false);

        author = (EditText) result.findViewById(R.id.author);
        title = (EditText) result.findViewById(R.id.title);
        text = (EditText) result.findViewById(R.id.text);

        cancel = (Button) result.findViewById(R.id.cancel);
        send = (Button) result.findViewById(R.id.send);

        return result;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleValue = title.getText().toString();
                String textValue = text.getText().toString();
                if (TextUtils.isEmpty(titleValue) || TextUtils.isEmpty(textValue)) {
                    Toast.makeText(getActivity(), R.string.add_title_and_desc, Toast.LENGTH_LONG).show();
                } else {
                    FirebaseFeedback feedback = new FirebaseFeedback();
                    feedback.author = author.getText().toString();
                    feedback.title = titleValue;
                    feedback.description = textValue;
                    feedback.approved = false;
                    feedback.votes = 0;
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child(References.FEEDBACK_REFERENCE)
                            .push()
                            .setValue(feedback);

                    Toast.makeText(getActivity(), R.string.thanks, Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }
        });
    }
}
