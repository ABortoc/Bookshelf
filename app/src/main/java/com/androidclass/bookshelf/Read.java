package com.androidclass.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Read extends AppCompatActivity {

    private ListView mListView;
    private BookAdapter mBookAdapter;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private ArrayList<Book> mBooksList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        addNewBook(mFirebaseUser.getUid(),"Joe Abercrombie", "The Blade Itself", "May, 2006");
        addNewBook(mFirebaseUser.getUid(),"Joe Abercrombie", "Before They Are Hanged", "March, 2007");
        addNewBook(mFirebaseUser.getUid(),"Joe Abercrombie", "Last Argument of Kings", "March, 2008");

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("read/"+mFirebaseUser.getUid()+"/");
        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot bookSnapshot: dataSnapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    Log.d("Read", "Books: " + book.getmTitle());
                    Log.d("Read", "Books: " + book.getmAuthor());
                    Log.d("Read", "Books: " + book.getmDate());
                    mBooksList.add(new Book(book.getmAuthor(), book.getmTitle(), book.getmDate(),
                            ContextCompat.getDrawable(Read.this, R.drawable.ic_book_placeholder)));
                }
                Log.d("Read books' list", "books' list:" + mBooksList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Read", "The read failed: " + databaseError.getCode());
            }
        });

        Log.d("Read books' list", "books' list:" + mBooksList);
        mListView = (ListView) findViewById(R.id.book_list);
        /*ArrayList<Book> bookList = new ArrayList<>();
        bookList.add(new Book("Joe Abercrombie", "The Blade Itself", "May, 2006",
                ContextCompat.getDrawable(this, R.drawable.ic_book_placeholder)));
        bookList.add(new Book("Joe Abercrombie", "Before They Are Hanged", "March, 2007",
                ContextCompat.getDrawable(this, R.drawable.ic_book_placeholder)));
        bookList.add(new Book("Joe Abercrombie", "Last Argument of Kings", "March, 2008",
                ContextCompat.getDrawable(this, R.drawable.ic_book_placeholder)));*/

        createView();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Read.this, SelectedBook.class);
                intent.putExtra("Author", mBookAdapter.getCurrentBook(position).getmAuthor());
                intent.putExtra("Title", mBookAdapter.getCurrentBook(position).getmTitle());
                intent.putExtra("Date", mBookAdapter.getCurrentBook(position).getmDate());
                startActivity(intent);
            }
        });
    }

    private void addNewBook (String userId, String author, String title, String date) {
        Book book = new Book (author, title, date);

        mFirebaseDatabaseReference.child("read").child(userId).child(title).setValue(book);
    }

    private void createView () {
        mBookAdapter = new BookAdapter(this, mBooksList);
        mListView.setAdapter(mBookAdapter);
    }
}
