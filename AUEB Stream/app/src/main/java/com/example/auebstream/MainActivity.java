package com.example.auebstream;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;

public class MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //let's go to the next activity
                Intent serverIntent = new Intent(MainActivity.this, ServerSet.class);
                startActivity(serverIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

}