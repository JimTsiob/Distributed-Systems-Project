package com.example.auebstream;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class Menu extends AppCompatActivity {

    ImageButton settingsButton;
    Button recordButton;
    Button uploadButton;
    Button streamButton;
    Button manageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        
        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //go to settings
                Intent settingsIntent = new Intent(Menu.this, Settings.class);
                startActivity(settingsIntent);
            }

        });

        recordButton = findViewById((R.id.recordButton));
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //go to record
                Intent recordIntent = new Intent(Menu.this, Record.class);
                startActivity(recordIntent);
            }

        });

        uploadButton = findViewById((R.id.uploadButton));
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //go to upload
                Intent uploadIntent = new Intent(Menu.this, Upload.class);
                startActivity(uploadIntent);
            }

        });

        streamButton = findViewById(R.id.streamButton);
        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if we haven't subscribed to any topics, go subscribe!
                if (AppNode.streamingLibrary.isEmpty()) {

                    //go to subscribe
                    Intent subscribeIntent = new Intent(Menu.this, Subscribe.class);
                    startActivity(subscribeIntent);
                }
                else {

                    //go to stream
                    Intent streamIntent = new Intent(Menu.this, Stream.class);
                    startActivity(streamIntent);
                }
            }

        });

        manageButton = findViewById(R.id.manageButton);
        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //go to manage
                Intent manageIntent = new Intent(Menu.this, Manage.class);
                startActivity(manageIntent);
            }

        });
    }

    @Override
    protected void onRestart() {

        //get the icon from before
        String file = "icon" + AppNode.pic;
        int resID = getResources().getIdentifier(file, "drawable", getPackageName());
        settingsButton.setBackgroundResource(resID);

        super.onRestart();
    }
}