import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

public class Broker {
	
	String brokerID;
	int port;
	static String IP = "127.0.0.1";
	ArrayList<Broker> brokerList;
	HashMap<String, ArrayList<String>> topics;
	ArrayList<AppNode> registeredPublishers;
	ArrayList<AppNode> registeredConsumers;
	
	public static void main(String[] args) throws UnknownHostException {
		
		Broker broker = new Broker();
		
		//the basics
		broker.initPortID();
		broker.topics = new HashMap<String, ArrayList<String>>();
		broker.registeredPublishers = new ArrayList<AppNode>();
		broker.registeredConsumers = new ArrayList<AppNode>();
		broker.brokerList = broker.updateBrokers();
		
		//the good stuff
		InetAddress inetAddress = InetAddress.getLocalHost();
		System.out.println("Server IP Address: " + inetAddress.getHostAddress());
		BrokerListening thread = new BrokerListening(broker);
		thread.start();
		
		/*this for loop has only one purpose, updating every broker's brokerList
		if we didn't do this, the oldest brokers would have no idea that there are
		new brokers they should add to their list because the oldest brokers used an
		outdated version of the configuration file to initialize their brokerList
		 
		this for loop asks to connect with every single broker (except itself)
		and uppon doing so, it tells the brokers to run the method updateBrokers*/

		for (Broker otherBroker: broker.brokerList) {
			try {
				
				Socket socket = new Socket(IP, Integer.valueOf(otherBroker.port));
				OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
				BufferedWriter bw = new BufferedWriter(osw);
				//bl stands for broker list
				bw.write("bl");
				bw.newLine();
				bw.write(broker.brokerID);
				
				//that's it, we are done
				bw.close();
				osw.close();
				socket.close();
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		
		}
		Scanner input = new Scanner(System.in);
		String again;
		do {
			again = input.nextLine();
			System.out.println("My topics:");
			for (AppNode pub: broker.registeredPublishers)
				for (String top: broker.topics.get(pub.AppNodeID))
					System.out.println(pub.AppNodeID + " has " + top);
			System.out.println("\nOther Brokers' topics:");
			ArrayList<String> temp = new ArrayList<String>();
			for (Broker br: broker.brokerList) {
				Collection<String> keys = br.topics.keySet();
				ArrayList<String> users = new ArrayList(keys);
				for (String user: users) {
					for (String topi: br.topics.get(user))
						System.out.println("B" + br.port + ": " + topi);
				}
			}
			System.out.println("\nMy registered Publishers: ");
			for (AppNode pub: broker.registeredPublishers) {
				System.out.println(pub.AppNodeID + " " + pub.IP + " " + pub.port);
			}
			System.out.println("\nMy registered Consumers: ");
			for (AppNode con: broker.registeredConsumers) {
				System.out.println(con.AppNodeID + " " + con.IP + " " + con.port);
			}
			System.out.println();
		}
		while (true);
		
	}
	
	void initPortID() {
		
		int B = 0;
		int portNum = 10000;
		
		try {

			String configFile = new File(System.getProperty("user.dir")).getParent() + "\\configuration.txt";
			File file = new File(configFile);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			Scanner sc = new Scanner(file);
			if (file.length() == 0) {
				writer.write("B" + String.valueOf(B) + " " + String.valueOf(portNum));
			}
			else {
				while (sc.hasNextLine()) {
					if (sc.nextLine().charAt(0) == 'B')
						B++;
					portNum += 250;
				}
				writer.newLine();
				writer.write("B" + String.valueOf(B) + " " + String.valueOf(portNum));
			}
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		brokerID = String.valueOf(B);
		port = portNum;
	}

	ArrayList<Broker> updateBrokers() {
		
		ArrayList<Broker> temp = new ArrayList<Broker>();
		try {
			String configFile = new File(System.getProperty("user.dir")).getParent() + "\\configuration.txt";
			File file = new File(configFile);
			Scanner sc = new Scanner(file);
			String line;
			String givenID;
			int givenPort;
			while(sc.hasNextLine()) {
				//the line we are reading
				line = sc.nextLine();
				//the ID of this line's interface
				givenID = line.substring(1, line.indexOf(" "));
				//the port of this line's interface
				givenPort = Integer.valueOf(line.substring(line.indexOf(" ") + 1, line.length()));
				if (line.contains("B") && (givenPort != port)) {
					temp.add(new Broker());
					temp.get(temp.size()-1).brokerID = givenID;
					temp.get(temp.size()-1).port = givenPort;
					temp.get(temp.size()-1).topics = new HashMap<String, ArrayList<String>>();
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		/*for (Broker str: temp) {
			System.out.println("B" + str.brokerID + " " + str.port);
		}
		System.out.println();*/
		
		return temp;
	}
	
	void updateBrokerTopics(Broker bro, String AppNodeID, String task) {
		
		//System.out.println("I am B" + bro.brokerID);
		for (Broker otherBroker: bro.brokerList) {
			
			try {
			
				Socket socket;
				//System.out.println("Start connection with B" + otherBroker.brokerID);
				socket = new Socket(otherBroker.IP, Integer.valueOf(otherBroker.port));
		    	OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
		    	BufferedWriter bw = new BufferedWriter(osw);
		    	
		    	//for adding topics
		    	if (task.equals("add")) {
		    		
		    		//ut stands for add brokerList topics
			    	bw.write("abt");
			    	bw.newLine();
			    	bw.write(AppNodeID);
			    	bw.newLine();
			    	bw.write(String.valueOf(bro.port));
		    	
			    	for (String topic: bro.topics.get(AppNodeID)) {
			    		bw.newLine();
			    		bw.write(topic);
			    	}
			    	
			    	//that's it, we are done
					bw.close();
					osw.close();
					socket.close();
		    	}
		    	//for deleting topics
		    	else if(task.equals("delete")) {
		    		
		    		//dbt stands for delete brokerList topics
			    	bw.write("dbt");
			    	bw.newLine();
			    	bw.write(AppNodeID);
			    	bw.newLine();
			    	bw.write(String.valueOf(bro.port));
			    	bw.newLine();
			    	
			    	if (bro.topics.containsKey(AppNodeID)) {
			    		bw.write("yes");
			    		for (String topic: bro.topics.get(AppNodeID)) {
				    		bw.newLine();
				    		bw.write(topic);
				    	}
			    	}
			    	else {
			    		bw.write("no");
			    	}
			    	
			    	//that's it, we are done
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
	
}