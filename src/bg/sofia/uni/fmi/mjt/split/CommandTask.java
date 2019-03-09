package bg.sofia.uni.fmi.mjt.split;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class CommandTask {

    private String username;
    private Socket socket;
    private Map<String, CommandTask> onlineUsers;


    public CommandTask(String username, Socket socket, Map<String, CommandTask> onlineUsers) {
        this.username = username;
        this.socket = socket;
        this.onlineUsers = onlineUsers;
    }

    private void registerUser(String username, String password, PrintWriter pwClient) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter("usersFile.txt", true))) {
            pw.println(username + " " + password);
            pw.flush();
            File usernameOwesTo = new File(username + "_owes_to.txt");
            usernameOwesTo.createNewFile();
            File oweToUsername = new File("who_owes_to_" + username + ".txt");
            oweToUsername.createNewFile();
            File payments = new File(username + "_payments.txt");
            payments.createNewFile();
            System.out.println(username + " is registered.");
            pwClient.println("You are successfully registered.");
            pwClient.flush();
        }
    }


    private boolean areUsernameAndPasswordMatching(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader("usersFile.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (tokens[0].equals(username) && tokens[1].equals(password)) {
                    System.out.println(username + " logged in!");
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File usersFile.txt is missing.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isRegistered(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader("usersFile.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                if (tokens[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("A problem occured while checking if " + username + " is registered.");

        }
        return false;
    }

    private boolean areAlreadyFriends(String username1, String username2) {
        boolean areAlreadyFriends = false;
        try (BufferedReader br = new BufferedReader(new FileReader("friendList.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("<->");
                if ((tokens[0].equals(username1) && tokens[1].equals(username2)) || (tokens[0].equals(username2) && tokens[1].equals(username1))) {
                    areAlreadyFriends = true;
                }
            }
        } catch (FileNotFoundException e1) {
            System.out.println("File friendList.txt doesn't exist!");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return areAlreadyFriends;
    }

    private void addToFriendList(String username1, String username2) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("friendList.txt"))) {
            if (!areAlreadyFriends(username1, username2)) {
                pw.println(username1 + "<->" + username2);
                pw.flush();
            }
        } catch (FileNotFoundException e1) {
            System.out.println("File friendList.txt doesn't exist!");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void createGroup(String creator, String groupName, String[] users) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter("groups.txt", true))) {
            pw.write(groupName + " " + creator + " ");
            for (String user : users) {
                pw.write(user + " ");
            }
            pw.write(System.lineSeparator());
        }
    }

    private void replaceOldAmount(String fileName, String name, String oldAmount, String newAmount) {
        File oldFile = new File(fileName);
        File temp = new File("temp.txt");
        String lineToChange = "";
        try (BufferedReader br = new BufferedReader(new FileReader(oldFile));
             PrintWriter pw = new PrintWriter(new FileWriter(temp, true))) {
            String line;
            while ((line = br.readLine()) != null) {
                if ((line.contains(name) && line.contains("Friend")) || (line.contains(name) && line.contains("Group"))) {
                    lineToChange = line;
                    continue;
                }
                pw.write(line + System.lineSeparator());
                pw.flush();
            }
            String finalLine = lineToChange.replaceAll(oldAmount, newAmount);
            pw.println(finalLine);
            pw.flush();

        } catch (IOException e) {
            System.out.println("A problem occured during updating the amount of a paying.");

        } finally {

            oldFile.delete();
            temp.renameTo(oldFile);
        }

    }

    private void replaceParticularPayingFromGroup(String fileName, String group, String friend,
                                                  String newAmount) {
        File oldFile = new File(fileName);
        File temp = new File("temp.txt");
        String lineToChange = "";
        try (BufferedReader br = new BufferedReader(new FileReader(oldFile));
             PrintWriter pw = new PrintWriter(new FileWriter(temp, true))) {
            String line;
            while ((line = br.readLine()) != null) {
                if ((line.contains(group) && line.contains("Group"))) {
                    lineToChange = line;
                    continue;
                }
                pw.write(line + System.lineSeparator());
                pw.flush();
            }

            StringBuilder help = new StringBuilder();
            String[] changeLine = lineToChange.split(" ");
            for (int i = 0; i < changeLine.length; i++) {
                if (changeLine[i].equals(friend)) {
                    help.append(changeLine[i]);
                    help.append(" ");
                    changeLine[i + 1] = newAmount;
                    help.append(newAmount);
                    help.append(" ");
                    i++;
                } else {
                    help.append(changeLine[i]);
                    help.append(" ");
                }

            }
            String finalLine = help.toString();
            pw.println(finalLine);
            pw.flush();

        } catch (IOException e) {
            System.out.println("A problem occured during updating the amount of a paying for a group by particular " +
                    "person.");
        } finally {

            oldFile.delete();
            temp.renameTo(oldFile);
        }
    }

    private void helpSplit(String file, String paid, double amount, String owes) {
        boolean isAlreadyOwing = false;
        double prevAmount = 0.0;
        String prevAmountStr = "";
        String owes1 = "";
        String newAmount = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file));
             PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            String line1;
            while ((line1 = br.readLine()) != null) {
                String[] tokens = line1.split(" ");
                if (tokens[0].equals("Friend")) {
                    if (tokens[1].equals(owes)) {
                        isAlreadyOwing = true;
                        owes1 = tokens[1];
                        prevAmountStr = tokens[2];
                        prevAmount += Double.parseDouble(prevAmountStr);
                        newAmount = String.valueOf(prevAmount += (amount / 2));
                    }
                }
            }
            if (!isAlreadyOwing) {
                pw.println("Friend " + owes + " " + (amount / 2));
                pw.flush();
            }
        } catch (IOException e) {
            System.out.println("A problem occured while trying to split the amount of money");
        }
        if (isAlreadyOwing) {
            replaceOldAmount(file, owes1, prevAmountStr, newAmount);
        }
    }

    private void split(String paid, double amount, String owes) {
        helpSplit("who_owes_to_" + paid + ".txt", paid, amount, owes);
        helpSplit(owes + "_owes_to.txt", owes, amount, paid);
    }


    private String[] extractGroupMembersWithoutPaid(String paid, String groupName) {
        StringBuilder members = new StringBuilder();
        try (BufferedReader brGroup = new BufferedReader(new FileReader("groups.txt"))) {

            String line;
            while ((line = brGroup.readLine()) != null) {
                String[] tokens = line.split(" ");

                if (tokens[0].equals(groupName)) {
                    for (int i = 1; i < tokens.length; i++) {
                        if (!tokens[i].equals(paid)) {
                            members.append(tokens[i]);
                            members.append(" ");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to get the members of group " + groupName);
        }
        return members.toString().split(" ");
    }

    private void splitGroup(String paid, double amount, String groupName) {
        boolean isWaitingFromGroupAlready = false;
        boolean isOwingGroupAlready = false;
        double prevAmount1 = 0.0;

        String prevAmountStr1 = "";

        String group1 = "";

        String newAmount1 = "";

        try (BufferedReader br1 = new BufferedReader(new FileReader("who_owes_to_" + paid + ".txt"));
             PrintWriter pw1 = new PrintWriter(new FileWriter("who_owes_to_" + paid + ".txt", true));
             BufferedReader groupReader = new BufferedReader(new FileReader("groups.txt"))) {

            String line1;
            while ((line1 = br1.readLine()) != null) {
                String[] tokens = line1.split(" ");
                if (tokens[0].equals("Group")) {
                    if (tokens[1].equals(groupName)) {
                        isWaitingFromGroupAlready = true;
                        group1 = tokens[1];
                        prevAmountStr1 = tokens[3];
                        prevAmount1 += Double.parseDouble(prevAmountStr1);
                        newAmount1 = String.valueOf(prevAmount1 += (amount / (numberOfGroupMembers(group1))));
                    }
                }
            }
            if (!isWaitingFromGroupAlready) {
                String[] members = extractGroupMembersWithoutPaid(paid, groupName);
                pw1.write("Group " + groupName + " ");
                for (String member : members) {
                    pw1.write(member + " " + (amount / (numberOfGroupMembers(groupName))) + " ");
                    pw1.flush();
                }
            }

            pw1.write(System.lineSeparator());
            pw1.flush();

            String lineGroup;
            while ((lineGroup = groupReader.readLine()) != null) {
                String[] tokens = lineGroup.split(" ");
                if (tokens[0].equals(groupName)) {
                    String[] members = extractGroupMembersWithoutPaid(paid, groupName);
                    for (String member : members) {
                        double prevAmount2 = 0.0;
                        String prevAmountStr2 = "";
                        String group2 = "";
                        String newAmount2 = "";
                        String lineMember;
                        try (BufferedReader brMember = new BufferedReader(new FileReader(member + "_owes_to" +
                                ".txt"));
                             PrintWriter pwMember = new PrintWriter(new FileWriter(member + "_owes_to.txt", true))) {

                            while ((lineMember = brMember.readLine()) != null) {
                                String[] tokensMember = lineMember.split(" ");
                                if (tokensMember[0].equals("Group")) {
                                    if (tokensMember[1].equals(groupName)) {
                                        isOwingGroupAlready = true;
                                        group2 = tokensMember[1];
                                        prevAmountStr2 = tokensMember[3];
                                        prevAmount2 += Double.parseDouble(prevAmountStr2);
                                        newAmount2 =
                                                String.valueOf(prevAmount2 += (amount / (numberOfGroupMembers
                                                        (group2))));
                                    }

                                }
                            }
                            if (!isOwingGroupAlready) {
                                pwMember.println("Group " + groupName + " " + paid + " " + (amount / (numberOfGroupMembers(groupName))));
                                pwMember.write(System.lineSeparator());
                                pwMember.flush();
                            }
                        }
                        if (isOwingGroupAlready) {
                            replaceOldAmount(member + "_owes_to.txt", group2, prevAmountStr2, newAmount2);
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("A problem occured while trying to split the amount of money for group " + groupName);
        }

        if (isWaitingFromGroupAlready) {
            replaceOldAmount("who_owes_to_" + paid + ".txt", group1, prevAmountStr1, newAmount1);
        }
    }

    private void paid(String owedTo, double amount, String paid) {
        boolean isAlreadyOwing1 = false;
        boolean isAlreadyOwing2 = false;
        double prevAmount1 = 0.0;
        double prevAmount2 = 0.0;
        String prevAmountStr1 = "";
        String prevAmountStr2 = "";
        String paid1 = "";
        String paid2 = "";
        String newAmount1 = "";
        String newAmount2 = "";
        try (BufferedReader br1 = new BufferedReader(new FileReader("who_owes_to_" + owedTo + ".txt"));
             BufferedReader br2 = new BufferedReader(new FileReader(paid + "_owes_to.txt"));
             PrintWriter pw = new PrintWriter(new FileWriter(paid + "_payments.txt", true))) {

            String line1;
            while ((line1 = br1.readLine()) != null) {
                String[] tokens = line1.split(" ");
                if (tokens[0].equals("Friend")) {
                    if (tokens[1].equals(paid)) {
                        isAlreadyOwing1 = true;
                        paid1 = tokens[1];
                        prevAmountStr1 = tokens[2];
                        prevAmount1 += Double.parseDouble(prevAmountStr1);
                        newAmount1 = String.valueOf(prevAmount1 -= amount);
                    }
                }
            }
            String line2;
            while ((line2 = br2.readLine()) != null) {
                String[] tokens = line2.split(" ");
                if (tokens[0].equals("Friend")) {
                    if (tokens[1].equals(owedTo)) {
                        isAlreadyOwing2 = true;
                        paid2 = tokens[1];
                        prevAmountStr2 = tokens[2];
                        prevAmount2 += Double.parseDouble(prevAmountStr2);
                        newAmount2 = String.valueOf(prevAmount2 -= amount);

                    }
                }
            }
            pw.println("Paid " + amount + " to " + owedTo);
            pw.flush();
        } catch (IOException e) {
            System.out.println("A problem occured while accepting a payment");
        }

        if (isAlreadyOwing1) {
            replaceOldAmount("who_owes_to_" + owedTo + ".txt", paid1, prevAmountStr1, newAmount1);
        }

        if (isAlreadyOwing2) {
            replaceOldAmount(paid + "_owes_to.txt", paid2, prevAmountStr2, newAmount2);
        }
    }


    private void groupPaid(String owedTo, double amount, String groupName, String paid) {
        boolean isWaitingFromGroupAlready = false;
        boolean isOwingGroupAlready = false;
        double prevAmount1 = 0.0;
        double prevAmount2 = 0.0;
        String prevAmountStr1 = "";
        String prevAmountStr2 = "";
        String group1 = "";
        String group2 = "";
        String newAmount1 = "";
        String newAmount2 = "";
        try (BufferedReader br1 = new BufferedReader(new FileReader("who_owes_to_" + owedTo + ".txt"));
             BufferedReader br2 = new BufferedReader(new FileReader(paid + "_owes_to.txt"));
             PrintWriter pw = new PrintWriter(new FileWriter(paid + "_payments.txt", true))) {

            String line1;
            while ((line1 = br1.readLine()) != null) {
                String[] tokens = line1.split(" ");
                if (tokens[0].equals("Group")) {
                    if (tokens[1].equals(groupName)) {
                        isWaitingFromGroupAlready = true;
                        group1 = tokens[1];
                        for (int i = 2; i < tokens.length; i++) {
                            if (tokens[i].equals(paid)) {
                                prevAmountStr1 = tokens[i + 1];
                                prevAmount1 += Double.parseDouble(prevAmountStr1);
                                newAmount1 = String.valueOf(prevAmount1 -= amount);
                            }
                        }
                    }
                }
            }

            String line2;
            while ((line2 = br2.readLine()) != null) {
                String[] tokens = line2.split(" ");
                if (tokens[0].equals("Group")) {
                    if (tokens[1].equals(groupName)) {
                        isOwingGroupAlready = true;
                        group2 = tokens[1];
                        if (tokens[2].equals(owedTo)) {
                            isOwingGroupAlready = true;
                            prevAmountStr2 = tokens[3];
                            prevAmount2 += Double.parseDouble(prevAmountStr2);
                            newAmount2 = String.valueOf(prevAmount2 -= amount);
                        }
                    }
                }
            }
            pw.println("Paid " + amount + " to " + owedTo + " for group " + groupName);
            pw.flush();
        } catch (IOException e) {
            System.out.println("Unable to split amount to group " + groupName);
        }

        if (isWaitingFromGroupAlready) {
            replaceParticularPayingFromGroup("who_owes_to_" + owedTo + ".txt", group1, paid, newAmount1);
        }

        if (isOwingGroupAlready) {
            replaceOldAmount(paid + "_owes_to.txt", group2, prevAmountStr1, newAmount2);
        }
    }

    private int numberOfGroupMembers(String groupName) {
        int membersCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("groups.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(groupName)) {
                    String[] tokens = line.split(" ");
                    membersCount = tokens.length - 1;
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot get the number of members of group " + groupName);
        }
        return membersCount;
    }

    private void getStatus(String username, PrintWriter pw) {
        try (BufferedReader br1 = new BufferedReader(new FileReader(username + "_owes_to.txt"));
             BufferedReader br2 = new BufferedReader(new FileReader("who_owes_to_" + username + ".txt"))) {
            String line1;
            while ((line1 = br1.readLine()) != null) {
                String[] tokens = line1.split(" ");
                if (tokens[0].equals("Group")) {
                    pw.println("You owe " + tokens[3] + "lv to " + tokens[2] + " from group " + tokens[1] + ".");
                } else if ((tokens[0].equals("Friends"))) {
                    pw.println("You owe " + tokens[2] + "lv to " + tokens[1] + ".");
                    pw.flush();
                }
            }
            String line2;
            while ((line2 = br2.readLine()) != null) {
                String[] tokens = line2.split(" ");
                if (tokens[0].equals("Friend")) {
                    pw.println(tokens[1] + " owes you " + tokens[2] + "lv.");
                    pw.flush();
                } else if (tokens[0].equals("Group")) {
                    for (int i = 2; i < tokens.length - 1; i += 2) {
                        pw.println(tokens[i] + " owes you " + tokens[i + 1] + "lv from group " + tokens[1] + ".");
                        pw.flush();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot get status of " + username);
        }
    }

    private void getHistory(String username, PrintWriter pw) {
        try (BufferedReader br = new BufferedReader(new FileReader(username + "_payments.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                pw.println(line);
                pw.flush();
            }
        } catch (IOException e) {
            System.out.println("A problem occured while trying to display paying history for " + username);
        }
    }


    public void commands() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            while (true) {
                String commandInput = br.readLine();

                if (commandInput != null) {
                    String[] tokens = commandInput.split("\\s+");
                    String cmd = tokens[0];
                    if (cmd.equals("login")) {
                        if (onlineUsers.containsKey(tokens[1])) {
                            pw.println("You are already logged in!");
                            pw.flush();
                        } else if (isRegistered(tokens[1]) && areUsernameAndPasswordMatching(tokens[1], tokens[2]) && !onlineUsers.containsKey(tokens[1])) {
                            onlineUsers.put(tokens[1], this);
                            pw.println("You logged in successfully!");
                            pw.flush();
                        } else {
                            pw.println("You are not registered or username doesn't match password! Please try again.");
                            pw.flush();
                        }
                    }
                    if (cmd.equals("register")) {
                        if (!isRegistered(tokens[1])) {
                            registerUser(tokens[1], tokens[2], pw);
                            onlineUsers.put(tokens[1], this);
                        } else {
                            pw.println("You are already registered! Please log in or register with another username.");
                            pw.flush();
                        }
                    }

                    if (cmd.equals("add-friend")) {
                        if (!isRegistered(tokens[1])) {
                            pw.println("There is no registered user with username " + tokens[1] + ". Please add only " +
                                    "registered users.");
                            pw.flush();
                        } else if (areAlreadyFriends(username, tokens[1])) {
                            pw.println("You are already friends with " + tokens[1] + ". Please add another friend or " +
                                    "continue with different command.");
                            pw.flush();
                        } else {
                            pw.println("You added " + tokens[1] + " to your friendlist.");
                            pw.flush();
                            addToFriendList(username, tokens[1]);
                        }
                    }
                    if (cmd.equals("create-group")) {
                        boolean areValidMembers = true;
                        StringBuilder members = new StringBuilder();
                        for (int i = 2; i < tokens.length; i++) {
                            if (!isRegistered(tokens[i])) {
                                pw.println("There is no registered user with username " + tokens[i] + ". Please try " +
                                        "again.");
                                pw.flush();
                                areValidMembers = false;
                            } else {
                                members.append(tokens[i]);
                                members.append(" ");
                            }
                        }
                        if (members.length() < 2) {
                            pw.println("A group cannot have less than 3 people in it! Please try again");
                            pw.flush();
                            areValidMembers = false;
                        }
                        if (areValidMembers) {
                            String[] membersString = members.toString().split(" ");
                            createGroup(username, tokens[1], membersString);
                            pw.println("You created the group " + tokens[1]);
                            pw.flush();
                        }
                    }
                    if (cmd.equals("split")) {
                        try {
                            Double.parseDouble(tokens[1]);
                        } catch (NumberFormatException e) {
                            pw.println(tokens[1] + " is not a valid float number. Please try again.");
                            pw.flush();
                        }
                        if (!isRegistered(tokens[2])) {
                            pw.println("There is no registered user with username " + tokens[2] + ". Please try again" +
                                    ".");
                            pw.flush();
                        } else if (!areAlreadyFriends(username, tokens[2])) {
                            pw.println("You can only split between friends or members of group. Please try again with" +
                                    " to split with a friend or continue with a different command");
                            pw.flush();
                        } else {
                            split(username, Double.parseDouble(tokens[1]), tokens[2]);
                            pw.println("Now " + tokens[2] + " owes you " + Double.parseDouble(tokens[1]) / 2 + "lv.");
                            pw.flush();
                        }
                    }
                    if (cmd.equals("group-split")) {
                        try {
                            Double.parseDouble(tokens[1]);
                        } catch (NumberFormatException e) {
                            pw.println(tokens[1] + " is not a valid float number");
                        }
                        pw.println("Now the people from " + tokens[2] + " owe you " + ((Double.parseDouble(tokens[1])) / (numberOfGroupMembers(tokens[2]))) + " lv.");
                        pw.flush();
                        splitGroup(username, Double.parseDouble(tokens[1]), tokens[2]);
                    }
                    if (cmd.equals("paid")) {
                        try {
                            Double.parseDouble(tokens[1]);
                        } catch (NumberFormatException e) {
                            pw.println(tokens[1] + " is not a valid float number");
                        }
                        if (!isRegistered(tokens[2])) {
                            pw.println("There is no registered user with username " + tokens[2] + ". Please try again" +
                                    ".");
                        } else {
                            paid(username, Double.parseDouble(tokens[1]), tokens[2]);
                            pw.println("You have accepted " + tokens[2] + "'s payment of " + tokens[1] + " lv.");
                        }
                    }
                    if (cmd.equals("paid-group")) {
                        try {
                            Double.parseDouble(tokens[1]);
                        } catch (NumberFormatException e) {
                            pw.println(tokens[1] + " is not a valid float number");
                        }
                        groupPaid(username, Double.parseDouble(tokens[1]), tokens[2], tokens[3]);
                        pw.println("You have accepted " + tokens[3] + "'s payment of " + tokens[1] + "lv from group " + tokens[2] + ".");
                    }
                    if (cmd.equals("get-status")) {
                        getStatus(username, pw);
                    }
                    if (cmd.equals("get-history")) {
                        getHistory(username, pw);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("socket is closed");
        }
    }
}


