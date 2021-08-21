package com.example.auebstream;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectionAppNodeThread extends Thread{

	Context context;
	private String task;
	private Socket socket;
	
	ConnectionAppNodeThread(Context context, String task, Socket socket) {
		this.context = context;
		this.task = task;
		this.socket = socket;
	}
	
	public void run() {
		
		try {
			
			InputStreamReader isr = new InputStreamReader(socket.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			
			task = br.readLine();
			
			//stands for request video
			if (task.equals("rv")) {
			
				OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
		    	BufferedWriter bw = new BufferedWriter(osw);
				
		    	String topic = br.readLine();
				
				ArrayList<Video> videos = whichToSend(topic);

				//sends the name to the broker
				bw.write(AppNode.channelName);
				bw.newLine();
				bw.flush();

				//sends the number of videos to the broker
				bw.write(String.valueOf(videos.size()));
				bw.newLine();
				bw.flush();
				
				//sends the video names to the broker
				for (Video video: videos) {
					bw.write(video.videoName);
					bw.newLine();
				}
				bw.flush();
				
				//writes the video bytes to the broker
				for (Video video: videos) {
					push(video, socket);
				}
			}
			//stands for new video
			else if (task.equals("nv")) {

				String creator = br.readLine();
				String videoName = br.readLine();
				
		    	DataInputStream dis;
					
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
				storeInFolder(videoBytes, context.getExternalFilesDir(null).getPath() + File.separator, videoName + " by " + creator);
				StreamingVideo newVideo = new StreamingVideo();
				newVideo.channelName = creator;
				newVideo.videoName = videoName;
				newVideo.videoURI = Uri.parse(context.getExternalFilesDir(null).getPath() + File.separator + videoName + " by " + creator + ".mp4");

				//if we've sent this video already, don't want any duplicates
				boolean flag = true;
				for (StreamingVideo iter : AppNode.streamingLibrary) {
					if (iter.videoName.equals(videoName) && iter.channelName.equals(creator))
						flag = false;
				}
				if (flag) {
					AppNode.streamingLibrary.add(newVideo);

					Handler handler = new Handler(Looper.getMainLooper());

					handler.post(new Runnable() {

						@Override
						public void run() {

							Toast.makeText(context, "Received video from " + creator + "!\nPlease reload the stream videos page.", Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Video> whichToSend(String topic) {

		if (topic.equals(AppNode.channelName)) {
			return AppNode.library;
		}
		else {

			ArrayList<Video> videos = new ArrayList<Video>();
			for (Video video: AppNode.library) {
				if (video.associatedHashtags.contains(topic))
					videos.add(video);
			}
			return videos;
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