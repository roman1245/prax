package kandrac.xyz.library.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import kandrac.xyz.library.BookDetailActivity;
import kandrac.xyz.library.R;
import kandrac.xyz.library.model.obj.Book;

/**
 * Created by kandrac on 23/11/15.
 */
public class BookCursorAdapter extends RecyclerView.Adapter<BookCursorAdapter.BindingHolder> {

    Context mContext;

    public class BindingHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView subtitle;

        public BindingHolder(View rowView) {
            super(rowView);
            image = (ImageView) rowView.findViewById(R.id.image);
            title = (TextView) rowView.findViewById(R.id.line1);
            subtitle = (TextView) rowView.findViewById(R.id.line2);
        }
    }


    public BookCursorAdapter(Context context) {
        mContext = context;
    }

    Cursor mCursor;

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
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

        DisplayUtils.displayScaledImage(mContext, book.imageFilePath, (ImageView) holder.itemView.findViewById(R.id.image));
        holder.title.setText(book.title);
        holder.subtitle.setText(book.authorsReadable);
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else return 0;
    }
}
