package com.example.auebstream;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Record extends AppCompatActivity {

    String title;
    String hashtags;
    boolean emptyTitle;
    boolean emptyHashtags;
    boolean errorTitle;
    boolean errorHashtags;
    Video newVideo;

    ImageButton backButton;
    EditText titleInput;
    EditText hashtagInput;
    ImageButton recordButton;
    TextView errorMessage1;
    TextView errorMessage2;
    TextView errorMessage3;

    private Uri videoUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        newVideo  = new Video();
        backButton = findViewById(R.id.backButton);
        titleInput = findViewById(R.id.titleInput);
        hashtagInput = findViewById(R.id.hashtagInput);
        recordButton = findViewById(R.id.recordButton);
        errorMessage1 = findViewById(R.id.errorMessage1);
        errorMessage1.setVisibility(View.INVISIBLE);
        errorMessage2 = findViewById(R.id.errorMessage2);
        errorMessage2.setVisibility(View.INVISIBLE);
        errorMessage3 = findViewById(R.id.errorMessage3);
        errorMessage3.setVisibility(View.INVISIBLE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                errorMessage1.setVisibility(View.INVISIBLE);
                errorMessage2.setVisibility(View.INVISIBLE);
                errorMessage3.setVisibility(View.INVISIBLE);

                errorTitle = false;
                errorHashtags = false;

                title = titleInput.getText().toString();
                hashtags = hashtagInput.getText().toString();

                //checking if any of them are empty
                if (title.isEmpty())
                    emptyTitle = true;
                else
                    emptyTitle = false;

                if (hashtags.isEmpty())
                    emptyHashtags = true;
                else
                    emptyHashtags = false;

                if (emptyTitle && emptyHashtags)
                    errorMessage3.setVisibility(View.VISIBLE);
                else if (emptyTitle) {
                    errorMessage1.setText("You can't use a blank title.");
                    errorMessage1.setVisibility(View.VISIBLE);
                } else if (emptyHashtags) {
                    errorMessage2.setText("You didn't choose any hashtags.\n");
                    errorMessage2.setVisibility(View.VISIBLE);
                }

                if (!emptyTitle) {
                    //checking if a video with this name exists already
                    for (Video videoTest : AppNode.library) {
                        if (videoTest.videoName.equals(title)) {
                            //not empty title, but wrong title
                            errorMessage1.setText("This title is not available.");
                            errorMessage1.setVisibility(View.VISIBLE);
                            errorTitle = true;
                        }
                    }
                }
                ArrayList<String> videoHashtags = new ArrayList<String>();

                if (!emptyHashtags) {
                    //modifying hashtags to be accurate
                    int count = 0;

                    //removing gaps
                    hashtags = hashtags.replaceAll(" ", "");

                    //counts hashtags
                    for (int i = 0; i < hashtags.length(); i++) {
                        if (hashtags.charAt(i) == '#') {
                            count++;
                        }
                    }
                    if (count == 1) {
                        videoHashtags.add(hashtags.substring(hashtags.indexOf("#"), hashtags.length()));
                    } else if (count > 1) {
                        //index of the next hashtag's start
                        int nexthash = hashtags.indexOf("#", hashtags.indexOf("#") + 1);
                        //adds first hashtag
                        videoHashtags.add(hashtags.substring(hashtags.indexOf("#"), nexthash));
                        //this for loop adds every hasthtag except the first one and the last one
                        for (int i = 0; i < count - 2; i++) {
                            hashtags = hashtags.substring(nexthash, hashtags.length());
                            nexthash = hashtags.indexOf("#", hashtags.indexOf("#") + 1);
                            videoHashtags.add(hashtags.substring(0, nexthash));
                        }
                        hashtags = hashtags.substring(nexthash, hashtags.length());
                        //adds last hashtag
                        videoHashtags.add(hashtags);
                    }

                    //removing blank hashtags and duplicates
                    int i;
                    String temp;
                    for (int k = 0; k < videoHashtags.size(); k++) {
                        temp = videoHashtags.get(k);
                        i = 0;
                        if (temp.equals("#")) {
                            videoHashtags.remove(temp);
                            k--;
                        } else {
                            for (String iter : videoHashtags) {
                                if (temp.equals(iter))
                                    i++;
                            }
                            if (i > 1) {
                                k--;
                                videoHashtags.remove(temp);
                            }
                        }
                    }

                    if (videoHashtags.isEmpty()) {
                        //not empty hashtags, but wrong hashtags
                        errorMessage2.setText("Each hashtag should start with a # and contain at least one character.");
                        errorMessage2.setVisibility(View.VISIBLE);
                        errorHashtags = true;
                    }
                }

                //if nothing went wrong
                if (!(emptyTitle || emptyHashtags || errorTitle || errorHashtags)) {

                    newVideo.videoName = title;
                    newVideo.uploaded = false;
                    newVideo.associatedHashtags = videoHashtags;

                    Intent recordIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    startActivityForResult(recordIntent, 101);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {

            //getting video properties
            newVideo.videoURI = data.getData();

            File file = new File(getFilePathFromContentUri(newVideo.videoURI, getContentResolver()));
            newVideo.bytes = new byte[(int) file.length()];

            try {

                FileInputStream fis = null;
                try {

                    fis = new FileInputStream(file);
                    //read file into bytes[]
                    fis.read(newVideo.bytes);
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            newVideo.chunks = newVideo.bytes.length / AppNode.chunk_size;
            newVideo.remainder = newVideo.bytes.length % AppNode.chunk_size;

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplicationContext(), newVideo.videoURI);

            newVideo.dateCreated = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE).substring(0, 8);
            newVideo.length = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
            if (newVideo.length == 0)
                newVideo.framerate = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));
            else
                newVideo.framerate = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)) / newVideo.length;
            newVideo.frameWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            newVideo.frameHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            retriever.release();

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                Boolean value = extras.getBoolean("get");
                if (value) {
                    //uploading the video
                    newVideo.uploaded = true;
                    //upload the video in the background
                    AppNodeUpload thread = new AppNodeUpload(newVideo);
                    thread.start();
                    AppNode.library.add(newVideo);
                    Toast.makeText(getApplicationContext(), "Video Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            else {
                //just saving the video
                AppNode.library.add(newVideo);
                Toast.makeText(getApplicationContext(), "Video Saved Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public static String getFilePathFromContentUri(Uri contentUri, ContentResolver contentResolver) {
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = contentResolver.query(contentUri, filePathColumn, null, null, null);

        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }
}
