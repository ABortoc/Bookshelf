package com.androidclass.bookshelf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class SelectedBook extends AppCompatActivity {

    private String mAuthor;
    private String mTitle;
    private String mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_book);

        getBookInfo();

        ImageView imageView = (ImageView) findViewById(R.id.imageView_cover_selected);
        setBookInfo(mAuthor, mTitle, mDate);

        //EditText description = (EditText) findViewById(R.id.text_description);
        TextView description = (TextView) findViewById(R.id.description_view);

        final Button editButton = (Button) findViewById(R.id.button_edit_submit);
        editButton.setTag(1);
        editButton.setText("Edit");
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int status = (Integer) v.getTag();
                if (status == 1) {
                    //TextViewClicked();
                    editButton.setText("Submit");
                    v.setTag(0);
                }
                else {
                    editButton.setText("Edit");
                    v.setTag(1);
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
        }
    }

    protected void setBookInfo (String mAuthor, String mTitle, String mDate) {
        TextView author = (TextView) findViewById(R.id.textView_author_selected);
        author.setText (mAuthor);

        TextView title = (TextView) findViewById(R.id.textView_title_selected);
        title.setText (mTitle);

        TextView date = (TextView) findViewById(R.id.textView_date_selected);
        date.setText (mDate);
    }

/*    public void TextViewClicked() {
        ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.edit_text_switcher);
        viewSwitcher.showNext();
        TextView textView = (TextView) viewSwitcher.findViewById(R.id.clickable_text_view);
        textView.setText("Hmmm");
    }*/
}
