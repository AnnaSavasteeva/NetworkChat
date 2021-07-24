package main.chat.clientserver.commands;

import java.io.Serializable;

public class PublicMessageCommandData implements Serializable, CommandsGeneral {

    private final String message;

    public PublicMessageCommandData(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
