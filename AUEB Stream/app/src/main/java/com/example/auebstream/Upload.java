package com.example.auebstream;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Upload extends AppCompatActivity {

    ImageButton backButton;
    Button uploadButton;
    LinearLayout linearLayout;
    ListView listView;
    TextView textView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        backButton = findViewById(R.id.backButton);
        uploadButton = findViewById(R.id.uploadButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //go to record
                Intent recordIntent = new Intent(Upload.this, Record.class);
                recordIntent.putExtra("get", true);
                startActivity(recordIntent);
            }
        });

        linearLayout = findViewById(R.id.linearLayout);
        listView = findViewById(R.id.listView);
        textView = findViewById(R.id.textView2);
        imageView = findViewById(R.id.imageView);

        linearLayout.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);

        //making listView
        //first row is videoName
        //second row is hashtags
        HashMap<String, String> videos = new HashMap<>();
        for (Video iterator: AppNode.library) {
            if (!iterator.uploaded)
                videos.put(iterator.videoName, iterator.hashtagLine());
        }

        if (videos.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
        else {
            linearLayout.setVisibility(View.VISIBLE);
            List<HashMap<String, String>> listItems = new ArrayList<>();
            SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.list_item,
                    new String[]{"First Line", "Second Line"},
                    new int[]{R.id.text1, R.id.text2});

            Iterator it = videos.entrySet().iterator();
            while (it.hasNext()) {
                HashMap<String, String> resultsMap = new HashMap<>();
                Map.Entry pair = (Map.Entry) it.next();
                resultsMap.put("First Line", pair.getKey().toString());
                resultsMap.put("Second Line", pair.getValue().toString());
                listItems.add(resultsMap);
            }
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //getting collection of videoNames
                    Collection<String> keys = videos.keySet();
                    //creating an ArrayList of videoNames
                    ArrayList<String> names = new ArrayList(keys);
                    String videoName = names.get(position);
                    showAlertDialog(videoName);
                }
            });
        }
    }

    public void showAlertDialog(String video) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Are you sure you want to upload " + video + "?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (Video iter : AppNode.library) {
                    if (iter.videoName.equals(video)) {
                        //uploading the video
                        iter.uploaded = true;
                        //upload the video in the background
                        AppNodeUpload thread = new AppNodeUpload(iter);
                        thread.start();
                        Toast.makeText(getApplicationContext(), "Video uploaded successfully!", Toast.LENGTH_SHORT).show();
                        //reload page
                        Intent uploadIntent = new Intent(Upload.this, Upload.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(uploadIntent);
                        //no animation
                        overridePendingTransition(0, 0);
                        finish();
                    }
                }
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