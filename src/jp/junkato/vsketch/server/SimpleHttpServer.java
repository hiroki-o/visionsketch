package jp.junkato.vsketch.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Simple HTTP server.
 * 
 * @author Jun KATO
 */
public class SimpleHttpServer implements HttpHandler {
	public static final int NUM_BACKLOG = 5;
	private HttpServer server;
	private DefaultFileHandler defaultFileHandler;
	private boolean isUserShutdownEnabled = false;

	public static void main(String[] args) throws IOException {
		new SimpleHttpServer().start();
	}

	public SimpleHttpServer() throws IOException {
		this(8080);
	}

	public SimpleHttpServer(int port) throws IOException {

		// ParameterFilter filter = new ParameterFilter();
		server = HttpServer.create(new InetSocketAddress(port), NUM_BACKLOG);

		// Default file handler
		defaultFileHandler = new DefaultFileHandler("public");

		server.setExecutor(null);
		server.createContext("/", defaultFileHandler);
		server.createContext("/system", this);
	}

	public HttpContext createContext(String path, HttpHandler handler) {
		if (path == null || handler == null) {
			return null;
		}
		return server.createContext(path, handler);
	}

	public boolean removeContext(HttpContext context) {
		if (context == null) {
			return false;
		}
		server.removeContext(context);
		return true;
	}

	public boolean removeContext(String path) {
		if (path == null) {
			return false;
		}
		try {
			server.removeContext(path);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		System.out.println(path);

		// Shutdown the server.
		if (path.equals("/system/off")) {
			if (isUserShutdownEnabled()) {
				exchange.sendResponseHeaders(200, 0);
				exchange.close();
				stop();
			} else {
				exchange.sendResponseHeaders(403, 0);
				exchange.close();
			}
			return;

		// Reload the setting.
		} else if (path.equals("/system/reload")) {
			this.defaultFileHandler.loadFiles();
			exchange.sendResponseHeaders(200, 0);
			exchange.close();
			return;
		}

		exchange.sendResponseHeaders(404, 0);
		exchange.close();
	}

	public boolean isUserShutdownEnabled() {
		return isUserShutdownEnabled;
	}

	public void setUserShutdownEnabled(boolean isUserShutdownEnabled) {
		this.isUserShutdownEnabled = isUserShutdownEnabled;
	}

	public void start() {
		int port = server.getAddress().getPort();
		server.start();
		System.out.println("--- Started HTTP server at port " + port);
	}

	public void stop() {
		int port = server.getAddress().getPort();
		server.stop(0);
		System.out.println("--- Stopped HTTP server at port " + port);
	}
}
