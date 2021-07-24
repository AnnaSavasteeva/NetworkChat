package main.chat.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import main.chat.client.ClientChat;
import main.chat.client.dialogs.Dialogs;

import main.chat.client.model.Network;
import main.chat.client.model.ReadCommandListener;
import main.chat.clientserver.Command;
import main.chat.clientserver.CommandType;
import main.chat.clientserver.commands.AuthOkCommandData;
import main.chat.clientserver.commands.AuthTimeoutCommandData;

import java.io.IOException;

public class AuthController {

    @FXML
    private PasswordField passwordField;
    @FXML private TextField loginField;
    @FXML private Button authButton;

    private ReadCommandListener readMessageListener;


    @FXML
    public void executeAuth(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();

        if(login == null || login.isEmpty() || password == null || password.isEmpty()) {
            Dialogs.AuthError.EMPTY_CREDENTIALS.show();
            return;
        }

        if (!connectToServer()) {
            Dialogs.NetworkError.SERVER_CONNECT.show();
            return;
        }

        try {
            Network.getInstance().sendAuthMsg(login, password);
        } catch (IOException e) {
            Dialogs.NetworkError.SEND_MESSAGE.show();
            e.printStackTrace();
        }
    }


    private boolean connectToServer() {
        Network network = getNetwork();
        return network.isConnected() || network.connect();
    }

    private Network getNetwork() {
        return Network.getInstance();
    }



    public void initMessageHandler() {
        readMessageListener = getNetwork().addReadMessageListener(new ReadCommandListener() {
            @Override
            public void processReceivedCommand(Command command) {
                if (command.getType() == CommandType.AUTH_OK) {
                    AuthOkCommandData data = (AuthOkCommandData) command.getData();
                    String username = data.getUsername();
                    System.out.println(username + " has connected!");
//                    Оборачиваем в runLater, чтобы система не упала с ошибкой при обращении к UI-элементам из отдельного потока
                    Platform.runLater(() -> ClientChat.INSTANCE.switchToMainChatWindow(username));
                } else if (command.getType() == CommandType.AUTH_TIMEOUT) {
                    AuthTimeoutCommandData data = (AuthTimeoutCommandData) command.getData();
                    System.out.println(data.getMessageFromServer());
                } else {
                    Platform.runLater(Dialogs.AuthError.INVALID_CREDENTIALS::show);
                    System.out.println("invalid credentials");
                }
            }
        });
    }


    public void close() {
        getNetwork().removeReadMessageListener(readMessageListener);
    }

}
