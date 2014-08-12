package vferries;

import net.codestory.http.WebServer;

public class RevealServer {
	public static void main(String[] args) {
		new WebServer(routes -> routes.
			    get("/init", "12").
			    get("/next", "Next").
			    get("/previous", "Previous")
			).start();
	}
}
