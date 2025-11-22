package com.client.negocio;

import com.server.negocio.Command;

public class Message {
    private Command command;

    private String data;

    public Message() { }

    public Message(Command command, String data) {
        this.command = command;
        this.data = data;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return command + "," + data;
    }

}
