package com.example.auebstream;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Manage extends AppCompatActivity {

    ImageButton backButton;
    LinearLayout linearLayout;
    ListView listView;
    TextView textView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
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

                    //go to manage video
                    Intent viewIntent = new Intent(Manage.this, ManageVideo.class);
                    viewIntent.putExtra("get", videoName);
                    startActivityForResult(viewIntent, 1);

                }
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Boolean bruh = data.getBooleanExtra("receive", false);

                if (bruh) {
                    //reload the page since a video got deleted
                    Intent manageIntent = new Intent(Manage.this, Manage.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(manageIntent);
                    //no animation
                    overridePendingTransition(0, 0);
                    finish();
                }
            }
        }
    }
}