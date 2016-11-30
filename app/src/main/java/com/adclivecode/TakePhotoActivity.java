package com.adclivecode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class TakePhotoActivity extends AppCompatActivity {

    private static final int RC_IMAGE_CAPTURE = 1;

    String currentPhotoPath;
    private ImageView ivPic;
    private boolean isPictureTaken;

    private Button uploadButton;
    private ProgressBar pb;
    private UUID uuid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();
        dispatchTakePictureIntent();
    }

    private void initViews() {
        setContentView(R.layout.activity_take_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ivPic = (ImageView) findViewById(R.id.iv_pic);
        ivPic.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                setPic();
            }
        });

        uploadButton = (Button) findViewById(R.id.button_upload);
        uploadButton.setVisibility(View.GONE);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPicture();
            }
        });

        pb = (ProgressBar) findViewById(R.id.pb);
        pb.setVisibility(View.GONE);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        uuid = UUID.randomUUID();

        String imageFileName = "JPEG_" + uuid.toString();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, RC_IMAGE_CAPTURE);
            }
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = ivPic.getWidth();
        int targetH = ivPic.getHeight();

        if (targetW == 0 || targetH == 0 || !isPictureTaken) {
            return;
        }

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        ivPic.setImageBitmap(bitmap);

        uploadButton.setVisibility(View.VISIBLE);
        pb.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
            isPictureTaken = true;
        }
    }

    private void uploadPicture() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference reference = storage.getReference("/pictures/" + uuid.toString() + ".jpg");
        final UploadTask uploadTask = reference.putFile(Uri.fromFile(new File(currentPhotoPath)));
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                throw new RuntimeException(e);
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                finish();
            }
        });
        uploadTask.addOnProgressListener(this, new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                pb.setProgress((int) taskSnapshot.getBytesTransferred());
                pb.setMax((int) taskSnapshot.getTotalByteCount());
            }
        });
    }

}
