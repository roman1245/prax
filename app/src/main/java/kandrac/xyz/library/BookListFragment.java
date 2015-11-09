package kandrac.xyz.library;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kandrac.xyz.library.databinding.BookListItemBinding;
import kandrac.xyz.library.model.Contract;
import kandrac.xyz.library.model.obj.Book;
import kandrac.xyz.library.utils.DisplayUtils;

/**
 * Created by kandrac on 20/10/15.
 */
public class BookListFragment extends SubtitledFragment implements LoaderManager.LoaderCallbacks<Cursor>, Searchable {

    @Bind(R.id.list)
    RecyclerView list;

    @OnClick(R.id.fab)
    public void addItem(View view) {
        startActivity(new Intent(getActivity(), EditBookActivity.class));
    }

    BookCursorAdapter adapter;
    String searchQuery;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.book_list_fragment, container, false);
        ButterKnife.bind(this, result);

        adapter = new BookCursorAdapter();
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(adapter);

        // Init database loading
        getActivity().getSupportLoaderManager().initLoader(MainActivity.BOOK_LIST_LOADER, null, this);

        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == MainActivity.BOOK_LIST_LOADER) {

            String selection = null;
            String[] selectionArgs = null;

            if (searchQuery != null && searchQuery.length() > 1) {
                selection = Contract.Books.BOOK_TITLE + " LIKE ?" +
                        " OR " + Contract.Authors.AUTHOR_NAME + " LIKE ?" +
                        " OR " + Contract.Books.BOOK_DESCRIPTION + " LIKE ? " +
                        " OR " + Contract.Books.BOOK_ISBN + " LIKE ? ";
                selectionArgs = new String[]{
                        "%" + searchQuery + "%",
                        "%" + searchQuery + "%",
                        "%" + searchQuery + "%",
                        "%" + searchQuery + "%"
                };
            }

            return new CursorLoader(
                    getActivity(),
                    Contract.BOOKS_AUTHORS_URI,
                    new String[]{"books." + Contract.Books.BOOK_ID, Contract.Books.BOOK_TITLE, Contract.Books.BOOK_IMAGE_FILE, Contract.Authors.AUTHOR_NAME},
                    selection,
                    selectionArgs,
                    null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.setCursor(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public boolean requestSearch(String query) {
        searchQuery = query;
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.BOOK_LIST_LOADER, null, this);
        return true;
    }

    @Override
    public int getTitle() {
        return R.string.menu_books_mine;
    }


    private class BookCursorAdapter extends RecyclerView.Adapter<BookCursorAdapter.BindingHolder> {

        public class BindingHolder extends RecyclerView.ViewHolder {
            public BindingHolder(View rowView) {
                super(rowView);
            }
        }

        Cursor mCursor;

        public void setCursor(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                    Intent intent = new Intent(getContext(), BookDetailActivity.class);
                    intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.id);
                    startActivity(intent);
                }
            });

            DisplayUtils.displayScaledImage(getActivity(), book.imageFilePath, (ImageView) holder.itemView.findViewById(R.id.image));
        }

        @Override
        public int getItemCount() {
            if (mCursor != null) {
                return mCursor.getCount();
            } else return 0;
        }
    }
}
