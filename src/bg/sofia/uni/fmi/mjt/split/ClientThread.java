package bg.sofia.uni.fmi.mjt.split;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class ClientThread implements Runnable {

    private String username;
    private Socket socket;
    private Map<String, CommandTask> onlineUsers;

    public ClientThread(String username, Socket socket, Map<String, CommandTask> onlineUsers) {
        this.username = username;
        this.socket = socket;
        this.onlineUsers = onlineUsers;
    }

    @Override
    public void run() {
        try {
            CommandTask commandTask = new CommandTask(username, socket
                    , onlineUsers);

            commandTask.commands();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

