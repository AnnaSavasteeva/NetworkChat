package main.chat.clientserver.commands;

import java.io.Serializable;

public class AuthOkCommandData implements Serializable, CommandsGeneral {

    private final String username;

    public AuthOkCommandData(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

}
