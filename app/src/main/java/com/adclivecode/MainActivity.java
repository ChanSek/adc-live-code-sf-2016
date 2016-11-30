package com.adclivecode;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private static final String LOG_TAG = "MainActivity";

    private TextView tvHello;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });

        tvHello = (TextView) findViewById(R.id.tv_hello);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
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
            String name = user.getDisplayName();
            if (name == null) {
                name = "unnamed user";
            }
            tvHello.setText(name);
        }
        else {
            tvHello.setText("You should log in!");
        }
    }

    private void writeUserDataToDb(FirebaseUser currentUser) {
        final AppUser user = new AppUser();
        user.uid = currentUser.getUid();
        user.name = currentUser.getDisplayName();
        final Uri photoUrl = currentUser.getPhotoUrl();
        if (photoUrl != null) {
            user.profilePicture = photoUrl.toString();
        }
        final DatabaseReference ref = db.getReference("/users/" + user.uid);
        ref.setValue(user);
    }

}
