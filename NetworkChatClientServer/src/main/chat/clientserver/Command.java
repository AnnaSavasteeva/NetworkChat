package main.chat.clientserver;

import main.chat.clientserver.commands.*;


import java.io.Serializable;
import java.util.List;

public class Command implements Serializable {
//    Суть: сервер или клиент будут получать объект типа Command и,
//    в зависимости от типа команды (значение поля type класса Command),
//    выполнять соответствующие действия

//    Используем интерфейс CommandsGeneral, т.к. заранее неизвестно,
//    объект какого типа будет записан в поле. Интерфейс и был создан только для этой цели.
//    Вместо интерфейса можно использовать общий класс Object
    private CommandsGeneral data;
    private CommandType type;


    public Object getData() {
        return data;
    }

    public CommandType getType() {
        return type;
    }


    public static Command authCommand(String login, String password) {
        Command command = new Command();
        command.type = CommandType.AUTH;
        command.data = new AuthCommandData(login, password);
        return command;
    }

    public static Command authOkCommand(String username) {
        Command command = new Command();
        command.type = CommandType.AUTH_OK;
        command.data = new AuthOkCommandData(username);
        return command;
    }

    public static Command errorCommand(String errorMessage) {
        Command command = new Command();
        command.type = CommandType.ERROR;
        command.data = new ErrorCommandData(errorMessage);
        return command;
    }

    public static Command publicMessageCommand(String message) {
        Command command = new Command();
        command.type = CommandType.PUBLIC_MESSAGE;
        command.data = new PublicMessageCommandData(message);
        return command;
    }

    public static Command privateMessageCommand(String receiver, String message) {
        Command command = new Command();
        command.type = CommandType.PRIVATE_MESSAGE;
        command.data = new PrivateMessageCommandData(receiver, message);
        return command;
    }

    public static Command clientMessageCommand(String sender, String message) {
        Command command = new Command();
        command.type = CommandType.CLIENT_MESSAGE;
        command.data = new ClientMessageCommandData(sender, message);
        return command;
    }

    public static Command updateUsersListCommand(List<String> users) {
        Command command = new Command();
        command.type = CommandType.UPDATE_USERS_LIST;
        command.data = new UpdateUsersListCommandData(users);
        return command;
    }

}
