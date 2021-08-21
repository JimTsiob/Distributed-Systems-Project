import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//this thread is used to accept requests of Brokers
public class BrokerListening extends Thread{
	private Broker bro;
	
	BrokerListening(Broker bro) {
		this.bro = bro;
	}
	
	public void run() {
		
		Socket socket = null;
		
		try {
			//string that decides which task we will operate
			String task = "";
			ServerSocket serverSocket = new ServerSocket(bro.port);
			//we are constantly listening on our own port
			while (true) {
				
				//whole thread stops here until someone wishes to connect to us
				socket = serverSocket.accept();
				//we start a new thread so no actions have to wait for BrokerListening to stop running
				ConnectionBrokerThread thread = new ConnectionBrokerThread(bro, task, socket);
				thread.start();
			}
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
	}
}