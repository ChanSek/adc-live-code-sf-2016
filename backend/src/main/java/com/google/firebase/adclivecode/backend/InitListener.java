package com.google.firebase.adclivecode.backend;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class InitListener implements ServletContextListener {

    public static String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    public static String FCM_KEY =
            "AAAAX-tbA-E:APA91bGreKjorJc2OlFsecyye9Xcst49vZtnGzVnLSXyBcURkyRMDuWMS9ITA-uNk0KxmGmYJxOb2Ik_xVZ6W_LEnUiq8OWq4nUjwPefeYF7xKAnGtOHm6G4oLsJFgAnKnHhCH422nAnAuEu3TeEuWdlSZBaG4vLlg";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final ServletContext context = sce.getServletContext();

        FirebaseOptions options = null;
        options = new FirebaseOptions.Builder()
                .setServiceAccount(
                        context.getResourceAsStream
                                ("/adc-live-code-sf-2016-firebase-adminsdk-z04q9-f5482604b1.json")
                ).setDatabaseUrl("https://adc-live-code-sf-2016.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/jobs");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final Iterator<DataSnapshot> jobsIterator = dataSnapshot.getChildren()
                        .iterator();

                while (jobsIterator.hasNext()) {
                    final NotificationJob job = jobsIterator.next().getValue(NotificationJob.class);

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference
                            ("/users");

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterator<DataSnapshot> usersIterator = dataSnapshot.getChildren().iterator();

                            while (usersIterator.hasNext()) {
                                AppUser user = usersIterator.next().getValue(AppUser.class);

                                sendNotificationToUser(user, job);

                            }
                            ref.child(job.uid).removeValue();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendNotificationToUser(AppUser user, NotificationJob job) {
        try {
            URL url = new URL(FCM_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setRequestProperty("Authorization", "key=" + FCM_KEY);

            JSONObject json = new JSONObject();
            json.put("to", user.fcmToken);
            JSONObject data = new JSONObject();
            data.put("title", "New picture uploaded!");
            data.put("body", "The title is: " + job.pictureTitle);
            json.put("data", data);

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(json.toString());
            writer.flush();
            connection.getInputStream();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}


class NotificationJob {

    public String pictureTitle;
    public String pictureUid;
    public String uid;

}

class AppUser {

    public String uid;
    public String name;
    public String profilePicture;
    public String fcmToken;

}
