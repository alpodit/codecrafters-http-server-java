import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

    try (BufferedReader inputStreamReader = new BufferedReader(
                 new InputStreamReader(clientSocket.getInputStream()));
             OutputStream outputStream = clientSocket.getOutputStream();) {
          String[] arg = inputStreamReader.readLine().split(" ");
          System.out.println(Arrays.toString(arg));
          String httpResponse;
          if (arg[1].equals("/")) {
            httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
          } else {
            httpResponse = "HTTP/1.1 404 BAD\r\n\r\n";
          }
          outputStream.write(httpResponse.getBytes(StandardCharsets.UTF_8));
          outputStream.flush();

        }
        catch(IOException e){
          System.err.println("IOException in client handler: " + e.getMessage());
        }
  }
}
