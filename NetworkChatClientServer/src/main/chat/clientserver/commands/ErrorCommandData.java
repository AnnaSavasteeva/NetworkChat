package main.chat.clientserver.commands;

import java.io.Serializable;

public class ErrorCommandData implements Serializable, CommandsGeneral {

    private final String errorMessage;

    public ErrorCommandData(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
