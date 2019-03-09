package bg.sofia.uni.fmi.mjt.split;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import java.util.Map;

public class Server {
    private static int PORT = 4444;
    private static Map<String, CommandTask> onlineUsers = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.printf("server is running on localhost:%d%n", PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("A client connected to server " + socket.getInetAddress());

                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String username = br.readLine();
                System.out.println(username + " connected");

                ClientThread client = new ClientThread(username, socket, onlineUsers);
                new Thread(client).start();
            }

        } catch (IOException e) {
            System.out.println("maybe another server is running or port 4444");
        }
    }

}

