package kandrac.xyz.library.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import kandrac.xyz.library.BookDetailActivity;
import kandrac.xyz.library.R;
import kandrac.xyz.library.databinding.BookListItemBinding;
import kandrac.xyz.library.model.obj.Book;

/**
 * Created by kandrac on 23/11/15.
 */
public class BookCursorAdapter extends RecyclerView.Adapter<BookCursorAdapter.BindingHolder> {

    Context mContext;

    public class BindingHolder extends RecyclerView.ViewHolder {
        public BindingHolder(View rowView) {
            super(rowView);
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
        return new BindingHolder(BookListItemBinding.inflate(inflater, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        mCursor.moveToPosition(position);
        final Book book = new Book(mCursor);

        BookListItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
        binding.setBook(book);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, BookDetailActivity.class);
                intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.id);
                mContext.startActivity(intent);
            }
        });

        DisplayUtils.displayScaledImage(mContext, book.imageFilePath, (ImageView) holder.itemView.findViewById(R.id.image));
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else return 0;
    }
}
