package main.chat.server.chat;

import main.chat.clientserver.Command;
import main.chat.clientserver.CommandType;
import main.chat.clientserver.commands.AuthCommandData;
import main.chat.clientserver.commands.PrivateMessageCommandData;
import main.chat.clientserver.commands.PublicMessageCommandData;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    private final MyServer server;
    private final Socket clientSocket;
//    Классы Object...Stream используются для обмена объектами, что нам и нужно,
//    т.к. сервер и клиент будут обмениваться объектами-командами из модуля NetworkChatClientServer
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private String username;

    private Timer timer;
    private static final int TIME_LIMIT = 120000;

    public ClientHandler(MyServer server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }



//    для обработки подключений
    public void handle(ExecutorService executor) throws IOException {
//        Обязательно в такой последовательности - противоположной клиенту
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

//        Создаем отдельный поток, чтобы основной поток не блокировался на методе accept,
//        т.к. нужно, чтобы в один и тот же момент времени можно было как ожидать новых подключений,
//        так и обрабатывать ранее полученные. Вот для обработки каждого нового подключения
//        и создаем отдельные потоки.
//        Обрабатываем исключение уже здесь, поскольку из потока, вызываемого в рамках текущего метода,
//        нельзя прокинуть исключение в другой внешний поток. Т.е. если ошибка былка брошена внутри потока,
//        то дальше потока она не уйдет.
        executor.execute(() -> {
            try {
                closeClientSocketOnTimeLimit(clientSocket);
                authentication();
                readMessages();
            } catch (IOException e) {
                System.err.println("Failed to process message from client");
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    System.err.println("Failed to close connection");
                }
            }
        });
    }

    private void authentication() throws IOException {
//        Оборачиваем в цикл, чтобы проверять логин и пароль до тех пор, пока не
//        будут введены корректные
        while (true) {
            Command command = readCommand();

            if (command == null) {
//                Перескакиваем на ожидание следующего сообщения
                continue;
            }

            if (command.getType() == CommandType.AUTH) {
                AuthCommandData data = (AuthCommandData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();

                String username = server.getAuthService().getUsernameByLoginAndPassword(login, password);
                if (username == null) {
                    sendCommand(Command.errorCommand("Некорректные логин и пароль!"));
                } else if (server.isUsernameBusy(username)) {
                    sendCommand(Command.errorCommand("Такой юзер уже существует!"));
                } else {
                    this.username = username;
                    sendCommand(Command.authOkCommand(username));
                    server.subscribe(this);
                    if (timer != null) {
                        timer.cancel();
                    }
                    return;
                }
            }
        }

    }



    private void readMessages() throws IOException {
        while (true) {
            Command command = readCommand();

            if (command == null) {
                continue;
            }

            switch (command.getType()) {
                case END:
                    return;
//                    помещаем код в {}, чтобы в рамках switch можно было использовать переменные с одинаковыми именами (data)
                case PRIVATE_MESSAGE: {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    String recipient = data.getReceiver();
                    String privateMessage = data.getMessage();
                    server.sendPrivateMessage(this, recipient, privateMessage);
                    break;
                }
                case PUBLIC_MESSAGE: {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    processMessage(data.getMessage());
                }
            }
        }
    }

    private void processMessage(String message) throws IOException {
        server.broadcastMessage(message, this);
    }

    private Command readCommand() throws IOException {
        Command command = null;
        try {
            command = (Command) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to find Command class");
            e.printStackTrace();
        }
        return command;
    }

    public void sendCommand(Command command) throws IOException {
        outputStream.writeObject(command);
    }


    private void closeClientSocketOnTimeLimit(Socket socket) {
        this.timer = new Timer(true);

        System.out.println("Time started");

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendCommand(Command.authTimeoutCommand("Connection closed: user is not logged in"));
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, TIME_LIMIT);
    }



    private void closeConnection() throws IOException {
        server.unsubscribe(this);
        clientSocket.close();
    }



    public String getUsername() {
        return username;
    }

}
