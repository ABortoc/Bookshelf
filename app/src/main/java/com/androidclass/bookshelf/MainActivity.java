package com.androidclass.bookshelf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

//Most of the code is just standard FireBase setup that is taken from the FireBase documentation
public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private Button mRead, mToRead, mLogOutButton;

    private static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private String mUsername;
    private String mPhotoUrl;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabase;

    private ConstraintLayout mMainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Layout variable is set to handle background image loading with Glide library
        mMainLayout = (ConstraintLayout) findViewById(R.id.main_layout);
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if(mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignIn.class));
            finish();
            return;
        }
        else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        writeNewUser(mFirebaseUser.getUid(), mUsername);


        mRead = (Button) findViewById(R.id.read);
        mToRead = (Button) findViewById(R.id.toread);
        mLogOutButton = (Button) findViewById(R.id.logout);

        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
        else {
            initialize();
        }

        //Sets background image from a drawable source
        Glide.with(this).load(R.drawable.main_background).into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                mMainLayout.setBackground(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    //Called from onCreate. On fresh install, when permissions have not been granted yet, this function
    //will track whether or not the user agreed to grant them. If the user declines, buttons will not
    //function, otherwise the app will be initialized and become functional.
    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions,
                                           @androidx.annotation.NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");

        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if(grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    //run initialize function
                    initialize();
                    }
                }
            }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    //Called from onRequestPermissionsResult() or onCreate(). Activates on click listeners to make buttons work.
    //Only called if permissions are granted to the application.
    public void initialize() {
        mRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Read.class);
                startActivity(intent);
            }
        });

        mToRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ToRead.class);
                startActivity(intent);
            }
        });

        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
    }

    @IgnoreExtraProperties
    public class User {

        public String username;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username) {
            this.username = username;
        }

    }

    private void writeNewUser(String userId, String name) {
        User user = new User(name);

        mDatabase.child("users").child(userId).setValue(user);
    }

    public void logOut() {
        mFirebaseAuth.signOut();
        finish();
        Intent intent = new Intent(MainActivity.this, SignIn.class);
        startActivity(intent);
    }
}
