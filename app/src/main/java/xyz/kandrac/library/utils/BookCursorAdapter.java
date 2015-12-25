package xyz.kandrac.library.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import xyz.kandrac.library.BookDetailActivity;
import xyz.kandrac.library.R;
import xyz.kandrac.library.model.obj.Book;

/**
 * Created by kandrac on 23/11/15.
 */
public class BookCursorAdapter extends RecyclerView.Adapter<BookCursorAdapter.BindingHolder> {

    private Context mContext;
    private Cursor mCursor;

    public class BindingHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView title;
        private TextView subtitle;

        public BindingHolder(View rowView) {
            super(rowView);
            image = (ImageView) rowView.findViewById(R.id.list_item_book_image);
            title = (TextView) rowView.findViewById(R.id.list_item_book_title);
            subtitle = (TextView) rowView.findViewById(R.id.list_item_book_subtitle);
        }
    }

    public BookCursorAdapter(Context context) {
        mContext = context;
    }

    public void setCursor(Cursor cursor) {
        Log.d("jano", "" + cursor.getCount());
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new BindingHolder(inflater.inflate(R.layout.book_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        mCursor.moveToPosition(position);
        final Book book = new Book(mCursor);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, BookDetailActivity.class);
                intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.id);
                mContext.startActivity(intent);
            }
        });

        DisplayUtils.displayScaledImage(mContext, book.imageFilePath, holder.image);
        holder.title.setText(book.title);
        holder.subtitle.setText(book.authorsReadable);
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            Log.d("BookCursorAdapter", Integer.toString(mCursor.getCount()));
            return mCursor.getCount();
        } else {
            Log.d("BookCursorAdapter", "0");
            return 0;
        }
    }
}
