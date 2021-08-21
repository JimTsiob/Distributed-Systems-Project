package com.example.auebstream;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

//this thread is used to delete topics from brokers in the background
public class AppNodeDelete extends Thread{

    ArrayList<String> topics = new ArrayList<>();

    AppNodeDelete(ArrayList <String> topics) {
        this.topics = topics;
    }

    public void run() {

        try {

            //now starts the good stuff, connections to brokers
            for (String iter: AppNode.brokers) {

                Socket socket = new Socket(AppNode.serverIP, Integer.valueOf(iter));
                OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                BufferedWriter bw = new BufferedWriter(osw);
                //dt stands for delete topics
                bw.write("dt");
                bw.newLine();
                bw.write(AppNode.AppNodeID);
                bw.newLine();
                bw.write(String.valueOf(AppNode.port));
                for (String ator: topics) {
                    bw.newLine();
                    bw.write(ator);
                }
                bw.close();
                osw.close();
                socket.close();
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}