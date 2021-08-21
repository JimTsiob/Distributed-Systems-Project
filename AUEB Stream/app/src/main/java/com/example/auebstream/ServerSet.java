package com.example.auebstream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerSet extends AppCompatActivity{

    private int STORAGE_PERMISSION_CODE = 1;
    String serverIP;
    EditText serverInput;
    Button submitButton;
    TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_set);

        if (!(ContextCompat.checkSelfPermission(ServerSet.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        serverInput = findViewById(R.id.serverInput);
        errorMessage = findViewById(R.id.errorMessage);
        errorMessage.setVisibility(View.INVISIBLE);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Socket socket = new Socket();
                boolean flag = true;
                try {
                    serverIP = serverInput.getText().toString();
                    submitButton.setEnabled(false);

                    //setting a timeout just in case
                    socket.connect(new InetSocketAddress(serverIP, 10000), 1500);

                }
                catch (IOException e) {
                    errorMessage.setVisibility(View.VISIBLE);
                    submitButton.setEnabled(true);
                    flag = false;
                }
                if (flag) {
                    try {
                        //writing trash, avoids error in ConnectionBrokerThread
                        OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                        BufferedWriter bw = new BufferedWriter(osw);

                        bw.write("trash");
                        bw.newLine();
                        bw.flush();

                        //setting our IP address
                        if (Build.HARDWARE.contains("ranchu"))
                            AppNode.IP = "127.0.0.1";
                        else {
                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                            AppNode.IP = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
                        }

                        //setting the server IP address
                        AppNode.serverIP = serverIP;

                        //let's go to the next activity
                        Intent nameIntent = new Intent(ServerSet.this, NameSet.class);
                        startActivity(nameIntent);
                        finish();

                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "AUEB Stream need this permission in order to function.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}