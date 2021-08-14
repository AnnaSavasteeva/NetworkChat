package main.chat.clientserver.commands;

import java.io.Serializable;

public class AuthTimeoutCommandData implements Serializable, CommandsGeneral {
    private String messageFromServer;

    public AuthTimeoutCommandData(String messageFromServer) {
        this.messageFromServer = messageFromServer;
    }

    public String getMessageFromServer() {
        return messageFromServer;
    }

}
