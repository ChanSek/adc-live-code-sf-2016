package com.adclivecode;

import android.app.Service;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String LOG_TAG = "MyFirebaseIDService";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(LOG_TAG, token);

        sendTokenToServer(token);

    }

    private void sendTokenToServer(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            HashMap<String, Object> childUpdate = new HashMap<>();
            childUpdate.put("/fcmToken", token);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/users/" + user
                    .getUid());

            ref.updateChildren(childUpdate);
        }
    }
}
