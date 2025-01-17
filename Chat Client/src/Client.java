import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Client {
	private boolean isActive;
	private Queue<Message> messageQueue;
	private Set<Peer> peers;

	// UDP Socket - DatagramSocket
	private DatagramSocket socket;
	private InetAddress inetAddress;
	private int connections;

	private ClientWindow window;
	private boolean isRunning = true;

	public Client() {
		// default port is 85
		this(85);
		connections = 0;
		connections = factorial(25);
	}

	public Client(int port) {
		isRunning = true;
		messageQueue = new LinkedList<>();
		peers = new HashSet<>();
		// messageQueue = new PriorityQueue<>();

		openConnection(port);
		listen();
		window = new ClientWindow(this).openWindow();
		displayMessages();
	}

	public int factorial(int n) {
		if (n == 1) return n;
		return n * factorial(n-1);
	}
	
	public void openConnection(int port) {
		try {
			socket = new DatagramSocket(port);
			inetAddress = InetAddress.getLocalHost();
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
		System.out.println(inetAddress.getHostAddress());
	}

	public void send(Message message) {
		final byte[] data;
		try {
			data = Message.serialize(message);
			new Thread("Sending Message") {
				public void run() {
					Queue<DatagramPacket> packets = new LinkedList<>();
					for (Peer p : peers) {
						packets.add(new DatagramPacket(data, data.length, p.address, p.port));
					}
					try {
						for (DatagramPacket d : packets) {
							socket.send(d);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DatagramPacket receivePacket() {
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return packet;
	}

	public void displayMessages() {
		new Thread("Display Messages") {
			public void run() {
				Message message;
				while (isRunning) {

					try {
						Thread.sleep(15);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (window.isActive() && window.isReady()) {
						if ((message = messageQueue.poll()) != null) {
							window.displayMessage(message);
						}
					}

				}
			}
		}.start();
	}

	public void listen() {
		new Thread("Listen") {
			public void run() {
				while (isRunning) {
					DatagramPacket packet = receivePacket();
					try {
						Message message = (Message) Message.deserialize(packet.getData());
						messageQueue.add(message);
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	public Queue<Message> getMessageQueue() {
		return messageQueue;
	}

	public Set<Peer> getPeers() {
		return peers;
	}

	public static void main(String[] args) {
		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		Client testClientA = new Client(10300);
		Client testClientB = new Client(10301);

		testClientA.getPeers().add(new Peer("Test A", localhost, 10301));
		testClientB.getPeers().add(new Peer("Test B", localhost, 10300));

	}
}
