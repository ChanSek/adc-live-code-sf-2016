package com.adclivecode;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

public class PictureViewHolder extends RecyclerView.ViewHolder {

    private TextView line1;
    private TextView line2;


    public PictureViewHolder(View itemView) {
        super(itemView);
        line1 = (TextView) itemView.findViewById(android.R.id.text1);
        line2 = (TextView) itemView.findViewById(android.R.id.text2);
    }

    public void setPicture(final PictureMetadata metadata) {
        line1.setText(metadata.title);
        final String date = DateUtils.formatDateTime(itemView.getContext(), metadata.timestamp, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        line2.setText(date);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ViewPictureActivity.class);
                intent.putExtra(ViewPictureActivity.EXTRA_METADATA, metadata);
                view.getContext().startActivity(intent);
            }
        });
    }

}
