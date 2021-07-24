package main.chat.client.model;

import main.chat.clientserver.Command;

public interface ReadCommandListener {
    void processReceivedCommand(Command command);
}
