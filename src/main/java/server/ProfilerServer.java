package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static server.ServerFrontEnd.getSiteData;

public class ProfilerServer {

    public static final String ANSI_YELLOW = "\033[1;38;2;255;255;0m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void initializeServer() throws IOException {
        int port = 2324;
        String content = readData();
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println(" ○ Output: " + ANSI_YELLOW + "http://localhost:" + port + ANSI_RESET);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String request = in.readLine();
            if (request.contains("/terminate")) {
                break;
            } else {
                if (request.startsWith("GET")) {
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/html");
                    out.println("Connection: close");
                    out.println();
                    String html = getSiteData(content);
                    out.println(html);
                }
            }
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
