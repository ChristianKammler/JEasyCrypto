import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CryptoClient implements Runnable, ReaderObserver {

	private static final int CAPABILITY_MENU = 1;
	private static final int ENCRYPT_MENU = 2;
	private static final int DECRYPT_MENU = 3;
	private static final int QUIT_MENU = 4;

	private ResponseReader reader = null;

	private DatagramSocket socket = null;
	private InetAddress serverAddr = null;
	private int serverPort = 10000;
	
	int requestId = 0;
	
	private Console console = System.console();

	/**
	 *Main client loop
	 *Asks CryptoServer address from user
	 *Shows menu to user and performs chosen action
	 */
	@Override
	public void run() {
		// Prepare variables.
		try {
			socket = new DatagramSocket(10001);
			
			serverAddr = queryServerAddress();
			if (serverAddr == null) {
				console.printf("Server address not given / invalid!\n");
				console.printf("Quitting CryptoClient!\n");
				return;
			}
			
			reader = new ResponseReader(socket, this);
			reader.start();

			int choice;
			do {
				// Display menu.
				choice = selectMenuItem();

				switch (choice) {
				// Handle Capability menu command.
				case CAPABILITY_MENU: {
					handleCapabilityRequest();
					break;
				}
				// Handle Encrypt menu command.
				case ENCRYPT_MENU: {
					handleEncryptRequest();
					break;
				}
				// Handle Decrypt menu command.
				case DECRYPT_MENU: {
					handleDecryptRequest();
					break;
				}
				// Handle Quit menu command.
				case QUIT_MENU: {
					handleQuitRequest();
					break;
				}
				}
			} while (choice != QUIT_MENU);
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			reader.interrupt();
			reader = null;
			socket.close();
			socket = null;
		}
	}

	/**
	 *Asks server address from the user
	 */
	private InetAddress queryServerAddress() {
		console.printf("Welcome to CryptoClient!\n");
		console.printf("Enter CryptoServer address in the form \"123.123.123.123\" or hostname\n");
		String address = console.readLine("Address > ");
		InetAddress addr;
		try {
			addr = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			addr = null;
		}
		return addr;
	}

	/**
	 *Starts client
	 */
	public static void main(String[] args) {
		new CryptoClient().run();
	}

	/**
	 *Shows text based menu to user
	 *@return Action chosen by user
	 */
	private int selectMenuItem() {
		int choice = -1;
		do {
			console.printf("Welcome to CryptoClient!\n");
			console.printf("Select command (1-4):\n");
			console.printf(" 1: Send capability request to service\n");
			console.printf(" 2: Send encrypt request to service\n");
			console.printf(" 3: Send decrypt request to service\n");
			console.printf(" 4: Quit application\n");
			console.flush();
			String selection = console.readLine("Your choice > ");
			try {
				choice = Integer.parseInt(selection);
			} catch (NumberFormatException e) {
				console.printf("** Error - not a valid number ** !\n");
				choice = -1;
			}
		} while (choice < 1 || choice > 4);
		return choice;
	}

	/**
	 *Wraps reading input reading from console
	 */
	private String enterText(String prompt, boolean required) {
		String s = "";
		do {
			s = console.readLine(prompt+" > ");
		} while (s.length() == 0 && required);
		return s;
	}

	/**
	 *Requests information for supported methods from the server
	 */
	private void handleCapabilityRequest() throws IOException {
		JSONObject request = new JSONObject();
		request.put("id", requestId++);
		request.put("operation", "capabilities");
		String data = request.toJSONString();
		console.printf("Request for capability info: " + data + "\n\n");
		byte[] serializedData = data.getBytes(StandardCharsets.UTF_16);
		DatagramPacket packet = new DatagramPacket(serializedData, serializedData.length, serverAddr, serverPort);
		socket.send(packet);
	}

	/**
	 *Sends encryption request to the server
	 */
	private void handleEncryptRequest() throws IOException {
		JSONObject request = new JSONObject();
		request.put("id", requestId++);
		request.put("operation", "encrypt");
		String method = enterText("Give encryption method", true);
		String text = enterText("Give text to encrypt", false);
		request.put("method", method);
		request.put("data", text);
		String data = request.toJSONString();
		console.printf("Request for encryption: " + data + "\n\n");
		byte[] serializedData = data.getBytes(StandardCharsets.UTF_16);
		DatagramPacket packet = new DatagramPacket(serializedData, serializedData.length, serverAddr, serverPort);
		socket.send(packet);
	}

	/**
	 *Sends decryption request to the server
	 */
	private void handleDecryptRequest() throws IOException {
		JSONObject request = new JSONObject();
		request.put("id", requestId++);
		request.put("operation", "decrypt");
		String method = enterText("Give decryption method", true);
		String text = enterText("Give text to decrypt", false);
		request.put("method", method);
		request.put("data", text);
		String data = request.toJSONString();
		console.printf("Request for decryption: " + data + "\n\n");
		byte[] serializedData = data.getBytes(StandardCharsets.UTF_16);
		DatagramPacket packet = new DatagramPacket(serializedData, serializedData.length, serverAddr, serverPort);
		socket.send(packet);
	}

	/**
	 *Quits client
	 */
	private void handleQuitRequest() {
		reader.stopReading();
	}

	/**
	 *Prints response information
	 */
	@Override
	public void handleResponse(JSONObject response) throws InterruptedException {
		System.out.println("Got response from reader...");
		System.out.printf("Request ID: %d\n", response.get("id"));
		System.out.printf("Operation: %s\n", response.get("operation"));
		System.out.printf("Result: %d\n", response.get("result"));
		System.out.printf("Data: %s\n", response.get("data"));
	}
}
