package com.androidclass.bookshelf;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;

//Adapter class used by Read and Toread activities to display a custom ListView, that includes
//images alongside text, which is the main purpose of this adapter.
public class BookAdapter extends ArrayAdapter<Book> {

    private Context mContext;
    private List<Book> mBookList = new ArrayList<>();

    public BookAdapter(@NonNull Context context, @LayoutRes ArrayList<Book> list) {
        super(context, 0, list);
        mContext = context;
        mBookList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.book_list, parent, false);
        }

        Book currentBook = mBookList.get(position);

        ImageView image = (ImageView) listItem.findViewById(R.id.imageView_cover);
        Glide.with(mContext).load(currentBook.getmCoverUrl()).placeholder(R.drawable.ic_book_placeholder)
        .into(image);

        TextView author = (TextView) listItem.findViewById(R.id.textView_author_selected);
        author.setText(currentBook.getmAuthor());

        TextView title = (TextView) listItem.findViewById(R.id.textView_title);
        title.setText(currentBook.getmTitle());

        TextView date = (TextView) listItem.findViewById(R.id.textView_releasedate);
        date.setText(currentBook.getmDate());

        Log.d("BookdAdapter", "imageurl: " + currentBook.getmCoverUrl() + " title: " + currentBook.getmTitle());

        return listItem;
    }

    public Book getCurrentBook (int position) {
        return mBookList.get(position);
    }
}
