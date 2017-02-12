package xyz.kandrac.library.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import xyz.kandrac.library.R;
import xyz.kandrac.library.model.firebase.FirebaseFeedback;

import static xyz.kandrac.library.model.firebase.References.FEEDBACK_REFERENCE;

/**
 * Created by jan on 11.2.2017.
 */

public class FeedbackFragment extends android.app.Fragment {

    RecyclerView list;
    FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.feedback_fragment, container, false);
        list = (RecyclerView) result.findViewById(R.id.list);
        fab = (FloatingActionButton) result.findViewById(R.id.fab);
        return result;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(new FeedbackAdapter());
    }

    private class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.Holder> {

        private ArrayList<FirebaseFeedback> data;
        private ArrayList<String> keys;

        FeedbackAdapter() {
            data = new ArrayList<>();
            keys = new ArrayList<>();

            FirebaseDatabase.getInstance()
                    .getReference()
                    .child(FEEDBACK_REFERENCE)
                    .orderByChild(FirebaseFeedback.KEY_APPROVED)
                    .equalTo(true)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            data.clear();
                            keys.clear();
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                data.add(child.getValue(FirebaseFeedback.class));
                                keys.add(child.getKey());
                            }
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new Holder(inflater.inflate(R.layout.list_item_feedback, parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(Holder holder, int position) {
            FirebaseFeedback feedback = data.get(position);
            holder.title.setText(feedback.title);
            holder.description.setText(feedback.description);
            holder.developerComment.setText(feedback.comment);
            holder.score.setText(Integer.toString(feedback.votes));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class Holder extends RecyclerView.ViewHolder {

            TextView title;
            TextView description;
            TextView developerComment;
            TextView score;

            public Holder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.feedback_title);
                description = (TextView) itemView.findViewById(R.id.feedback_description);
                developerComment = (TextView) itemView.findViewById(R.id.feedback_developer_comment);
                score = (TextView) itemView.findViewById(R.id.feedback_score);
            }
        }
    }
}
