package xyz.kandrac.library.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import xyz.kandrac.library.R;

public class ImportAdapter extends RecyclerView.Adapter<ImportAdapter.ViewHolder> {

    public static final String COLUMN_BOOK_TITLE = "";
    public static final String COLUMN_AUTHOR_NAME = "";
    public static final String COLUMN_ISBN = "";
    public static final String COLUMN_PUBLISHER_NAME = "";
    public static final String COLUMN_BORROWED_TO = "";
    public static final String COLUMN_ID = "";
    public static final String COLUMN_PLACEMENT = "";

    private Context mContext;
    private String[] mContent;

    public ImportAdapter(Context context) {
        mContext = context;
    }

    public void setContent(String[] columnNames) {
        mContent = columnNames;
        notifyDataSetChanged();
    }

    /**
     * {@link ImportAdapter}'s {@code ViewHolder}. Required for using with {@link RecyclerView}
     * and really the best practice in order to be efficient with {@link android.widget.AdapterView}
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView id;
        private TextView text;
        private Spinner spinner;

        public ViewHolder(View rowView) {
            super(rowView);
            id = (TextView) rowView.findViewById(R.id.column_id);
            text = (TextView) rowView.findViewById(R.id.column_text);
            spinner = (Spinner) rowView.findViewById(R.id.column_representation);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ViewHolder(inflater.inflate(R.layout.list_item_import_column, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.id.setText(position+".");
        holder.text.setText(mContent[position]);
    }

    @Override
    public int getItemCount() {
        if (mContent != null) {
            return mContent.length;
        } else {
            return 0;
        }
    }
}
