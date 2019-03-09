package bg.sofia.uni.fmi.mjt.split;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final static int PORT = 4444;
    private final static String HOST = "localhost";
    private String username;
    private PrintWriter pw;
    private BufferedReader br;


    public static void main(String[] args) {
        new Client().run();
    }

    public void run() {
        System.out.println("Welcome to SplitWise");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String input = scanner.nextLine();
                String[] tokens = input.split(" ");
                String command = tokens[0];
                if (command.equals("connect")) {

                    String username = tokens[1];
                    this.username = username;
                    connect(HOST, PORT, username);
                } else if (command.equals("register")) {
                    if (tokens.length != 3) {
                        System.out.println("Wrong input! You must register with a username and a password!");
                    } else {
                        pw.println(tokens[0] + " " + " " + tokens[1] + " " + tokens[2]);
                    }
                } else if (command.equals("login")) {
                    if (tokens.length != 3) {
                        System.out.println("Wrong input! You must log in with your username and password!");
                    } else {
                        pw.println(tokens[0] + " " + " " + tokens[1] + " " + tokens[2]);
                    }
                } else if (command.equals("add-friend")) {
                    pw.println(tokens[0] + " " + tokens[1]);
                } else if (command.equals("create-group")) {
                    StringBuilder members = new StringBuilder();
                    for (int i = 2; i < tokens.length; i++) {
                        members.append(tokens[i]);
                        members.append(" ");
                    }
                    String[] membersStr = members.toString().split(" ");
                    pw.println(tokens[0] + " " + tokens[1] + " " + String.join(" ", membersStr));
                } else if (command.equals("split")) {
                    StringBuilder reason = new StringBuilder();
                    for (int i = 3; i < tokens.length; i++) {
                        reason.append(tokens[i]);
                        reason.append(" ");
                    }
                    String[] reasonStr = reason.toString().split(" ");
                    pw.println(tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + String.join(" ", reasonStr));

                } else if (command.equals("group-split")) {
                    StringBuilder reason = new StringBuilder();
                    for (int i = 3; i < tokens.length; i++) {
                        reason.append(tokens[i]);
                        reason.append(" ");
                    }
                    String reasonStr = reason.toString();
                    pw.println(tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + String.join(" ", reasonStr));

                } else if (command.equals("paid")) {
                    StringBuilder reason = new StringBuilder();
                    for (int i = 3; i < tokens.length; i++) {
                        reason.append(tokens[i]);
                        reason.append(" ");
                    }
                    String[] reasonStr = reason.toString().split(" ");
                    pw.println(tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + String.join(" ", reasonStr));
                } else if (command.equals("get-status")) {
                    pw.println(tokens[0] + " " + username);
                } else if (command.equals("paid-group")) {
                    StringBuilder reason = new StringBuilder();
                    for (int i = 4; i < tokens.length; i++) {
                        reason.append(tokens[i]);
                        reason.append(" ");
                    }
                    String[] reasonStr = reason.toString().split(" ");

                    pw.println(tokens[0] + " " + tokens[1] + " " + tokens[2] + " " + tokens[3] + " " + String.join(" ",
                            reasonStr));

                } else if (command.equals("get-history")) {
                    pw.println(tokens[0] + " " + username);
                } else {
                    System.out.println("Undefined command!");
                    pw.println(input);
                }
            }
        }
    }


    private void connect(String host, int port, String username) {
        try {
            Socket socket = new Socket(HOST, PORT);
            pw = new PrintWriter(socket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("You are successfully connected");
            pw.println(username);

            new Thread(() -> {

                try {
                    while (true) {
                        String serverMsg = br.readLine();
                        System.out.println(serverMsg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            System.out.println("=> cannot connect to the server, make sure that the server is " +
                    "started");
        }
    }
}




