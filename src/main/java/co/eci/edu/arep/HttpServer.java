package co.eci.edu.arep;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

    public static void main(String[] args) throws URISyntaxException {

        ServerSocket serverSocket = initializeServerSocket(35000);

        boolean running = true;
        while (running) {
            try {

                Socket clientSocket = acceptClientConnection(serverSocket);

                handleClientRequest(clientSocket);
            } catch (IOException e) {
                System.err.println("Error al aceptar la conexión: " + e.getMessage());
                running = false;
            }
        }
    }

    /**
     * Initialize a  server socket in the given port.
     *
     * @param port port number.
     * @return socket instance.
     */
    static ServerSocket initializeServerSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server init, waiting for connections...");
        } catch (IOException e) {
            System.err.println("Can´t use the port " + port);
            System.exit(1);
        }
        return serverSocket;
    }

    /**
     * Accept a client connection.
     *
     * @param serverSocket server socket instance.
     * @return client socket instance.
     */
    private static Socket acceptClientConnection(ServerSocket serverSocket) throws IOException {
        Socket clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
            System.out.println("Client connected...");
        } catch (IOException e) {
            System.err.println("Failed to accept the connection.");
            System.exit(1);
        }
        return clientSocket;
    }

    /**
     * Handle the client request and send the response.
     *
     * @param clientSocket client socket instance.
     */
    private static void handleClientRequest(Socket clientSocket) throws IOException, URISyntaxException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        // Read the request line
        String requestLine = in.readLine();
        if (requestLine == null) {
            sendErrorResponse(out, "Error: Invalid HTTP request (no request line).");
            return;
        }

        // Process the request line and extract the path
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            sendErrorResponse(out, "Error: invalid HTTP request line.");
            return;
        }

        String path = requestParts[1];

        // Generate the response based on the requested path
        generateResponse(path, in, out);

        // Close the streams and the socket
        out.flush();
        out.close();
        in.close();
        clientSocket.close();
    }

    /**
     * Send an error response to the client.
     *
     * @param out output stream.
     * @param errorMessage error message.
     */
    static void sendErrorResponse(OutputStream out, String errorMessage) throws IOException {
        String errorResponse = "HTTP/1.1 400 Bad Request\r\n"
                + "Content-Type: text/plain\r\n"
                + "\r\n"
                + errorMessage;
        out.write(errorResponse.getBytes());
        out.flush();
    }

    /**
     * Generate the response based on the requested path.
     *
     * @param path requested path.
     * @param in input stream.
     * @param out output stream.
     */
    static void generateResponse(String path, BufferedReader in, OutputStream out) throws IOException, URISyntaxException {
        if (path.startsWith("/data")) {
            handleDataRequest(in, out);
        } else {
            generateFileResponse(path, out);
        }
    }

    /**
     * Handle the data request and send the response to the client.
     * The data request can be a GET request or a POST request.
     *
     * @param in input stream.
     * @param out output stream.
     */
    static void handleDataRequest(BufferedReader in, OutputStream out) throws IOException, URISyntaxException {
        String requestLine = in.readLine();
        if (requestLine == null) {
            sendErrorResponse(out, "Error: Invalid HTTP request (no request line).");
            return;
        }

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            sendErrorResponse(out, "Error: invalid HTTP request line.");
            return;
        }

        String httpMethod = requestParts[0];

        if (httpMethod.equals("POST")) {
            String response = handlePostRequest(in);
            out.write(response.getBytes());
        } else {
            String response = handleGetRequest(new URI(requestParts[1]).getQuery());
            out.write(response.getBytes());
        }
    }

    /**
     * Handle a POST request and send the response to the client.
     *
     * @param in input stream.
     * @return response string.
     */
    static String handlePostRequest(BufferedReader in) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        String line;

        // Read the request headers
        while ((line = in.readLine()) != null && !line.isEmpty()) { }

        // Read the request body
        while ((line = in.readLine()) != null) {
            requestBody.append(line);
        }

        // Process the request body
        Map<String, String> data = processPostData(requestBody.toString());

        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: application/json\r\n"
                + "\r\n"
                + "{\"name\": \"" + data.get("name") + "\", \"age\": \"" + data.get("age") + "\", \"animal\": \"" + data.get("animal") + "\"}";
    }

    /**
     * Process the POST data and return a map with the key-value pairs.
     *
     * @param requestBody request body.
     * @return map with the key-value pairs.
     */
    static Map<String, String> processPostData(String requestBody) {
        Map<String, String> data = new HashMap<>();
        for (String param : requestBody.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length > 1) {
                data.put(keyValue[0], keyValue[1]);
            }
        }
        return data;
    }

    /**
     * Handle a GET request and send the response to the client.
     *
     * @param query query string.
     * @return response string.
     */
    private static String handleGetRequest(String query) {

        String name = "unknown";
        int age = 30;
        String favoriteAnimal = "rabbit";

        if (query != null && !query.isEmpty()) {
            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length > 1) {
                    if (keyValue[0].equals("name")) name = keyValue[1];
                    if (keyValue[0].equals("age")) age = Integer.parseInt(keyValue[1]);
                    if (keyValue[0].equals("favoriteAnimal")) favoriteAnimal = keyValue[1];
                }
            }
        }

        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: application/json\r\n"
                + "\r\n"
                + "{\"name\": \"" + name + "\", \"age\":" + age + ", \"favoriteAnimal\":\"" + favoriteAnimal + "\"}";
    }

    /**
     * Generate a file response based on the requested path.
     *
     * @param path requested path.
     * @param out output stream.
     */
    static void generateFileResponse(String path, OutputStream out) throws IOException {
        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        String basePath = "src/main/resources";
        String fullPath = basePath + path;
        File file = new File(fullPath);

        // verify if the file exists and is readable
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            // Return a 404 error response perzonalized
            String errorPagePath = basePath + "/error.html";
            File errorFile = new File(errorPagePath);

            if (errorFile.exists() && errorFile.isFile() && errorFile.canRead()) {
                // iF the error page exists, send it
                String contentType = getContentType("error.html");
                String header = "HTTP/1.1 404 Not Found\r\n"
                        + "Content-Type: " + contentType + "\r\n"
                        + "Content-Length: " + errorFile.length() + "\r\n"
                        + "\r\n";
                out.write(header.getBytes());

                try (FileInputStream fileInputStream = new FileInputStream(errorFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                // If the error page does not exist, send a default error message
                sendErrorResponse(out, "Error: File not found.");
            }
            return;
        }

        // Send the file
        String contentType = getContentType(path);
        String header = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + file.length() + "\r\n"
                + "\r\n";
        out.write(header.getBytes());

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Get the content type based on the file extension.
     *
     * @param filePath file path.
     * @return content type.
     */
    private static String getContentType(String filePath) {
        String ext = filePath.substring(filePath.lastIndexOf('.') + 1);

        return switch (ext) {
            case "html" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> "application/octet-stream";
        };
    }
}
