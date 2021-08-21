package com.example.auebstream;

import android.net.Uri;

import java.util.ArrayList;

public class Video {
	
	public String videoName;
	public Uri videoURI;
	public boolean uploaded;
	public String dateCreated;
	public long length;
	public long framerate;
	public String frameWidth;
	public String frameHeight;
	public ArrayList<String> associatedHashtags;
	public byte[] bytes;
	public int chunks;
	public int remainder;
	
	//creates a string with all the hashtags
	public String hashtagLine() {
		String output = "";
		for (String hashtag: associatedHashtags) {
			output += hashtag;
			output += " ";
		}
		//removing the last gap
		output = output.substring(0, output.length());
		return output;
	}

	//creates a string with the right date form
	public String dateLine() {
		return dateCreated.substring(6,8) + "/" + dateCreated.substring(4,6) + "/" + dateCreated.substring(0,4);
	}
	
	public String toString() {
		return ("Video Name: " + videoName + "\n"
				+ "Date Created: " + dateLine() + "\n"
				+ "Duration: " + length + "\n"
				+ "Framerate: " + framerate + "\n"
				+ "Frame Width: " + frameWidth + "\n"
				+ "Frame Height: " + frameHeight + "\n"
				+ "Hashtags: " + hashtagLine());		
	}
}