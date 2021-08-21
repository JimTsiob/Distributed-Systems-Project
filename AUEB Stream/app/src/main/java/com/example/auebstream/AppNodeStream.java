package com.example.auebstream;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

//this thread is used to receive videos from brokers in the background
public class AppNodeStream extends Thread {

    String topic;
    Context context;

    AppNodeStream(String topic, Context context) {
        this.topic = topic;
        this.context = context;
    }

    public void run() {

        try {

            int i = (int) (Math.random() * AppNode.brokers.size());

            Socket socket = new Socket(AppNode.serverIP, Integer.valueOf(AppNode.brokers.get(i)));
            OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bw = new BufferedWriter(osw);

            //rv stands for request video
            bw.write("rv");
            bw.newLine();
            bw.write(AppNode.IP);
            bw.newLine();
            bw.write(AppNode.AppNodeID);
            bw.newLine();
            bw.write(String.valueOf(AppNode.port));
            bw.newLine();
            bw.write(topic);
            bw.newLine();
            bw.flush();

            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();

            if (result.equals("success")) {

                //adding the topic to the consumer's list
                AppNode.topicsInterested.add(topic);

                whichToGet(br, socket);

            }
            else if (result.equals("failure")) {

                String next = br.readLine();
                if (next.equals("yes")) {

                    Socket newSocket = new Socket(AppNode.serverIP, Integer.valueOf(br.readLine()));
                    OutputStreamWriter nosw = new OutputStreamWriter(newSocket.getOutputStream());
                    BufferedWriter nbw = new BufferedWriter(nosw);

                    nbw.write("rv");
                    nbw.newLine();
                    nbw.write(AppNode.IP);
                    nbw.newLine();
                    nbw.write(AppNode.AppNodeID);
                    nbw.newLine();
                    nbw.write(String.valueOf(AppNode.port));
                    nbw.newLine();
                    nbw.write(topic);
                    nbw.newLine();
                    nbw.flush();

                    InputStreamReader nisr = new InputStreamReader(newSocket.getInputStream());
                    BufferedReader nbr = new BufferedReader(nisr);
                    //it's not used in this case, just trash
                    nbr.readLine();

                    //adding the topic to the consumer's list
                    AppNode.topicsInterested.add(topic);

                    whichToGet(nbr, newSocket);
                }
                else if(next.equals("no")) {

                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(context, "No video with the topic " + topic + " was found.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void whichToGet(BufferedReader br, Socket socket) {

        try {

            String again = br.readLine();

            while (again.equals("go")) {

                String creator = br.readLine();
                int videoCount = Integer.valueOf(br.readLine());

                //video names
                ArrayList<String> names = new ArrayList<String>();

                for (int j = 0; j < videoCount; j++) {
                    //gets video name from the broker
                    names.add(br.readLine());
                }

                DataInputStream dis;

                //saves the videos on the streaming library of the user
                for (int j = 0; j < videoCount; j++) {

                    dis = new DataInputStream(socket.getInputStream());

                    int chunks = dis.readInt();
                    int remainder = dis.readInt();
                    byte[] videoBytes = new byte[chunks*AppNode.chunk_size + remainder];
                    int k = 0;

                    for (k = 0; k < chunks; k++) {

                        //gets video bytes from broker
                        dis.readFully(videoBytes, k*AppNode.chunk_size, AppNode.chunk_size);

                    }
                    dis.readFully(videoBytes, k*AppNode.chunk_size, remainder);
                    storeInFolder(videoBytes, context.getExternalFilesDir(null).getPath() + File.separator, names.get(j) + " by " + creator);
                    StreamingVideo newVideo = new StreamingVideo();
                    newVideo.channelName = creator;
                    newVideo.videoName = names.get(j);
                    newVideo.videoURI = Uri.parse(context.getExternalFilesDir(null).getPath() + File.separator + names.get(j) + " by " + creator + ".mp4");

                    //if we've sent this video already, don't want any duplicates
                    boolean flag = true;
                    for (StreamingVideo iter : AppNode.streamingLibrary) {
                        if (iter.videoName.equals(names.get(j)) && iter.channelName.equals(creator))
                            flag = false;
                    }
                    if (flag)
                        AppNode.streamingLibrary.add(newVideo);
                }

                Handler handler = new Handler(Looper.getMainLooper());

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (videoCount == 1 )
                            Toast.makeText(context, "Streaming video successfully!\nPlease reload the stream videos page.", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(context, "Streaming videos successfully!\nPlease reload  the stream videos page.", Toast.LENGTH_LONG).show();
                    }
                });

                again = br.readLine();
            }
        }
        catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    void storeInFolder(byte[] videoBytes, String videoPath, String videoName) {

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(videoPath + videoName + ".mp4");

            //write bytes[] into new file
            fos.write(videoBytes);
            fos.close();
        }
        catch (IOException e) {
        }
    }
}
