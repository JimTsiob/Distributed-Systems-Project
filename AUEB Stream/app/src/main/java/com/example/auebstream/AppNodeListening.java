package com.example.auebstream;

import android.content.Context;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//this thread is used to accept requests of Brokers
public class AppNodeListening extends Thread{

	Context context;

	AppNodeListening(Context context) {
		this.context = context;
	}

	public void run() {
		
		Socket socket = null;
		
		try {
			//string that decides which task we will operate
			String task = "";
			ServerSocket serverSocket = new ServerSocket(AppNode.port);

			//we are constantly listening on our own port
			while (true) {

				//whole thread stops here until someone wishes to connect to us
				socket = serverSocket.accept();

				//we start a new thread so no actions have to wait for AppNodeListening stop running
				ConnectionAppNodeThread thread = new ConnectionAppNodeThread(context, task, socket);
				thread.start();
			}
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
	}
}