package com.example.auebstream;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class NameSet extends AppCompatActivity{

    String name;
    String serverIP = AppNode.serverIP;
    EditText nameInput;
    Button submitButton;
    TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_set);

        nameInput = findViewById(R.id.nameInput);
        errorMessage = findViewById(R.id.errorMessage);
        errorMessage.setVisibility(View.INVISIBLE);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean flag = true;
                try {
                    name = nameInput.getText().toString();
                    submitButton.setEnabled(false);

                    //starting connection
                    Socket socket = new Socket(serverIP, 10000);

                    OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                    BufferedWriter bw = new BufferedWriter(osw);

                    //now the broker will initialize our port and will check if our name is available
                    //stands for initialize me
                    bw.write("im");
                    bw.newLine();
                    bw.flush();
                    bw.write(name.toLowerCase());
                    bw.newLine();
                    bw.flush();

                    //reading values from Broker
                    InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                    BufferedReader br = new BufferedReader(isr);

                    String result = br.readLine();
                    //if our name was valid
                    if (result.equals("yes")) {
                        AppNode.AppNodeID = name.toLowerCase();
                        AppNode.port = Integer.valueOf(br.readLine());
                        int i = Integer.valueOf(br.readLine());
                        AppNode.brokers = new ArrayList<>();
                        AppNode.brokers.add(br.readLine());
                        for (int j = 0; j < i; j++) {
                            AppNode.brokers.add(br.readLine());
                        }
                        AppNode.channelName = name;
                        AppNode.pic = "0";
                        AppNode.topicsInterested = new ArrayList<>();
                        AppNode.library = new ArrayList<>();
                        AppNode.streamingLibrary = new ArrayList<>();

                        //let's go to the next activity
                        Intent registeredIntent = new Intent(NameSet.this, Registered.class);
                        startActivity(registeredIntent);
                        finish();
                    }
                    else {
                        errorMessage.setText(result);
                        errorMessage.setVisibility(View.VISIBLE);
                        submitButton.setEnabled(true);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}