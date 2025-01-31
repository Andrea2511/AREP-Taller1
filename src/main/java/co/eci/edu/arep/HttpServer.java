package co.eci.edu.arep;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    private static final int PORT = 8080;
    private static final String WEB_ROOT = "src/main/resources";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleRequest(clientSocket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        String requestLine = in.readLine();
        if (requestLine == null) return;
        System.out.println("Request: " + requestLine);

        String[] tokens = requestLine.split(" ");
        if (tokens.length < 2) return;

        String method = tokens[0];
        String path = tokens[1];

        if (path.equals("/hello")) {
            handleHelloRequest(clientSocket, out);
        } else if (path.equals("/hellopost")) {
            handlePostRequest(in, out);
        } else {
            serveStaticFile(path, out);
        }
    }

    private static void handleHelloRequest(Socket clientSocket, OutputStream out) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Hello, World!";
        out.write(response.getBytes());
        out.flush();
    }

    private static void handlePostRequest(BufferedReader in, OutputStream out) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "POST request received";
        out.write(response.getBytes());
        out.flush();
    }

    private static void serveStaticFile(String path, OutputStream out) throws IOException {

        if (path.equals("/")) path = "/index.html";
        System.out.println("Serving static file: " + path);
        File file = new File(WEB_ROOT, path);
        System.out.println(file.getAbsolutePath());

        if (!file.exists() || file.isDirectory()) {
            String response = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "404 Not Found";
            out.write(response.getBytes());
        } else {
            String contentType = getContentType(file);
            byte[] fileData = Files.readAllBytes(file.toPath());

            String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + fileData.length + "\r\n" +
                    "\r\n";
            out.write(responseHeaders.getBytes());
            out.write(fileData);
        }
        out.flush();
    }

    private static String getContentType(File file) {
        Map<String, String> mimeTypes = new HashMap<>();
        mimeTypes.put("html", "text/html");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "application/javascript");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("png", "image/png");

        String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1);
        return mimeTypes.getOrDefault(ext, "application/octet-stream");
    }
}
