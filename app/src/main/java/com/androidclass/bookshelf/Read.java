package com.androidclass.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

//Activity that displays objects from the Read table of the database in a ListView.
public class Read extends AppCompatActivity {

    private ListView mListView;
    private BookAdapter mBookAdapter;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private ArrayList<Book> mBooksList = new ArrayList<>();
    private FloatingActionButton mAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //Extracts objects from the Read table from the database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("read/"+mFirebaseUser.getUid()+"/");
        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot bookSnapshot: dataSnapshot.getChildren()) {
                    Book book = bookSnapshot.getValue(Book.class);
                    Log.d("Read", "Books: " + book.getmTitle());
                    Log.d("Read", "Books: " + book.getmAuthor());
                    Log.d("Read", "Books: " + book.getmDate());
                    Log.d("Read", "Books URL: " + book.getmCoverUrl());
                    mBooksList.add(new Book(book.getmAuthor(), book.getmTitle(), book.getmDate(),
                            book.getmCoverUrl(), true));
                    createView();
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

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Read.this, SelectedBook.class);
                intent.putExtra("Author", mBookAdapter.getCurrentBook(position).getmAuthor());
                intent.putExtra("Title", mBookAdapter.getCurrentBook(position).getmTitle());
                intent.putExtra("Date", mBookAdapter.getCurrentBook(position).getmDate());
                intent.putExtra("CoverURL", mBookAdapter.getCurrentBook(position).getmCoverUrl());
                intent.putExtra("Table", "read");
                startActivity(intent);
            }
        });

        mAdd = (FloatingActionButton) findViewById(R.id.floatingActionButton_add);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                Intent intent = new Intent(Read.this, AddBook.class);
                intent.putExtra("Table","read");
                startActivity(intent);
                finish();
            }
        });
    }

    //Called from onCreate(). Sets up a ListView using BookAdapter adapter class.
    private void createView () {
        mBookAdapter = new BookAdapter(this, mBooksList);
        mListView.setAdapter(mBookAdapter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        super.recreate();
    }
}
