package main.chat.client.model;

import main.chat.clientserver.Command;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Network {

    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 8190;

//    Singleton is a creational design pattern, which ensures
//    that only one object of its kind exists and provides
//    a single point of access to it for any other code.
//    Singleton has almost the same pros and cons as global variables.
//    Although they're super-handy, they break the modularity of your code.
//    У нас только одно сетевое соединение, поэтому можно использовать Singelton.
    private static Network INSTANCE;


//    На случай получения внешних данных
    private final String host;
    private final int port;

    private Socket socket;
    private ObjectInputStream socketInput;
    private ObjectOutputStream socketOutput;

//    Коллекция обработчиков событий (кнопок может быть несколько, событий при нажатии на одну кнопку тоже может быть несколько...)
//    CopyOnWriteArrayList<> - такой же ArrayList, но "потокобезопасный", поскольку каждый слушатель
//    будет запущен в рамках отдельного потока и возможна ситуация гонки
    private List<ReadCommandListener> listeners = new CopyOnWriteArrayList<>();

    private Thread readMessageProcess;
    private boolean connected;


//    Конструкторы приватные, т.к. экземпляр Network создается в getInstance()
    private Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private Network() {
        this(SERVER_HOST, SERVER_PORT);
    }



//    Аналог init() в ClientChat: здесь создание экземпляра Network происходит при первом вызове getInstance().
//    Такой способ удобно использовать, если нельзя зарнее предугадать, в какой момент лучше создать этот экземпляр.
    public static Network getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Network();
        }
        return INSTANCE;
    }



    public boolean connect() {
        try {
            socket = new Socket(host, port);
//            Обязательно в такой последовательности - противоположной серверу
            socketOutput = new ObjectOutputStream(socket.getOutputStream());
            socketInput = new ObjectInputStream(socket.getInputStream());

            readMessageProcess = startReadMessageProcess();

            connected = true;

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to establish connection");
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }


    public void sendAuthMsg(String login, String password) throws IOException {
        sendCommand(Command.authCommand(login, password));
    }

    public void sendPrivateMsg(String recipient, String msg) throws IOException {
        sendCommand(Command.privateMessageCommand(recipient, msg));
    }

    public void sendMsg(String msg) throws IOException {
        sendCommand(Command.publicMessageCommand(msg));
    }

    public void sendCommand(Command command) throws IOException {
        try {
            socketOutput.writeObject(command);
        } catch (IOException e) {
            System.err.println("Failed to send message to server");
//            Пробрасываем исключение явно, чтобы обработать ошибку на уровне графического интерфейса
            throw e;
        }
    }


//        Чтобы нивелировать подвисание в графическом интерфейсе выносим логику в отдельный поток
    private Thread startReadMessageProcess() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    Command command = readCommand();
                    if (command == null) {
                        continue;
                    }
                    for (ReadCommandListener messageListener : listeners) {
                        messageListener.processReceivedCommand(command);
                    }

                } catch (IOException e) {
                    System.err.println("Failed to read message from server");
                    close();
                    break;
                }
            }
        });

//        Если наше приложение умирает, стоит убивать и этот поток, т.к. его работа - фоновая
        thread.setDaemon(true);

        thread.start();

        return thread;
    }

    private Command readCommand() throws IOException {
        Command command = null;
        try {
            command = (Command) socketInput.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to find Command class");
            e.printStackTrace();
        }
        return command;
    }


    public ReadCommandListener addReadMessageListener(ReadCommandListener listener) {
        listeners.add(listener);
        return listener;
    }

    public void removeReadMessageListener(ReadCommandListener listener) {
        listeners.remove(listener);
    }


    public void close() {
        try {
            connected = false;
            readMessageProcess.interrupt();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
