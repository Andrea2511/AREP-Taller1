package co.eci.edu.arep;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpServerTest {

    /**
     * Test 1: Verify the initialization of the server socket
     * @throws IOException
     */
    @Test
    public void testInitializeServerSocket() throws IOException {
        ServerSocket serverSocket = HttpServer.initializeServerSocket(35000);
        assertNotNull(serverSocket, "Server socket should not be null");
        assertTrue(serverSocket.isBound(), "Server socket should be bound to the port");
        serverSocket.close();
    }

    /**
     * Test 2: Verify the generation of a response for a GET request
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testGenerateResponseGet() throws IOException, URISyntaxException {
        String request = "GET /data?name=John&age=25 HTTP/1.1";
        BufferedReader reader = new BufferedReader(new StringReader(request));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServer.generateResponse("/data", reader, out);
        String response = out.toString();
        assertTrue(response.contains("200 OK"), "Response should contain 200 OK");
        assertTrue(response.contains("application/json"), "Response should contain JSON content type");
    }

    /**
     * Test 3: Verify the generation of a file response
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testGenerateFileResponse() throws IOException {
        String filePath = "/index.html"; // Cambiar a la ruta real del archivo
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServer.generateFileResponse(filePath, out);
        String response = out.toString();
        assertTrue(response.contains("HTTP/1.1 200 OK"), "Response should contain 200 OK");
        assertTrue(response.contains("Content-Type: text/html"), "Response should have Content-Type text/html");
    }

    /**
     * Test 4: Verify the generation of error response
     * @throws IOException
     */
    @Test
    public void testGenerateErrorResponse() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServer.sendErrorResponse(out, "404 Not Found");
        String response = out.toString();
        assertTrue(response.contains("404 Not Found"), "Response should contain 404 Not Found");
    }

    /**
     * Test 5: Verify the generation of a file response for an existing file
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void generateFileResponseForExistingFile() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServer.generateFileResponse("/index.html", out);
        String response = out.toString();
        assertTrue(response.contains("200 OK"), "Response should contain 200 OK");
        assertTrue(response.contains("Content-Type: text/html"), "Response should have Content-Type text/html");
    }


}