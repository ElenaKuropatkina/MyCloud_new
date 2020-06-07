package com.elenakuropatkina.my.cloud.server;

import com.elenakuropatkina.my.cloud.common.CommandMessage;
import com.elenakuropatkina.my.cloud.common.ListMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class CommandService {

    Path userPath;

    public Path getPath() {
        return userPath;
    }

    public CommandMessage auth(CommandMessage cmd) throws IOException {
        String s = cmd.getData();
        String[] tokens = s.split(" ");
        String login = tokens[0];
        userPath = Paths.get("my_server_storage/" + login);
        System.out.println("папка пользователя " + userPath);
        String pass = tokens[1];
        if (AuthService.checkLoginAndPass(login, pass)) {
            return new CommandMessage(CommandMessage.Command.AUTH_OK);
        } else
            return new CommandMessage(CommandMessage.Command.AUTH_FALSE);
    }


    public ListMessage deleteFile(CommandMessage cmd) throws IOException {
        System.out.println("Сейчас удалим " + userPath + "/" + cmd.getData());
        if (Files.exists(Paths.get(userPath + "/" + cmd.getData()))) {
            System.out.println("Файл удаляется " + userPath + "/" + cmd.getData());
            Files.delete(Paths.get(userPath + "/" + cmd.getData()));
        }
        return getList();
    }

    public ListMessage getList() throws IOException {
        ListMessage lm = new ListMessage();
        lm.setList(Files.list(userPath)
                .filter(p -> !Files.isDirectory(p))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList()));
        return lm;

    }

    public ListMessage renameFile(CommandMessage cmd) throws IOException {
        String s = cmd.getData();
        System.out.println(s);
        String[] tokens = s.split(" ");
        System.out.println(tokens[0]);
        System.out.println(tokens[1]);
        if (Files.exists(Paths.get(userPath + "/" + tokens[0]))) {
            File original = userPath.resolve(tokens[0]).toFile();
            File newFile = userPath.resolve(tokens[1]).toFile();
            if (original.exists() & original.isFile() & original.canWrite()) {
                original.renameTo(newFile);
            }
        }
        return getList();
    }

}
