import java.util.LinkedList;
import java.util.Queue;

public class Client {
	private boolean isActive;
	private Queue<Message> messageQueue;
	private Thread displayThread;
	private ClientWindow window;
	
	public Client() {
		messageQueue = new LinkedList<>();
		//
//		messageQueue = new PriorityQueue<>();
		window = new ClientWindow(this);
		window.openClient();
		window.displayMessage(new Message("Hello"));
	}
	
	public void send(Message message) {
		// TODO: implement this!
	}
	
	public void activateMessages() {
		displayThread  = new Thread() {
			public void run() {
				while (!messageQueue.isEmpty()) {
					Message message;
					if ((message = messageQueue.poll()) != null) {
						window.displayMessage(message);
					}
				}
			}
		};
		displayThread.start();
	}
	
	public void deactivateMessages() {
		displayThread.interrupt();
	}
	
	public Queue<Message> getMessageQueue() {
		return messageQueue;
	}
	
	public static void main(String[] args) {
		Client testClient = new Client();
	}
}
