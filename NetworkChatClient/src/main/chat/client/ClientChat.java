package main.chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import main.chat.client.controllers.AuthController;
import main.chat.client.controllers.ChatController;

import java.io.*;
import java.util.ArrayList;

public class ClientChat extends Application {

//    INSTANCE - это объект ClientChart, который создается автоматически в методе start() благодаря init()
//    Это дает возможность избавиться от пробрасывания экземпляра класса между другими классами с целью вызова
//    нужных методов
    public static ClientChat INSTANCE;

    private static final String CHAT_WINDOW_FXML = "..\\..\\..\\chat.fxml";
    private static final String AUTH_DIALOG_FXML = "/authDialog.fxml";

    private static final String HISTORY_FOLDER = "NetworkChatClient\\history";
    private static final int HISTORY_LIMIT = 100;


    private Stage chatStage;
    private Stage authStage;
    private FXMLLoader chatWindowLoader;
    private FXMLLoader authLoader;


//    init() вызывается ДО start(), это специальный метод для инициализации статической переменной типа данного класса
    @Override
    public void init() {
        INSTANCE = this;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
//        База для окна чата
        this.chatStage = primaryStage;

//        Отрисовываем окна чата и аутентификации
        initViews();
        getChatStage().show();
        getAuthStage().show();

//        Получаем контроллер аутентификациии и запускаем обработчик полученных от сервера сообщений
        getAuthController().initMessageHandler();

    }



//    Получаем контроллеры окон аутентификации и чата
    private AuthController getAuthController() {
        return authLoader.getController();
    }

    public ChatController getChatController() {
        return chatWindowLoader.getController();
    }



//    Отрисовка окон аутентификации и чата
    private void initViews() throws IOException {
        initChatWindow();
        initAuthDialog();
    }

    private void initAuthDialog() throws java.io.IOException {
        authLoader = new FXMLLoader();
        authLoader.setLocation(ClientChat.class.getResource(AUTH_DIALOG_FXML));
        Parent authDialogPanel = authLoader.load();

        authStage = new Stage();
        authStage.initOwner(chatStage);
//        Чтобы окно чата было недоступно для редактирования, пока юзер не пройдет аутентификацию
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.setScene(new Scene(authDialogPanel));
    }

    private void initChatWindow() throws IOException {
        chatWindowLoader = new FXMLLoader();
        chatWindowLoader.setLocation(ClientChat.class.getResource(CHAT_WINDOW_FXML));
        Parent root = chatWindowLoader.load();

        this.chatStage.setScene(new Scene(root));
    }

//    Используется в методе initMessageHandler() в AuthController для закрытия окна аутентификации
    public Stage getAuthStage() {
        return authStage;
    }
//    Для присвоения окну чата имени пользователя (в методе initMessageHandler() в AuthController)
    public Stage getChatStage() {
        return chatStage;
    }



//    После успешной аутентификации присваиваем имя юзера окну чата,
//    закрываем окно аутентификации, получаем контроллер чата и
//    запускаем обработку сообщений в чате
    public void switchToMainChatWindow(String username) {
        getChatStage().setTitle(username);
        getChatController().setUsername(username);
        getChatController().initMessageHandler();

        getAuthController().close();
        getAuthStage().close();

        this.loadHistoryFiles();
    }



//    history
    public static String getHistoryFolder() {
        return HISTORY_FOLDER;
    }

    private void loadHistoryFiles() {
        File historyFolder = new File(HISTORY_FOLDER);
        File[] historyFilesCollection = historyFolder.listFiles();

        if (historyFilesCollection != null && historyFilesCollection.length > 0) {
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
                int startLine = (linesArr.size() <= linesLimit) ? 0 : (linesArr.size() - linesLimit);
                for (int i = startLine; i < linesArr.size(); i++) {
                    writer.write(linesArr.get(i) + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        launch(args);
    }

}
