package main.chat.server.chat;


import main.chat.server.chat.auth.AuthService;

import main.chat.clientserver.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private final List<ClientHandler> clients = new ArrayList<>();
    private AuthService authService;

    public void start(int port) {

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server has been started");

            authService = new AuthService();

            while (true) {
                waitAndProcessClientConnection(serverSocket);
            }

        } catch (IOException e) {
            System.err.println("Failed to bind port " + port);
            e.printStackTrace();
        }
    }

    private void waitAndProcessClientConnection(ServerSocket serverSocket) throws IOException {
        System.out.println("Waiting for new client connection");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client has been connected");

        ClientHandler clientHandler = new ClientHandler(this, clientSocket);

        clientHandler.handle();
    }

    //    Используем synchronized, т.к. к clients может идти одновременное обращение сразу из нескольких потоков:
//    рассылка сообщения в чате, добавление подключения в clients, удаление подклбчения из clients
    public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException {
        for (ClientHandler client : clients) {
//            equals тут можно не сипользовать, т.к. мы сравниваем не идентичность, а именно равенство
            if (client != sender) {
                client.sendCommand(Command.clientMessageCommand(sender.getUsername(), message));
            }
        }
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

    public synchronized void sendPrivateMessage(ClientHandler sender, String recipient, String privateMessage) throws IOException {
        for (ClientHandler client: clients) {
            if (client != sender && client.getUsername().equals(recipient)) {
                client.sendCommand(Command.clientMessageCommand(sender.getUsername(), privateMessage));
                break;
            }
        }
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

}
