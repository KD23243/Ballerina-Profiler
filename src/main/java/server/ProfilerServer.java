package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static server.ServerFrontEnd.getSiteData;

public class ProfilerServer {

    public static void initServer() throws IOException {
        int port = 2324;

        String content = readData();
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println(" ○ Output: " + "http://localhost:" + port); // Print the number of instrumented files

        while (true) {
            // accept incoming client connections
            Socket clientSocket = serverSocket.accept();

            // create a reader to read data from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // create a writer to send data back to the client
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // read the HTTP request header
            String request = in.readLine();

            if (request.contains("/off")) {
                break;
            } else {

                // check if it's a GET request
                if (request.startsWith("GET")) {
                    // write the HTTP response header
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/html");
                    out.println("Connection: close");
                    out.println();

                    String html = getSiteData(content);
                    out.println(html);

                }
            }
            // close the client connection
            clientSocket.close();

        }

    }

    private static String readData() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("performance_report.json"));
        StringBuilder contents = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            contents.append(line);
        }
        reader.close();

        return String.valueOf(contents);
    }
}
