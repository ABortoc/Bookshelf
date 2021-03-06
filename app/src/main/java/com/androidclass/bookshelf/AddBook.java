package com.androidclass.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AddBook extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private EditText mAuthor;
    private EditText mTitle;
    private EditText mDate;
    private Button mAdd;
    private Button mScan;
    private Button mScanIsbn;
    private String mTable;
    private Uri mPhotoURI;
    private String thumbnailURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        getTable();
        Log.d("AddBook", "table: " + mTable);

        mAuthor = (EditText) findViewById(R.id.edittext_author);
        mTitle = (EditText) findViewById(R.id.edittext_title);
        mDate = (EditText) findViewById(R.id.edittext_date);
        mAdd = (Button) findViewById(R.id.button_add);
        mScan = (Button) findViewById(R.id.button_scan);
        mScanIsbn = (Button) findViewById(R.id.button_scanIsbn);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewBook(mTable, mFirebaseUser.getUid(), mAuthor.getText().toString(), mTitle.getText().toString(),
                        mDate.getText().toString());
            }
        });

        mScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        mScanIsbn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isbnScan();
            }
        });
    }

    //Called from onCreate to set the mTable variable to either "read" or "toread". This is done to allow
    //one to reuse this activity for both Read and Toread activities. Read and Toread activities put extra
    //string that signals which table is being used in their intent when launching this activity.
    private void getTable() {
        Intent intent = getIntent();
        if(intent.hasExtra("Table")){
            mTable = intent.getStringExtra("Table");
        }
    }

    //Called from onCreate on button click from mAdd button to add the book to the appropriate table in the
    //database. Uses a Book class constructor that takes only 3 arguments to create an object that can be
    //added to the database. Launches either Read or Toread activity based on the mTable variable.
    private void addNewBook(String table, String userId, String author, String title, String date) {
        Book book = new Book (author, title, date);

        mFirebaseDatabaseReference.child(table).child(userId).child(title).setValue(book);

        if(mTable.equals("read")) {
            Intent intent = new Intent(AddBook.this, Read.class);
            startActivity(intent);
            finish();
        }
        else if(mTable.equals("toread")) {
            Intent intent = new Intent(AddBook.this, ToRead.class);
            startActivity(intent);
            finish();
        }
    }

    //Called from getBookInfo to add the book to the appropriate table in the
    //database. Uses a Book class constructor that takes 4 arguments to create an object that can be
    //added to the database. This add function adds a book to database from a photo of an isbn
    //barcode, and it also adds a url for the cover image of the book. Launches either Read or Toread
    //activity based on the mTable variable.
    private void addNewBookFromPhoto(String table, String userId, String author, String title,
                                      String date, String coverUrl) {
        Book book = new Book (author, title, date, coverUrl, true);
        mFirebaseDatabaseReference.child(table).child(userId).child(title).setValue(book);

        if(mTable.equals("read")) {
            Intent intent = new Intent(AddBook.this, Read.class);
            startActivity(intent);
            finish();
        }
        else if(mTable.equals("toread")) {
            Intent intent = new Intent(AddBook.this, ToRead.class);
            startActivity(intent);
            finish();
        }
    }

    //Taken from android official documentation guide "Take Photos". Launches camera and saves the
    //photo when it is taken. Called when a user clicks mScan button ("Take a Photo" inside the app).
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("AddBook", "dispatchTakePictureIntent exception: " + ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mPhotoURI = FileProvider.getUriForFile(this,
                        "com.androidclass.bookshelf",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //Taken from android official documentation guide "Take Photos". Creates a file for the photo
    //that will be taken. Called from dispatchTakePictureIntent(). Returns a File type.
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    //Pieces of code were taken from android official documentation guide "Scan Barcodes with ML Kit on
    //Android", modified and put together to make this function. Called when the user clicks mIsbnScan
    //button ("Add From Photo" inside the app). Launches getBookInfo() with ISBN string argument.
    private void isbnScan() {
        FirebaseVisionImage firebaseVisionImage;
        try {
            firebaseVisionImage = FirebaseVisionImage.fromFilePath(this, mPhotoURI);
            Log.d("AddBook", "Isbn scan got an image");
        }
        catch(IOException e) {
            e.printStackTrace();
            Log.d("AddBook", "Isbn scan did not get an image");
            return;
        }
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector();
        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        for (FirebaseVisionBarcode barcode: barcodes) {
                            String isbn = barcode.getRawValue();
                            getBookInfo(isbn);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("AddBook", "Failed to detect anything");
                    }
                });
    }

    //Called from isbnScan() with the ISBN string that resulted from the scan. Uses volley library to
    //access Google Books API via an http request, using the isbn number from the argument. Iterates
    //through the JSON objects received from the Books API to find and set author, title, release
    //date and book cover image url. Launches addNewBookFromPhoto() with the arguments of the aforementioned
    //fields.
    private void getBookInfo(String isbn) {
        String api_key = getString(R.string.api_key);
        String book_url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn + "&key=" + api_key;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, book_url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {
                        try {
                            JSONArray jsonArray = result.getJSONArray("items");
                            if(!result.getString("totalItems").equals(0)) {
                                for(int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    JSONObject volumeInfo = jsonObject.getJSONObject("volumeInfo");
                                    JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                                    thumbnailURL = imageLinks.getString("thumbnail");
                                    JSONArray authors = volumeInfo.getJSONArray("authors");
                                    String author = authors.getString(0);
                                    String title = volumeInfo.getString("title");
                                    String date = volumeInfo.getString("publishedDate");
                                    Log.d("AddBook:", "getBookInfo authors: " + author + " " + title + " " + date);
                                    addNewBookFromPhoto(mTable, mFirebaseUser.getUid(), author, title,
                                            date, thumbnailURL);
                                }
                            }
                            else {
                                Log.d("AddBook", "getBookInfo: didn't get any items");
                            }
                            Log.d("AddBook", "getBookInfo jsonData: " + thumbnailURL);
                        } catch (JSONException e) {
                            Toast.makeText(AddBook.this, "No book information was found.", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                            Log.e("AddBook:", "JSON exeception: " + e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("AddBook", "getBookInfo error:" + error);
                    }
                });

        AppController.getInstance().addToRequestQueue(request);
    }
}
