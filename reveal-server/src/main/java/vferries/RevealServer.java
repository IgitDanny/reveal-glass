package vferries;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import net.codestory.http.WebServer;

public class RevealServer {
	private static BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);
	private static BlockingQueue<String> commands = new ArrayBlockingQueue<>(5);
	
	public static void main(String[] args) {
		new WebServer(routes -> routes.
			    get("/init", () -> sendCommand("init")).
			    get("/next", () -> sendCommand("next")).
			    get("/previous", () -> sendCommand("previous")).
			    post("/listen", () -> waitForCommand()).
			    post("/ack", context -> {
			    	  try {
						queue.put(context.get("data"));
						return waitForCommand();
			    	  } catch (Exception e) {
			    	  }
			    	  return null;
			    	})
			    	.filter((uri, context, next) -> {
			    		System.out.println(uri);
			    		return next.get().withAllowOrigin("*");	
			    	})
			).start();
	}

	private static String sendCommand(String command) {
		String ack = null;
		try {
			commands.put(command);
			ack = queue.take();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ack;
	}

	public static String waitForCommand() {
		String command = null;
		try {
			command = commands.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return command;
	}
}
