package com.example.auebstream;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class Stream extends AppCompatActivity {

    ImageButton backButton;
    ImageButton subscribeButton;
    ImageButton streamButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        subscribeButton = findViewById(R.id.subscribeButton);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //go to subscribe
                Intent subscribeIntent = new Intent(Stream.this, Subscribe.class);
                startActivity(subscribeIntent);
            }
        });

        streamButton = findViewById(R.id.streamButton);
        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //go to play
                Intent playIntent = new Intent(Stream.this, Play.class);
                startActivity(playIntent);
            }
        });
    }
}