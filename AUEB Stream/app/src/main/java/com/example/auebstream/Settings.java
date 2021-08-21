package com.example.auebstream;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class Settings extends AppCompatActivity {

    ImageButton backButton;
    ImageView profile;
    TextView name;
    TextView server;
    TextView phone;
    TextView port;
    ImageButton icon[] = new ImageButton[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        backButton = findViewById(R.id.backButton);
        profile = findViewById((R.id.imageView2));

        //set the updated image on profile
        String file = "icon" + AppNode.pic;
        int resID = getResources().getIdentifier(file, "drawable", getPackageName());
        profile.setImageResource(resID);

        name = findViewById(R.id.name);
        server = findViewById(R.id.server);
        phone = findViewById(R.id.phone);
        port = findViewById(R.id.port);

        //set text
        name.setText(AppNode.channelName);
        server.setText("Server IP Address: " + AppNode.serverIP);
        phone.setText("My IP Address: " + AppNode.IP);
        port.setText("My Port: " + String.valueOf(AppNode.port));

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        //for every button
        for (int i = 0; i < 8; i++) {

            file = "icon" + i;
            resID = getResources().getIdentifier(file, "id", getPackageName());
            icon[i] = findViewById(resID);

            int finalI = i;

            icon[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    //change the pic
                    AppNode.pic = String.valueOf(finalI);
                    String file = "icon" + finalI;
                    int resID = getResources().getIdentifier(file, "drawable", getPackageName());
                    profile.setImageResource(resID);
                }

            });
        }

    }

}