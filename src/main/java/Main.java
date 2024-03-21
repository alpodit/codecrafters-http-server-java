import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  private static final String CRLF = "\r\n";
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    
    try {
      serverSocket = new ServerSocket(4221);
      while (true) {
        serverSocket.setReuseAddress(true);
        clientSocket =
            serverSocket.accept(); // Wait for connection from client.
        System.out.println("accepted new connection");
        handleClient(clientSocket);
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
  private static void handleClient(final Socket clientSocket) {
    try (var outputStream = clientSocket.getOutputStream()) {
      outputStream.write(encodeHttpResponse("HTTP/1.1 200 OK"));
      outputStream.flush();
    } catch (IOException e) {
      System.err.println("IOException in client handler: " + e.getMessage());

    }
  }
  private static byte[] encodeHttpResponse(String value) {
    if (value == null) {
      return String.format("-1%s%s", CRLF, CRLF).getBytes();
    }

    return String.format("%s%s%s", value, CRLF, CRLF).getBytes();
  }
}
