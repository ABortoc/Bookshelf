package com.androidclass.bookshelf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class ToRead extends AppCompatActivity {

    private ListView mListView;
    private BookAdapter mBookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_read);

        mListView = (ListView) findViewById(R.id.book_list2);
        ArrayList<Book> bookList = new ArrayList<>();
        bookList.add(new Book("Joe Abercrombie", "The Blade Itself", "May, 2006",
                ContextCompat.getDrawable(this, R.drawable.ic_book_placeholder)));
        bookList.add(new Book("Joe Abercrombie", "Before They Are Hanged", "March, 2007",
                ContextCompat.getDrawable(this, R.drawable.ic_book_placeholder)));
        bookList.add(new Book("Joe Abercrombie", "Last Argument of Kings", "March, 2008",
                ContextCompat.getDrawable(this, R.drawable.ic_book_placeholder)));

        mBookAdapter = new BookAdapter(this, bookList);
        mListView.setAdapter(mBookAdapter);
    }
}
