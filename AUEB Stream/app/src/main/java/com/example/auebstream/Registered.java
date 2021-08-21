package com.example.auebstream;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public class Registered extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3500;
    TextView textView;
    ImageView tick;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered);

        //the good stuff
        AppNodeListening thread = new AppNodeListening(getApplicationContext());
        thread.start();

        textView = findViewById((R.id.textView1));
        textView.setText(AppNode.channelName);
        tick = findViewById(R.id.tick);
        Drawable drawable = tick.getDrawable();
        if (drawable instanceof  AnimatedVectorDrawableCompat) {
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
        }
        else if (drawable instanceof  AnimatedVectorDrawable) {
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //let's go to the next activity
                Intent menuIntent = new Intent(Registered.this, Menu.class);
                startActivity(menuIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}