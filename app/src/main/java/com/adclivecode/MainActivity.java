package com.adclivecode;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private static final String LOG_TAG = "MainActivity";

    private TextView tvHello;
    private RecyclerView rv;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseRecyclerAdapter<PictureMetadata, PictureViewHolder> adapter;

    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            updateViews();
            if (firebaseAuth.getCurrentUser() != null) {
                writeUserDataToDb(firebaseAuth.getCurrentUser());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    private void initViews() {
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TakePhotoActivity.class));
            }
        });

        tvHello = (TextView) findViewById(R.id.tv_hello);

        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authStateListener);
        stopSynchronizingPictures();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_login:
                Log.d(LOG_TAG, "login");
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                        .build(),
                    RC_SIGN_IN);
                return true;
            case R.id.action_logout:
                auth.signOut();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateViews() {
        final FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            tvHello.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            startSynchronizingPictures();
        }
        else {
            tvHello.setVisibility(View.VISIBLE);
            tvHello.setText("You should log in!");
            rv.setVisibility(View.GONE);
            stopSynchronizingPictures();
        }
    }

    private void startSynchronizingPictures() {
        final DatabaseReference pictures = db.getReference("/pictures");
        final Query query = pictures.orderByChild("timestamp");
        adapter = new FirebaseRecyclerAdapter<PictureMetadata, PictureViewHolder>(PictureMetadata.class, android.R.layout.simple_list_item_2, PictureViewHolder.class, query) {
            @Override
            protected void populateViewHolder(PictureViewHolder viewHolder, PictureMetadata metadata, int position) {
                viewHolder.setPicture(metadata);
            }
        };
        rv.setAdapter(adapter);
    }

    private void stopSynchronizingPictures() {
        if (adapter != null) {
            adapter.cleanup();
            adapter = null;
        }
    }

    private void writeUserDataToDb(FirebaseUser currentUser) {
        final AppUser user = new AppUser();
        user.uid = currentUser.getUid();
        user.name = currentUser.getDisplayName();
        user.fcmToken = FirebaseInstanceId.getInstance().getToken();
        final Uri photoUrl = currentUser.getPhotoUrl();
        if (photoUrl != null) {
            user.profilePicture = photoUrl.toString();
        }
        final DatabaseReference ref = db.getReference("/users/" + user.uid);
        ref.setValue(user);
    }

}
