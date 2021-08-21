package com.example.auebstream;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Play extends AppCompatActivity {

    ImageButton backButton;
    ListView listView;
    ImageButton reloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        reloadButton = findViewById(R.id.reloadButton);

        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //reload page
                Intent uploadIntent = new Intent(Play.this, Play.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(uploadIntent);
                //no animation
                overridePendingTransition(0, 0);
                finish();
            }
        });

        listView = findViewById(R.id.listView);

        //making listView
        //first row is videoName
        //second row is channelName
        HashMap<String, String> videos = new HashMap<>();
        for (StreamingVideo iterator: AppNode.streamingLibrary)
            videos.put(iterator.videoName, "by " + iterator.channelName);

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

                for (StreamingVideo iter : AppNode.streamingLibrary) {
                    if (iter.videoName.equals(videoName)) {

                        //play the video
                        Intent playVideoIntent = new Intent(Play.this, VideoPlayActivity.class);
                        playVideoIntent.putExtra("videoUri", iter.videoURI.toString());
                        startActivity(playVideoIntent);
                    }
                }
            }
        });
    }

}