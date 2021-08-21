package com.example.auebstream;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ManageVideo extends AppCompatActivity {

    TextView title;
    TextView hashtags;
    TextView dateCreated;
    TextView duration;
    TextView framerate;
    TextView frameWidth;
    TextView frameHeight;
    TextView uploaded;
    ImageButton playButton;
    ImageButton deleteButton;
    ImageButton backButton;

    Video video;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_video);

        title = findViewById(R.id.title);
        hashtags = findViewById(R.id.hashtags);
        dateCreated = findViewById(R.id.dateCreated);
        duration = findViewById(R.id.duration);
        framerate = findViewById(R.id.framerate);
        frameWidth = findViewById(R.id.frameWidth);
        frameHeight = findViewById(R.id.frameHeight);
        uploaded = findViewById(R.id.uploaded);
        playButton = findViewById(R.id.playButton);
        deleteButton = findViewById(R.id.deleteButton);
        intent = new Intent();

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {

                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        String videoName = extras.getString("get");

        for (Video iter : AppNode.library) {
            if (iter.videoName.equals(videoName)) {
                title.setText("Title: " + iter.videoName);
                hashtags.setText("Hashtags: " + iter.hashtagLine());
                dateCreated.setText("Date Created: " + iter.dateLine());
                duration.setText("Duration: " + iter.length + "s");
                framerate.setText("Framerate: " + iter.framerate);
                frameWidth.setText("Frame Width: " + iter.frameWidth);
                frameHeight.setText("Frame Height: " + iter.frameHeight);
                if (iter.uploaded)
                    uploaded.setText("Uploaded: Yes");
                else
                    uploaded.setText("Uploaded: No");

                video = iter;
            }
        }

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {

                //play the video
                Intent playVideoIntent = new Intent(ManageVideo.this, VideoPlayActivity.class);
                playVideoIntent.putExtra("videoUri", video.videoURI.toString());
                startActivity(playVideoIntent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlertDialog(video);
            }
        });

    }

    public void showAlertDialog(Video video) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Are you sure you want to delete " + video.videoName + "?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //delete the video
                AppNode.library.remove(video);

                ArrayList<String> topics = new ArrayList<String>();
                topics.add(AppNode.channelName);
                topics.addAll(video.associatedHashtags);

                if (video.uploaded) {
                    //delete topics in the background
                    AppNodeDelete thread = new AppNodeDelete(topics);
                    thread.start();
                }

                Toast.makeText(getApplicationContext(), "Video deleted successfully!", Toast.LENGTH_SHORT).show();

                //true means reload the page Manage.class since a video got deleted
                intent.putExtra("receive", true);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert.create().show();
    }
}