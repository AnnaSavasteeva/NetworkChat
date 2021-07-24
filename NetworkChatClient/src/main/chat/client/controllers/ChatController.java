package main.chat.client.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.chat.client.dialogs.Dialogs;

import main.chat.client.model.Network;
import main.chat.client.model.ReadCommandListener;
import main.chat.clientserver.Command;
import main.chat.clientserver.CommandType;
import main.chat.clientserver.commands.ClientMessageCommandData;
import main.chat.clientserver.commands.UpdateUsersListCommandData;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ChatController {

    @FXML
    public ListView<String> usersList;

    @FXML
    private Button sendButton;
    @FXML
    private TextArea chatHistory;
    @FXML
    private TextArea messageTextArea;


    @FXML
    private void sendMessage() {
        String message = messageTextArea.getText().trim();

        if (message.isEmpty()) {
            messageTextArea.clear();
            return;
        }

        String recipient = null;
        if (!usersList.getSelectionModel().isEmpty()) {
            recipient = usersList.getSelectionModel().getSelectedItem();
        }

        try {
            if (recipient != null) {
                Network.getInstance().sendPrivateMsg(recipient, message);
            } else {
                Network.getInstance().sendMsg(message);
            }
        } catch (IOException e) {
            Dialogs.NetworkError.SEND_MESSAGE.show();
        }

        appendMsgToChat("Я", message);
    }

    private void appendMsgToChat(String sender, String message) {
        chatHistory.appendText(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.appendText(System.lineSeparator());
        if (sender != null) {
            chatHistory.appendText(sender + ":");
            chatHistory.appendText(System.lineSeparator());
        }

        chatHistory.appendText(message);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());
        messageTextArea.clear();
    }

    @FXML
    public void sendTextAreaMessage(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            event.consume(); // otherwise a new line will be added to the textArea after the sendFunction() call
            if (event.isShiftDown()) {
                messageTextArea.appendText(System.lineSeparator());
            } else {
                sendMessage();
            }
        }
    }

    public void initMessageHandler() {
        Network.getInstance().addReadMessageListener(new ReadCommandListener() {
            @Override
            public void processReceivedCommand(Command command) {
                if (command.getType() == CommandType.CLIENT_MESSAGE) {
                    ClientMessageCommandData data = (ClientMessageCommandData) command.getData();
                    Platform.runLater(() -> ChatController.this.appendMsgToChat(data.getSender(), data.getMessage()));
                    } else if (command.getType() == CommandType.UPDATE_USERS_LIST) {
                        UpdateUsersListCommandData data = (UpdateUsersListCommandData) command.getData();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                updateUsersList(data.getUsers());
                            }
                        });
                    }
            }
        });
    }

    public void updateUsersList(List<String> users) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                usersList.setItems(FXCollections.observableArrayList(users));
            }
        });
    }

}
