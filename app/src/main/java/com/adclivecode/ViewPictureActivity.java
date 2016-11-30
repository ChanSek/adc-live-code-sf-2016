package com.adclivecode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.Serializable;

public class ViewPictureActivity extends AppCompatActivity {

    public static final String EXTRA_METADATA = "meta";
    private PictureMetadata meta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        meta = (PictureMetadata) intent.getSerializableExtra(EXTRA_METADATA);

        initViews();
    }

    private void initViews() {
        setContentView(R.layout.activity_view_picture);
        ImageView ivPic = (ImageView) findViewById(R.id.iv_pic);
        Glide.with(this).load(meta.downloadUrl).into(ivPic);
    }

}
