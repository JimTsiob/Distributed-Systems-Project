package com.example.auebstream;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class Subscribe extends AppCompatActivity {

    String topic;

    ImageButton backButton;
    RadioButton channelNameButton;
    RadioButton hashtagButton;
    ImageButton subscribeButton;
    TextView errorMessage1;
    TextView errorMessage2;
    EditText topicInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        backButton = findViewById(R.id.backButton);
        channelNameButton = findViewById(R.id.channelNameButton);
        hashtagButton = findViewById(R.id.hashtagButton);
        subscribeButton = findViewById(R.id.subscribeButton);
        errorMessage1 = findViewById(R.id.errorMessage1);
        errorMessage1.setVisibility(View.INVISIBLE);
        errorMessage2 = findViewById(R.id.errorMessage2);
        errorMessage2.setVisibility(View.INVISIBLE);
        topicInput = findViewById(R.id.topicInput);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        topicInput.setEnabled(false);
        channelNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                topicInput.setFilters(new InputFilter[] { channelNameFilter });
                topicInput.setText("");
                topicInput.setHint("Tsiobieman");
                channelNameButton.setClickable(false);
                hashtagButton.setClickable(true);
                topicInput.setEnabled(true);
            }
        });

        hashtagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                topicInput.setFilters(new InputFilter[] { hashtagFilter });
                topicInput.setText("");
                topicInput.setHint("#diamond_hands");
                hashtagButton.setClickable(false);
                channelNameButton.setClickable(true);
                topicInput.setEnabled(true);
            }
        });

        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                errorMessage1.setVisibility(View.INVISIBLE);
                errorMessage2.setVisibility(View.INVISIBLE);

                topic = topicInput.getText().toString();

                //checking if it is empty
                if (topic.isEmpty())
                    errorMessage2.setVisibility(View.VISIBLE);
                else {
                    if (AppNode.topicsInterested.contains(topic)) {
                        errorMessage1.setText("You are subscribed to this topic already.");
                        errorMessage1.setVisibility(View.VISIBLE);
                    }
                    else if ((channelNameButton.isClickable() && topic.charAt(0) != '#') || (topic.charAt(0) == '#' && topic.length() == 1)) {
                        errorMessage1.setText("Hashtag should start with a # and contain at least one character.");
                        errorMessage1.setVisibility(View.VISIBLE);
                    }
                    else {
                        //subscribe to the topic in the background
                        AppNodeStream thread = new AppNodeStream(topic, getApplicationContext());
                        thread.start();
                        Toast.makeText(getApplicationContext(), "Subscribe Request Successful", Toast.LENGTH_SHORT).show();

                        finish();
                    }
                }
            }
        });
    }

    //to allow specific characters
    //filter for channelName keyboard
    InputFilter channelNameFilter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                        Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!(Character.isLetterOrDigit(source.charAt(i)) && source.toString().matches("[a-zA-Z ]+")) &&
                        !Character.toString(source.charAt(i)).equals("_"))
                {
                    return "";
                }
            }
            return null;
        }
    };

    //filter for hashtag keyboard
    InputFilter hashtagFilter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    if (!(Character.isLowerCase(source.charAt(i)) && source.toString().matches("[a-zA-Z ]+")) &&
                            !Character.toString(source.charAt(i)).equals("_") &&
                            !Character.toString(source.charAt(i)).equals("#")) {
                        return "";
                    }
                }
            }
            return null;
        }
    };
}