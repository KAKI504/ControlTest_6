import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class SimpleServer {
    private final HttpServer server;
    private final Map<String, RouteHandler> routes = new HashMap<>();

    public SimpleServer(String host, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/", new MainHandler());
    }

    protected void registerGet(String route, RouteHandler handler) {
        routes.put("GET " + route, handler);
    }

    protected void registerPost(String route, RouteHandler handler) {
        routes.put("POST " + route, handler);
    }

    public void start() {
        server.setExecutor(null);
        server.start();
    }

    protected void respond404(HttpExchange exchange) throws IOException {
        String response = "404 Not Found";
        exchange.sendResponseHeaders(404, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    protected interface RouteHandler {
        void handle(HttpExchange exchange) throws IOException;
    }

    private class MainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            String key = method + " " + path;
            RouteHandler handler = routes.get(key);

            if (handler == null) {
                for (Map.Entry<String, RouteHandler> entry : routes.entrySet()) {
                    String routeKey = entry.getKey();

                    if (routeKey.endsWith("/*")) {
                        String prefix = routeKey.substring(0, routeKey.length() - 1);
                        if (key.startsWith(prefix)) {
                            handler = entry.getValue();
                            break;
                        }
                    }
                }
            }

            if (handler != null) {
                try {
                    handler.handle(exchange);
                } catch (Exception e) {
                    e.printStackTrace();

                    String response = "500 Internal Server Error: " + e.getMessage();
                    exchange.sendResponseHeaders(500, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            } else {
                respond404(exchange);
            }
        }
    }
}