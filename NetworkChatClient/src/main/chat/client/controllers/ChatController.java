package main.chat.client.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.chat.client.ClientChat;
import main.chat.client.dialogs.Dialogs;

import main.chat.client.model.Network;
import main.chat.client.model.ReadCommandListener;
import main.chat.clientserver.Command;
import main.chat.clientserver.CommandType;
import main.chat.clientserver.commands.ClientMessageCommandData;
import main.chat.clientserver.commands.UpdateUsersListCommandData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ChatController {
    private String username;

    @FXML
    public ListView<String> usersList;

    @FXML
    private Button sendButton;
    @FXML
    private TextArea chatHistory;
    @FXML
    private TextArea messageTextArea;



//    messages
    @FXML
    private void sendMessage() {
        String message = messageTextArea.getText().trim();
        String senderUsername = this.username;
        String pathToSenderHistoryFile = this.createPathToHistoryFile(senderUsername);
        String historyData = null;

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
                historyData = createHistoryData(senderUsername, recipient, message);
            } else {
                Network.getInstance().sendMsg(message);
                historyData = createHistoryData(senderUsername, "all users", message);
            }
        } catch (IOException e) {
            Dialogs.NetworkError.SEND_MESSAGE.show();
        }

        appendMsgToChat("Я", message);
        this.putDataToHistory(historyData, pathToSenderHistoryFile);
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
                    String senderUsername = data.getSender();
                    String message = data.getMessage();

                    Platform.runLater(() -> ChatController.this.appendMsgToChat(senderUsername, message));

                    String historyData = createHistoryData(senderUsername, username, message);
                    String pathToRecipientHistoryFile = createPathToHistoryFile(username);
                    putDataToHistory(historyData, pathToRecipientHistoryFile);

                } else if (command.getType() == CommandType.UPDATE_USERS_LIST) {
                    UpdateUsersListCommandData data = (UpdateUsersListCommandData) command.getData();

                    Platform.runLater(() -> updateUsersList(data.getUsers()));
                }
            }
        });
    }



//    history
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
        return ClientChat.getHistoryFolder() + "\\history" + "_" + username + ".txt";
    }



//    users
    public void updateUsersList(List<String> users) {
        Platform.runLater(() -> usersList.setItems(FXCollections.observableArrayList(users)));
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
