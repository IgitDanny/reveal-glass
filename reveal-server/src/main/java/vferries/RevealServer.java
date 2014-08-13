package vferries;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

import net.codestory.http.WebServer;
import net.codestory.http.payload.Payload;

public class RevealServer {
	private static BlockingQueue<Acknowledge> queue = new ArrayBlockingQueue<>(1);
	private static BlockingQueue<String> commands = new ArrayBlockingQueue<>(1);
	
	public static void main(String[] args) {
		new WebServer(routes -> routes.
			    get("/init", sendCommand("init")).
			    get("/next", sendCommand("next")).
			    get("/previous", sendCommand("previous")).
			    get("/listen", Stream.generate(RevealServer::waitForCommand)).
			    post("/ack", context -> {
			    	  Acknowledge ack = context.contentAs(Acknowledge.class);
			    	  try {
						queue.put(ack);
			    	  } catch (Exception e) {
			    	  }
			    	  return Payload.created();
			    	})
			).start();
	}

	private static Acknowledge sendCommand(String command) {
		System.out.println("Received " + command);
		Acknowledge ack = null;
		try {
			commands.put(command);
			ack = queue.take();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ack;
	}

	public static String waitForCommand() {
		try {
			return commands.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "";
	}
}
