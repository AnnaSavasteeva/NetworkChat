package main.chat.clientserver.commands;

import java.io.Serializable;

public class AuthCommandData implements Serializable, CommandsGeneral {

    private final String login;
    private final String password;

    public AuthCommandData(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }

}
