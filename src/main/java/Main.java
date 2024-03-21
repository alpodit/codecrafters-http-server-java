import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Main {
    // CRLF: Carriage Return, Line Feed
    private static final String CRLF = "\r\n";
    private static String directory = "";

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible
        // when running tests.
        System.out.println("Logs from your program will appear here!");


        HashMap<String, String> arguments = new HashMap<>();
        for (int i = 0; i < args.length - 1; i += 2) {
        arguments.put(args[i].substring(2), args[i + 1]);
        }
        if (arguments.containsKey("directory")) {
            directory = arguments.get("directory");
        }

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

        try (BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream();) {

            
            String[] arg = inputStreamReader.readLine().split(" ");

            String httpResponse;
            if(arg[0].equals("POST") && arg[1].contains("/files/")){
                String filePath = directory + arg[1].substring(7);
                File file = new File(filePath);
                if(file.exists()){
                    httpResponse = "HTTP/1.1 400 BAD\r\n\r\n";
                }
                else{

                    FileWriter writer = new FileWriter(file);
                    // İstek vücut kısmını oku


                    StringBuilder requestBody = new StringBuilder();

                    String line;

                    while ((line = inputStreamReader.readLine()) != null && !line.isEmpty()) {
                        requestBody.append(line).append("\r\n");
                        System.out.println(line);
                    }
                    

                    String bodyContent = inputStreamReader.read();;

                    writer.write(bodyContent);
                    writer.close();


                    httpResponse = "HTTP/1.1 201 OK\r\n\r\n";
                }
            }
            else if (arg[1].equals("/")) { // 200 OK
                httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
            } else if (arg[1].contains("/echo/")) { // echo
                String echoString = arg[1].substring(6);

                httpResponse = ResponseString(echoString,"text/plain", 200);
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

                httpResponse = ResponseString(userAgentString,"text/plain", 200);
                System.out.println("httpResponse: " + httpResponse);
            } 
            else if(arg[1].contains("/files/")){
                String filePath = directory + arg[1].substring(7);

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

                    httpResponse = ResponseString(fileContent.toString(),"application/octet-stream", 200);
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
    private static String ResponseString(final String bodyData, final String contentType, int responseCode) {

        String codeDescString = responseCode == 404 ? "BAD" : "OK";

        String httpResponse = "HTTP/1.1 " + responseCode + " " + codeDescString + CRLF;

        return httpResponse + "Content-Type: "+ contentType + CRLF + "Content-Length: " + bodyData.length() + CRLF + CRLF + bodyData + CRLF + CRLF;
    }
}
