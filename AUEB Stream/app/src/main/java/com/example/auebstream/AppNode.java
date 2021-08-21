package com.example.auebstream;

import android.app.Application;

import java.util.ArrayList;

public class AppNode extends Application {

    final static int chunk_size = 512000;

    public static String serverIP;
    public static String AppNodeID;
    public static int port;
    public static ArrayList<String> topicsInterested;
    public static String channelName;
    public static String IP;
    public static String pic;
    public static ArrayList<Video> library;
    public static ArrayList<StreamingVideo> streamingLibrary;
    public static ArrayList<String> brokers;

}
