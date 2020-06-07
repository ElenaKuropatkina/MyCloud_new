package com.elenakuropatkina.my.cloud.common;

public class CommandMessage extends AbstractMessage {

    public enum Command {
        AUTH, AUTH_OK, AUTH_FALSE, FILE_DELETE, FILE_GET_LIST, FILE_RENAME, FILE_REQUEST
    }

    private Command cmd;
    private String data;

    public CommandMessage(Command cmd) {
        this.cmd = cmd;
    }

    public CommandMessage(Command cmd, String data) {
        this.cmd = cmd;
        this.data = data;
    }

    public Command getCommand() {
        return cmd;
    }

    public String getData() {
        return data;
    }
}
