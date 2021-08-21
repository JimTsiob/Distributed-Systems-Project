package com.example.auebstream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

//this thread is used to upload a video in the background
public class AppNodeUpload extends Thread{

    Video video;

    AppNodeUpload(Video video) {
        this.video = video;
    }

    public void run() {

        try {

            ArrayList<String> topics = new ArrayList<String>();
            topics.add(AppNode.channelName);
            topics.addAll(video.associatedHashtags);
            sendTopics(topics);

            //send video to topic subscribes
            for (String topic : topics) {
                for (String port : AppNode.brokers) {

                    Socket socket = new Socket(AppNode.serverIP, Integer.valueOf(port));
                    OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                    BufferedWriter bw = new BufferedWriter(osw);

                    //nv stands for new video
                    bw.write("nv");
                    bw.newLine();
                    bw.write(AppNode.channelName);
                    bw.newLine();
                    bw.write(AppNode.AppNodeID);
                    bw.newLine();
                    bw.write(topic);
                    bw.newLine();
                    bw.flush();

                    InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    String toDO = br.readLine();

                    if (toDO.equals("yes")) {
                        int times = Integer.valueOf(br.readLine());
                        for (int i = 0; i < times; i++) {
                            bw.write(video.videoName);
                            bw.newLine();
                            bw.flush();
                            push(video, socket);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendTopics(ArrayList<String> parameterTopics) {

        try {

            //parameter topics to MD5
            ArrayList<String> topics = new ArrayList<String>();
            for (String topic: parameterTopics)
                topics.add(MD5(topic));

            //hashmap with key: MD5 IP+port and value: port
            HashMap<String, String> brokersHash = new HashMap<>();
            for (String port: AppNode.brokers) {
                brokersHash.put(MD5(AppNode.serverIP + port), port);
            }

            //getting collection of IP+ports from brokers
            Collection<String> keys = brokersHash.keySet();
            //creating an ArrayList of IP+ports
            ArrayList<String> hashing = new ArrayList(keys);
            //sorting hashing ArrayList
            Collections.sort(hashing);

            //hashmap so we can know if a specific topic has been sent or not
            HashMap<String, Boolean> topicSent = new HashMap<String, Boolean>();
            for (String topic: topics)
                topicSent.put(topic, false);

            int i;
            //now starts the good stuff, connections to brokers
            for (String iter: hashing) {
                i = 0;
                Socket socket = new Socket(AppNode.serverIP, Integer.valueOf(brokersHash.get(iter)));
                OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                BufferedWriter bw = new BufferedWriter(osw);
                //st stands for sends topics
                bw.write("st");
                bw.newLine();
                bw.flush();
                bw.write(AppNode.IP);
                bw.newLine();
                bw.flush();
                bw.write(AppNode.AppNodeID);
                bw.newLine();
                bw.flush();
                bw.write(String.valueOf(AppNode.port));
                for (String ator: topics) {
                    if (topicSent.get(ator) == false) {
                        if (iter.compareTo(ator) > 0) {
                            bw.newLine();
                            bw.write(parameterTopics.get(i));
                            topicSent.put(ator, true);
                        }
                    }
                    i++;
                }
                bw.close();
                osw.close();
                socket.close();
            }
            Socket socket = new Socket(AppNode.serverIP, Integer.valueOf(brokersHash.get(hashing.get(0))));
            OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write("st");
            bw.newLine();
            bw.flush();
            bw.write(AppNode.IP);
            bw.newLine();
            bw.flush();
            bw.write(AppNode.AppNodeID);
            bw.newLine();
            bw.flush();
            bw.write(String.valueOf(AppNode.port));
            i = 0;
            for (String topic: topics) {
                if (topicSent.get(topic) == false) {
                    bw.newLine();
                    bw.write(parameterTopics.get(i));
                }
                i++;
            }
            bw.close();
            osw.close();
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String MD5(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes(), 0, string.length());
            return new BigInteger(1, md.digest()).toString(16);
        }
        catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public void push(Video video, Socket socket) {

        byte[] videoBytes = video.bytes;

        try {

            Thread.sleep(1000);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            int i = 0;

            //sending chunk number to broker
            dos.writeInt(video.chunks);
            //sending remainder to broker
            dos.writeInt(video.remainder);
            dos.flush();

            for (i = 0; i < video.chunks; i++) {

                dos.write(videoBytes, i*AppNode.chunk_size, AppNode.chunk_size);
                dos.flush();

            }

            dos.write(videoBytes, i*AppNode.chunk_size, video.remainder);
            dos.flush();
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}