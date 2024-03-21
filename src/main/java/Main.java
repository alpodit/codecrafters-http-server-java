import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
    // CRLF: Carriage Return, Line Feed
    private static final String CRLF = "\r\n";

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible
        // when running tests.
        System.out.println("Logs from your program will appear here!");

        // Uncomment this block to pass the first stage

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(4221);
            while (true) {
                serverSocket.setReuseAddress(true);

                final Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
                System.out.println("accepted new connection");

                Thread thread = new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                thread.start();
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

            if (arg[1].equals("/")) { // 200 OK
                httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
            } else if (arg[1].contains("/echo/")) { // echo
                String echoString = arg[1].substring(6);

                httpResponse = "HTTP/1.1 200 OK\r\n" + bodyResponseString(echoString);
                System.out.println("httpResponse: " + httpResponse);
            }
            // user-agent
            else if (arg[1].equals("/user-agent")) { // HTTP Request içindeki User-Agent header'ındaki bilgiyi döndürür.

                String line;
                String userAgentString = "";
                while ((line = inputStreamReader.readLine()) != null) {
                    if (line.isEmpty()) {
                        break; // Başlık alanları bittiğinde döngüyü sonlandır
                    }
                    // User-Agent başlığını kontrol et
                    if (line.startsWith("User-Agent:")) {
                        userAgentString = line.substring("User-Agent:".length()).trim();
                    }
                }

                httpResponse = "HTTP/1.1 200 OK\r\n" + bodyResponseString(userAgentString);
                System.out.println("httpResponse: " + httpResponse);
            } 
            else if(arg[1].contains("/files/")){
                String filePath = arg[1].substring(7);

                File file = new File(filePath);
                
                if(file.exists()){

                    StringBuilder fileContent = new StringBuilder();

                    try(FileInputStream inputStream = new FileInputStream(file)){
                        int character;
                        while ((character = inputStream.read()) != -1) {
                            fileContent.append((char) character);
                        }
                    }catch(IOException e){
                        System.err.println("IOException in file reading: " + e.getMessage());
                    }

                    httpResponse = "HTTP/1.1 200 OK\r\n" + bodyResponseString(fileContent.toString());
                }
                else{
                    httpResponse = "HTTP/1.1 404 BAD\r\n\r\n";
                }
            }
            else { // 404 BAD
                httpResponse = "HTTP/1.1 404 BAD\r\n\r\n";
            }
            outputStream.write(httpResponse.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

        } catch (IOException e) {
            System.err.println("IOException in client handler: " + e.getMessage());
        }
    }
    private static String bodyResponseString(final String bodyData) {
        return "Content-Type: text/plain" + CRLF + "Content-Length: " + bodyData.length() + CRLF + bodyData + CRLF + CRLF;
    }
}
