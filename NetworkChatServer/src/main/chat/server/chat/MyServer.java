package main.chat.server.chat;


import main.chat.server.chat.auth.AuthService;

import main.chat.clientserver.Command;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyServer {
    private final static String DB_URL = "jdbc:sqlite:NetworkChatServer/networkChatDb.db";
    private static final String HISTORY_FOLDER = "NetworkChatClient\\src\\history";
    private static final int HISTORY_LIMIT = 100;

    private final List<ClientHandler> clients = new ArrayList<>();
    private Connection connection;
    private AuthService authService;


    public void start(int port) {

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server has been started");

            this.loadHistory();

            this.dbConnect();
            authService = new AuthService(this.connection);

            while (true) {
                waitAndProcessClientConnection(serverSocket);
            }

        } catch (IOException e) {
            System.err.println("Failed to bind port " + port);
            e.printStackTrace();
        }
    }

    private void loadHistory() {
        File historyFolder = new File(HISTORY_FOLDER);
        File[] historyFilesCollection = historyFolder.listFiles();

        if (historyFilesCollection.length > 0) {
            loadLimitedHistory(historyFilesCollection, HISTORY_LIMIT);
        }
    }

    private void loadLimitedHistory(File[] filesCollection, int linesLimit) {
        for (File file : filesCollection) {
            ArrayList<String> linesArr = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String str;
                while ((str = reader.readLine()) != null) {
                    linesArr.add(str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                int startLine = linesArr.size() - linesLimit;
                for (int i = startLine; i < linesArr.size(); i++) {
                    writer.write(linesArr.get(i) + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void waitAndProcessClientConnection(ServerSocket serverSocket) throws IOException {
        System.out.println("Waiting for new client connection");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client has been connected");

        ClientHandler clientHandler = new ClientHandler(this, clientSocket);

        clientHandler.handle();
    }


    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
        updateUsersListForClients();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) throws IOException {
        clients.remove(clientHandler);
        updateUsersListForClients();
    }


    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isUsernameBusy(String username) {
        for (ClientHandler client: clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }

        return false;
    }


//    ???????????????????? synchronized, ??.??. ?? clients ?????????? ???????? ?????????????????????????? ?????????????????? ?????????? ???? ???????????????????? ??????????????:
//    ???????????????? ?????????????????? ?? ????????, ???????????????????? ?????????????????????? ?? clients, ???????????????? ?????????????????????? ???? clients
    public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException {
        String senderUsername = sender.getUsername();

        for (ClientHandler client : clients) {
            String pathToHistoryFile = this.createPathToHistoryFile(client.getUsername());
            String historyData;

//            equals ?????? ?????????? ???? ????????????????????????, ??.??. ???? ???????????????????? ???? ????????????????????????, ?? ???????????? ??????????????????
            if (client != sender) {
                client.sendCommand(Command.clientMessageCommand(senderUsername, message));
                historyData = createHistoryData(senderUsername, client.getUsername(), message);
            } else {
                historyData = createHistoryData(senderUsername, "all users", message);
            }

            this.putDataToHistory(historyData, pathToHistoryFile);
        }
    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String recipient, String privateMessage) throws IOException {
        String senderUsername = sender.getUsername();
        String historyData = createHistoryData(senderUsername, recipient, privateMessage);
        boolean gotSender = false;
        boolean gotRecipient = false;

        for (ClientHandler client: clients) {
            if (!gotRecipient && (client != sender && client.getUsername().equals(recipient))) {
                client.sendCommand(Command.clientMessageCommand(senderUsername, privateMessage));

                String pathToRecipientHistory = this.createPathToHistoryFile(client.getUsername());
                this.putDataToHistory(historyData, pathToRecipientHistory);
                gotRecipient = true;
            }

            if (!gotSender && (client == sender)) {
                String pathToSenderHistory = this.createPathToHistoryFile(senderUsername);
                this.putDataToHistory(historyData, pathToSenderHistory);
                gotSender = true;
            }

            if (gotSender && gotRecipient) {
                break;
            }
        }
    }

    private void putDataToHistory(String historyData, String path) {
        if (!isHistoryFileExist(path)) {
            createHistoryFile(path);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            writer.write(historyData);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isHistoryFileExist(String pathToFile) {
        return (new File(pathToFile).exists());
    }

    private void createHistoryFile(String pathToFile) {
        try {
            (new File(pathToFile)).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createHistoryData (String sender, String recipient, String message) {
        return String.format("[ Date: %s ] --- [ Sender: %s ] --- [ Recipient: %s ] --- [ Message: %s ]%n",
                DateFormat.getDateTimeInstance().format(new Date()), sender, recipient, message, System.lineSeparator());
    }

    private String createPathToHistoryFile(String username) {
        return HISTORY_FOLDER + "\\history" + "_" + username + ".txt";
    }


    public void updateUsersListForClients() throws IOException {
        List<String> users = new ArrayList<>();

        for (ClientHandler client : clients) {
            users.add(client.getUsername());
        }

        for (ClientHandler client : clients) {
            client.sendCommand(Command.updateUsersListCommand(users));
        }
    }


    private void dbConnect() {
        try {
            System.out.println("Database has been connected");
            this.connection = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Database connection failed");
        }
    }

    public void dbDisconnect() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
