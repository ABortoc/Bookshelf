package com.androidclass.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

//Reused by both Read and Toread activity. mTable variable controls which table is being used. It is
//passed from the activity that launched this activity, either Read or Toread.
public class SelectedBook extends AppCompatActivity {

    private String mAuthor;
    private String mTitle;
    private String mDate;
    private String mDesc;
    private String mCoverURL;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private Button mRemove;
    private Button mCalendar;
    private EditText mDescription_edit;
    private TextView mDescription;
    private String mTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_book);

        getBookInfo();
        setBookInfo(mAuthor, mTitle, mDate);

        ImageView imageView = (ImageView) findViewById(R.id.imageView_cover_selected);
        mCalendar = (Button) findViewById(R.id.button_calendar);
        if(mTable.equals("read")) {
            mCalendar.setVisibility(View.INVISIBLE);
        }
        else if(mTable.equals("toread")) {
            mCalendar.setVisibility(View.VISIBLE);
        }
        mCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openApp(SelectedBook.this, "com.google.android.calendar");
            }
        });

        Glide.with(this).load(mCoverURL).placeholder(R.drawable.ic_book_placeholder)
                .into(imageView);
        mDescription_edit = (EditText) findViewById(R.id.description_view_edit);
        mDescription = (TextView) findViewById(R.id.description_view);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //Extracts book info from the database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference(mTable+"/"+mFirebaseUser.getUid()+"/"+mTitle+"/");
        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Book book = dataSnapshot.getValue(Book.class);
                mDesc = book.getmDescription();
                mDescription.setText(mDesc);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("SelectedBook", "The read failed: " + databaseError.getCode());
            }
        });

        //editButton changes text every time it is clicked. Makes either EditText or TextView visible,
        //and calls updateBookInfo() when user clicks editButton ("Submit" inside the app).
        final Button editButton = (Button) findViewById(R.id.button_edit_submit);
        editButton.setTag(1);
        editButton.setText(getString(R.string.button_edit));
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int status = (Integer) v.getTag();
                if (status == 1) {
                    editButton.setText(getString(R.string.button_submit));
                    v.setTag(0);
                    mDescription_edit.setVisibility(View.VISIBLE);
                    mDescription.setVisibility(View.INVISIBLE);
                }
                else {
                    editButton.setText(getString(R.string.button_edit));
                    v.setTag(1);
                    mDescription_edit.setVisibility(View.INVISIBLE);
                    mDescription.setVisibility(View.VISIBLE);
                    mDescription.setText(mDescription_edit.getText().toString());
                    updateBookInfo();
                }
            }
        });

        mRemove = (Button) findViewById(R.id.button_remove);
        mRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                mFirebaseDatabaseReference.child(mTable).child(mFirebaseUser.getUid()).child(mTitle).removeValue();
                if(mTable.equals("read")) {
                    Intent intent = new Intent(SelectedBook.this, Read.class);
                    startActivity(intent);
                }
                else if(mTable.equals("toread")) {
                    Intent intent = new Intent(SelectedBook.this, ToRead.class);
                    startActivity(intent);
                }
            }
        });
    }

    protected void getBookInfo () {
        Intent intent = getIntent();
        if (intent.hasExtra("Author")) {
            mAuthor = intent.getStringExtra("Author");
            mTitle = intent.getStringExtra("Title");
            mDate = intent.getStringExtra("Date");
            mCoverURL = intent.getStringExtra("CoverURL");
            mTable = intent.getStringExtra("Table");
        }
    }

    //Called from onCreate to set author, title and date variables to the values passed from the
    //activity that launched this activity, either Read or Toread.
    protected void setBookInfo (String mAuthor, String mTitle, String mDate) {
        TextView author = (TextView) findViewById(R.id.textView_author_selected);
        author.setText (mAuthor);

        TextView title = (TextView) findViewById(R.id.textView_title_selected);
        title.setText (mTitle);

        TextView date = (TextView) findViewById(R.id.textView_date_selected);
        date.setText (mDate);
    }

    //Called when user clicks editButton ("Submit" inside the app). Updates database entry with the
    //description text that the user provides in the EditText.
    protected void updateBookInfo() {
        String description = mDescription.getText().toString();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(mTable)
                .child(mFirebaseUser.getUid()).child(mTitle);
        Map<String, Object> updates = new HashMap<>();
        updates.put("mDescription", description);
        reference.updateChildren(updates);
    }

    //Called when mCalendar button ("Calendar" inside the app) is pressed. Launches Google Calendar.
    //Takes context and package name arguments (Context and String type respectively).
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                Toast.makeText(context, "Requires Google Calendar to be installed.", Toast.LENGTH_LONG).show();
                return false;
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
