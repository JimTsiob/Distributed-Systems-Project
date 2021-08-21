import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

public class ConnectionBrokerThread extends Thread{

	final static int chunk_size = 512000;
	private Broker bro;
	private String task;
	private Socket socket;
	
	ConnectionBrokerThread(Broker bro, String task, Socket socket) {
		this.bro = bro;
		this.task = task;
		this.socket = socket;
	}
	
	public void run() {
		
		try {
			
			InputStreamReader isr = new InputStreamReader(socket.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			task = br.readLine();
			//stands for broker list
			if (task.equals("bl")) {
				bro.brokerList = bro.updateBrokers();
				System.out.println("[Updated brokerList, B" + br.readLine() + " was added]");
			}
			//stands for initialize me
			else if(task.equals("im")) {
				int portNum = 10000;
				
				try {
					String name = br.readLine();
					String line;
					String configFile = new File(System.getProperty("user.dir")).getParent() + "\\configuration.txt";
					File file = new File(configFile);
					
					BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
					Scanner sc = new Scanner(file);
					
					OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                    BufferedWriter bw = new BufferedWriter(osw);
                    
                    boolean flag = true;
					if (name.equals("")) {
						bw.write("You can't use a blank username.");
						bw.newLine();
						bw.flush();
						flag = false;
					}
					else {
						while (sc.hasNextLine()) {
							line = sc.nextLine();
							if ((line.charAt(0) == 'A') && (line.substring(1, line.indexOf(" ")).equals(name))) {
								bw.write("This username is not available.");
								bw.newLine();
								bw.flush();
								flag = false;
							}
							portNum += 250;
						}
						writer.newLine();
					}
				
					//yes, your name is valid, here is your port and broker ports lad
					if (flag) {
						writer.write("A" + name + " " + String.valueOf(portNum));
						writer.close();
						
						bw.write("yes");
						bw.newLine();
						bw.flush();
						bw.write(String.valueOf(portNum));
						bw.newLine();
						bw.flush();
						bw.write(String.valueOf(bro.brokerList.size()));
						bw.newLine();
						bw.flush();
						bw.write(String.valueOf(bro.port));
						bw.newLine();
						bw.flush();
						for (Broker iter: bro.brokerList) {
							bw.write(String.valueOf(iter.port));
							bw.newLine();
							bw.flush();
						}
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			//stands for send topics
			else if (task.equals("st")) {
				
				//checks if publisher is already registered
				boolean flag = true;
				//check if publisher should get registered
				boolean shouldReg = false;
				String thisLine = null;
				String AppNodeIP = br.readLine();
				String AppNodeID = br.readLine();
				String port = br.readLine();
				
				while ((thisLine = br.readLine()) != null) {
					if (!bro.topics.containsKey(AppNodeID))
						bro.topics.put(AppNodeID, new ArrayList<String>());
					shouldReg = true;
					bro.topics.get(AppNodeID).add(thisLine);
					System.out.println("[Added topic " + thisLine + " from " + AppNodeID + "]");	
				}
				
				for (AppNode pub: bro.registeredPublishers) {
					if (String.valueOf(pub.port).equals(port))
						flag = false;
				}
				if (flag && shouldReg) {
					AppNode pubToAdd = new AppNode();
					pubToAdd.IP = AppNodeIP;
					pubToAdd.AppNodeID = AppNodeID;
					pubToAdd.port = Integer.valueOf(port);
					bro.registeredPublishers.add(pubToAdd);
					System.out.println("[Added " + AppNodeID + " to registeredPublishers]");
				}
				//if broker has something to send
				if (shouldReg)
					bro.updateBrokerTopics(bro, AppNodeID, "add");
			}
			//stands for delete topics
			else if (task.equals("dt")) {
				
				//checks if a topic was deleted
				boolean flag = false;
				String thisLine = null;
				String AppNodeID = br.readLine();
				String otherPort = br.readLine();

				while ((thisLine = br.readLine()) != null) {
					if (bro.topics.containsKey(AppNodeID))
						if (bro.topics.get(AppNodeID).remove(thisLine)) {
							flag = true;
							System.out.println("[Removed topic " + thisLine + " from " + AppNodeID + "]");
						}
				}
				if (bro.topics.containsKey(AppNodeID)) 
					if (bro.topics.get(AppNodeID).size() == 0) {
						bro.topics.remove(AppNodeID);
						for (int i = 0; i < bro.registeredPublishers.size(); i++) {
							if (String.valueOf(bro.registeredPublishers.get(i).port).equals(otherPort)) {
								bro.registeredPublishers.remove(i);
								System.out.println("[Removed " + AppNodeID + " from registeredPublishers]");
								break;
							}
						}
					}
				if (flag)
					bro.updateBrokerTopics(bro, AppNodeID, "delete");
			}
			//stands for delete user
			else if (task.equals("du")) {
				
				String AppNodeID = br.readLine();
				String otherPort = br.readLine();
				
				for (int i = 0; i < bro.registeredPublishers.size(); i++) {
					if (String.valueOf(bro.registeredPublishers.get(i).port).equals(otherPort)) {
						bro.registeredPublishers.remove(bro.registeredPublishers.get(i));
						bro.topics.remove(AppNodeID);
						bro.updateBrokerTopics(bro, AppNodeID, "delete");
						System.out.println("[Erased Publisher " + AppNodeID + " from existance]");
						break;
					}
				}
				for (int i = 0; i < bro.registeredConsumers.size(); i++) {
					if (String.valueOf(bro.registeredConsumers.get(i).port).equals(otherPort)) {
						bro.registeredConsumers.remove(bro.registeredConsumers.get(i));
						System.out.println("[Erased Consumer " + AppNodeID + " from existance]");
						break;
					}
				}
			}
			//stands for add brokerList topics
			else if (task.equals("abt")) {
				
				String AppNodeID = br.readLine();
				String otherPort = br.readLine();
				String thisLine = null;
				for (int i = 0; i < bro.brokerList.size(); i++) {
					if (String.valueOf(bro.brokerList.get(i).port).equals(otherPort)) {
						if (!bro.brokerList.get(i).topics.containsKey(AppNodeID)) {
							bro.brokerList.get(i).topics.put(AppNodeID, new ArrayList<String>());
						}
						else
							bro.brokerList.get(i).topics.put(AppNodeID, new ArrayList<String>());
					}
				}
				while ((thisLine = br.readLine()) != null) {
					for (int i = 0; i < bro.brokerList.size(); i++) {
						if (String.valueOf(bro.brokerList.get(i).port).equals(otherPort)) {
							bro.brokerList.get(i).topics.get(AppNodeID).add(thisLine);
							break;
						}
					}
				}
			}
			//stands for delete brokerList topics
			else if (task.equals("dbt")) {
				String AppNodeID = br.readLine();
				String otherPort = br.readLine();
				String toDo = br.readLine();
				String thisLine = null;
				
				//just remove some topics, not the user entirely
				if (toDo.equals("yes")) {
					for (int i = 0; i < bro.brokerList.size(); i++) {
						if (String.valueOf(bro.brokerList.get(i).port).equals(otherPort)) {
							bro.brokerList.get(i).topics.put(AppNodeID, new ArrayList<String>());
						}
					}
					while ((thisLine = br.readLine()) != null) {
						for (int i = 0; i < bro.brokerList.size(); i++) {
							if (String.valueOf(bro.brokerList.get(i).port).equals(otherPort)) {
								bro.brokerList.get(i).topics.get(AppNodeID).add(thisLine);
								break;
							}
						}
					}
				}
				//remove the user entirely
				else if (toDo.equals("no")) {
					for (int i = 0; i < bro.brokerList.size(); i++) {
						if (String.valueOf(bro.brokerList.get(i).port).equals(otherPort)) {
							bro.brokerList.get(i).topics.remove(AppNodeID);
							break;
						}
					}
				}
			}
			//stands for request video
			else if (task.equals("rv")) {
				
				String AppNodeIP = br.readLine();
				String AppNodeID = br.readLine();
				String otherPort = br.readLine();
				String topic = br.readLine();
				
				//if the broker has this topic or not
				boolean gotIt = false;
				
				//Consumer OutputStreamWriter
				OutputStreamWriter oswc = new OutputStreamWriter(socket.getOutputStream());
				BufferedWriter bwc = new BufferedWriter(oswc);
				
				//tests if this broker has vidoes to send based on the topic
				if (!bro.registeredPublishers.isEmpty()) {
					for (AppNode pub: bro.registeredPublishers) {
						//if the publisher we are checking has the topic
						if ((bro.topics.get(pub.AppNodeID).contains(topic)) && (!pub.AppNodeID.equals(AppNodeID))) {
							gotIt = true;
							bwc.write("success");
							bwc.newLine();
							bwc.flush();
							break;
						}
					}
				}
				//if the broker has videos to send based on the topic
				if (gotIt) {
					if (!bro.registeredPublishers.isEmpty()) {
						for (AppNode pub: bro.registeredPublishers) {
							//if the publisher we are checking has the topic
							if ((bro.topics.get(pub.AppNodeID).contains(topic)) && (!pub.AppNodeID.equals(AppNodeID))) {
								
								oswc = new OutputStreamWriter(socket.getOutputStream());
								bwc = new BufferedWriter(oswc);
								//tells the consumer that this publisher is gonna send a video
								bwc.write("go");
								bwc.newLine();
								bwc.flush();
								
								System.out.println("[Found the topic " + topic + " on A" + pub.AppNodeID + "]");
								
								//when flag == true, add user to registeredConsumers
								boolean flag = true;
								//add or not?
								if (!bro.registeredConsumers.isEmpty()) {
									for (AppNode iter: bro.registeredConsumers) {
										if (iter.AppNodeID.equals(AppNodeID)) {
											flag = false;
											if (!iter.topicsInterested.contains(topic))
												iter.topicsInterested.add(topic);
										}
									}
								}
								//adding AppNode to registeredConsumers
								if (flag) {
									bro.registeredConsumers.add(new AppNode());
									bro.registeredConsumers.get(bro.registeredConsumers.size()-1).IP = AppNodeIP;
									bro.registeredConsumers.get(bro.registeredConsumers.size()-1).AppNodeID = AppNodeID;
									bro.registeredConsumers.get(bro.registeredConsumers.size()-1).port = Integer.valueOf(otherPort);
									bro.registeredConsumers.get(bro.registeredConsumers.size()-1).topicsInterested = new ArrayList<String>();
									bro.registeredConsumers.get(bro.registeredConsumers.size()-1).topicsInterested.add(topic);
									System.out.println("[Added " + AppNodeID + " to registeredConsumers]");
								}
								
								//Starting conneciton with the Publisher
								Socket socketPub = new Socket(pub.IP, Integer.valueOf(Integer.valueOf(pub.port)));
								
								//Publisher InputStreamReader
								InputStreamReader isrp = new InputStreamReader(socketPub.getInputStream());
								BufferedReader brp = new BufferedReader(isrp);
								
								//Publisher OutputStreamWriter
						    	OutputStreamWriter oswp = new OutputStreamWriter(socketPub.getOutputStream());
						    	BufferedWriter bwp = new BufferedWriter(oswp);
						    	
						    	bwp.write("rv");
						    	bwp.newLine();
						    	bwp.flush();
						    	
						    	//informs the publisher about the wanted topic
						    	bwp.write(topic);
						    	bwp.newLine();
						    	bwp.flush();
						    	
						    	//learns what the publisher's name is
						    	String name = brp.readLine();
						    	
						    	//learns how many videos are gonna be streamed from this publisher
						    	int videoCount = (Integer.valueOf(brp.readLine()));
						    	
						    	if (videoCount == 1)
						    		System.out.println("[Publisher A" + pub.AppNodeID + " has only one video to send]");
						    	else
						    		System.out.println("[Publisher A" + pub.AppNodeID + " has " + videoCount + " videos to send]");
						    			
						    	//informs the consumer what the publisher's name is
						    	bwc.write(name);
						    	bwc.newLine();
						    	bwc.flush();
						    	
						    	//informs the consumer how many videos are gonna be streamed
						    	bwc.write(String.valueOf(videoCount));
						    	bwc.newLine();
						    	bwc.flush();
						    	
						    	//sends the video names to the consumer
						    	for (int i = 0; i < videoCount; i++) {
						    		bwc.write(brp.readLine());
						    		bwc.newLine();
						    	}
						    	bwc.flush();
		
							    //Consumer DataOutputStream
						    	DataOutputStream dosc = new DataOutputStream(socket.getOutputStream());
							    
							    //Publisher DataInputStream
							    DataInputStream disp = new DataInputStream(socketPub.getInputStream());
							    
						    	for (int i = 0; i < videoCount; i++) {
						    		
								    int j = 0;
								    
						    		//receiving chunk number from publisher
								    int chunks = disp.readInt();
								    //sending chunk number to consumer
								    dosc.writeInt(chunks);
						    		dosc.flush();
						    		
						    		//receiving remainder from publisher
						    		int remainder = disp.readInt();
						    		//sending remainder to consumer
						    		dosc.writeInt(remainder);
						    		dosc.flush();
						    		
						    		for (j = 0; j < chunks; j++) {
						    			
							    		//gets video bytes from publisher
									    byte[] videoBytes = new byte[chunk_size];
									    disp.readFully(videoBytes, 0, chunk_size);   		
									    
									    //writes bytes to consumer
										dosc.write(videoBytes);
										dosc.flush();
										
						    		}
						    		
						    		//gets remainder video bytes from publisher
								    byte[] videoBytes = new byte[remainder];
								    disp.readFully(videoBytes, 0, remainder);	
								    
								    //writes remainder bytes to consumers
									dosc.write(videoBytes);
									dosc.flush();
									
						    	}
							}
						}
						
						//tells the consumer that he is done sending videos
				    	bwc.write("done");
				    	bwc.newLine();
				    	bwc.flush();
					}
				}
				//else this broker doesn't have the topic
				else if (!gotIt) {
					System.out.println("[Could not find a video with the topic " + topic + "]");
					
					bwc.write("failure");
					bwc.newLine();
					bwc.flush();
					boolean flag = false;
					
					for (Broker broker: bro.brokerList) {
						
						HashMap<String, ArrayList<String>> temp = new HashMap<String, ArrayList<String>>(broker.topics);
						//removing the consumer's topics so we don't return his own video
						temp.remove(AppNodeID);
						
						Collection<ArrayList<String>> values = temp.values();
					    //creating an ArrayList of ArrayLists of other broker's topics
					    ArrayList<ArrayList<String>> otherTopics = new ArrayList(values);
					    //when flag = true, means I found the topic on that broker
					   
					    
					    for (ArrayList<String> other: otherTopics) {
					    	if (other.contains(topic)) {
					    		flag = true;
					    		System.out.println("[Broker B" + broker.brokerID + " has the topic " + topic + "]");
					    		System.out.println("[Connecting Consumer A" + AppNodeID + " to B" + broker.brokerID + "]");
					    		break;
					    	}
					    }
					    if (flag) {
					    	bwc.write("yes");
					    	bwc.newLine();
					    	bwc.flush();
					    	bwc.write(String.valueOf(broker.port));
					    	bwc.newLine();
					    	bwc.flush();
					    	break;
					    }
					}
					//if we didn't find the topic
					if (!flag) {
						bwc.write("no");
						bwc.newLine();
						bwc.flush();
					}
				}
			}
			//stands for new video
			else if (task.equals("nv")) {
				
				String name = br.readLine();
				String AppNodeID = br.readLine();
				String topic = br.readLine();
				
				//send video or not
				boolean flag = false;
				for (AppNode iter: bro.registeredConsumers) {
					if (iter.topicsInterested.contains(topic)) {
						flag = true;
						break;
					}
				}
				//Publisher OutputStreamWriter
				OutputStreamWriter oswp = new OutputStreamWriter(socket.getOutputStream());
				BufferedWriter bwp = new BufferedWriter(oswp);
				
				if (flag) {
					
					//yes, send the video
					bwp.write("yes");
					bwp.newLine();
					bwp.flush();
					ArrayList<Socket> connections = new ArrayList<Socket>();
					int count = 0;
					
					//deciding which consumers we will send the video to
					for (AppNode iter: bro.registeredConsumers)
						if ((iter.topicsInterested.contains(topic)) && (!iter.AppNodeID.equals(AppNodeID))) {
							connections.add(new Socket(iter.IP, Integer.valueOf(iter.port)));
							count++;
						}
					
					bwp.write(String.valueOf(count));
					bwp.newLine();
					bwp.flush();
					
				    //Publisher DataInputStream
				    DataInputStream disp = new DataInputStream(socket.getInputStream());
				    
					for (int i = 0; i < count; i++) {
			    		
					    int j = 0;
					    
					    Socket socketCon = connections.get(i);
					    
					    //Consumer DataOutputStream
				    	DataOutputStream dosc = new DataOutputStream(socketCon.getOutputStream());
				    	
				    	OutputStreamWriter oswc = new OutputStreamWriter(socketCon.getOutputStream());
						BufferedWriter bwc = new BufferedWriter(oswc);
				    	
						bwc.write("nv");
						bwc.newLine();
						bwc.flush();
						
						bwc.write(name);
						bwc.newLine();
						bwc.flush();
						
				    	String videoName = br.readLine();
					    bwc.write(videoName);
					    bwc.newLine();
						bwc.flush();
					    
			    		//receiving chunk number from publisher
					    int chunks = disp.readInt();
					    //sending chunk number to consumer
					    dosc.writeInt(chunks);
			    		dosc.flush();
			    		
			    		//receiving remainder from publisher
			    		int remainder = disp.readInt();
			    		//sending remainder to consumer
			    		dosc.writeInt(remainder);
			    		dosc.flush();
			    		
			    		for (j = 0; j < chunks; j++) {
				    		//gets video bytes from publisher
						    byte[] videoBytes = new byte[chunk_size];
						    disp.readFully(videoBytes, 0, chunk_size);   		
						    
						    //writes bytes to consumer
							dosc.write(videoBytes);
							dosc.flush();
							
			    		}
			    		
			    		//gets remainder video bytes from publisher
					    byte[] videoBytes = new byte[remainder];
					    disp.readFully(videoBytes, 0, remainder);	
					    
					    //writes remainder bytes to consumers
						dosc.write(videoBytes);
						dosc.flush();
						
			    	}
				}
				else {
					
					//no, don't send the video
					bwp.write("no");
					bwp.newLine();
					bwp.flush();
				}
			}
		}
		catch (IOException e) {
		}
	}
}
