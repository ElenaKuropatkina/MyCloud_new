package com.elenakuropatkina.my.cloud.client;

import com.elenakuropatkina.my.cloud.common.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MyClientController implements Initializable {

    @FXML
    TextField tfFileNameLocal;
    @FXML
    TextField tfFileNameRemote;

    @FXML
    ListView<String> filesList;
    @FXML
    ListView<String> remoteFilesList;

    @FXML
    HBox upperPanel;
    @FXML
    HBox bottomPanel;

    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    Label label;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {

            try {
                while (true) {
                    AbstractMessage am = Network.readMsg();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        System.out.println("Получен файл с номером " + fm.getPartNumber());
                        fileProcessingIn(Paths.get("client_storage/"), fm);
                        refreshLocalFilesList();
                    }
                    if (am instanceof ListMessage) {
                        ListMessage lm = (ListMessage) am;
                        System.out.println(lm.getList().toString());
                        refreshRemoteFilesList(lm);
                    }
                    if (am instanceof CommandMessage) {
                        CommandMessage cm = (CommandMessage) am;
                        setAuth(cm);
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
    }

    public void setAuth(CommandMessage cm) {

        if (cm.getCommand().equals(CommandMessage.Command.AUTH_FALSE)) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            label.setVisible(true);

        }
        if (cm.getCommand().equals(CommandMessage.Command.AUTH_OK)) {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
        }
    }


    public void fileProcessingIn(Path path, FileMessage fm) throws IOException {
        try {
            System.out.println("Пришел файл с номером " + fm.getPartNumber());
            boolean a = true;
            if (fm.getPartNumber() == (fm.getQuantityOfParts() - 1)) {
                a = false;
                System.out.println("Файл получен");
            }
            System.out.println("Файл загружается  " + fm.getPartNumber() + "/" + fm.getQuantityOfParts());
            FileOutputStream out = new FileOutputStream(path + "/" + fm.getFilename(), a);
            out.write(fm.getData());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fileProcessingOut(String fileName) throws IOException {
        long size = Files.size(Paths.get("client_storage/" + fileName));
        int sizeOfFiles = 45 * 1024 * 1024;
        int quantityOfParts = (int) (size / sizeOfFiles + 1);
        byte[] buffer = new byte[sizeOfFiles];
        FileMessage fm = new FileMessage(fileName, quantityOfParts, -1, buffer);
        FileInputStream in = new FileInputStream("client_storage/" + fileName);
        for (int i = 0; i < quantityOfParts; i++) {
            int byteCount = in.read(fm.getData());
            fm.setPartNumber(i);
            if (byteCount < sizeOfFiles) {
                fm.setData(Arrays.copyOfRange(fm.getData(), 0, byteCount));
            }
            Network.sendMsg(fm);
            System.out.println("Файл отправляется: часть " + i);
        }
        in.close();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileNameRemote.getLength() > 0) {
            Network.sendMsg(new CommandMessage(CommandMessage.Command.FILE_REQUEST, tfFileNameRemote.getText()));
            tfFileNameRemote.clear();

        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) throws Exception {
        if (tfFileNameLocal.getLength() > 0) {
            fileProcessingOut(tfFileNameLocal.getText());
            tfFileNameLocal.clear();
        }
    }

    public void pressOnDeleteRemoteBtn(ActionEvent actionEvent) {
        if (tfFileNameRemote.getLength() > 0) {
            Network.sendMsg(new CommandMessage(CommandMessage.Command.FILE_DELETE, tfFileNameRemote.getText()));
            System.out.println("Отправлен запрос на удаление " + tfFileNameRemote.getText());
            tfFileNameRemote.clear();
        }
    }

    public void pressOnDeleteLocalBtn(ActionEvent actionEvent) throws IOException {
        if (tfFileNameLocal.getLength() > 0) {
            Files.delete(Paths.get("client_storage/" + tfFileNameLocal.getText()));
            tfFileNameLocal.clear();
            refreshLocalFilesList();
        }
    }

    public void pressOnGetListBtn(ActionEvent actionEvent) {
        Network.sendMsg(new CommandMessage(CommandMessage.Command.FILE_GET_LIST));
    }

    public void pressOnRenameLocalBtn(ActionEvent actionEvent) throws IOException {
        if (tfFileNameLocal.getLength() > 0) {
            String s = tfFileNameLocal.getText();
            String[] tokens = s.split(" ");
            Path folder = Paths.get("client_storage");

            File original = folder.resolve(tokens[0]).toFile();
            File newFile = folder.resolve(tokens[1]).toFile();

            if (original.exists() & original.isFile() & original.canWrite()) {
                original.renameTo(newFile);
            }
            tfFileNameLocal.clear();
            refreshLocalFilesList();
        }
    }


    public void pressOnRenameRemoteBtn(ActionEvent actionEvent) {
        if (tfFileNameRemote.getLength() > 0) {
            Network.sendMsg(new CommandMessage(CommandMessage.Command.FILE_RENAME, tfFileNameRemote.getText()));
        }
        tfFileNameRemote.clear();
    }

    public void refreshLocalFilesList() {
        Platform.runLater(() -> {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get("client_storage"))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> filesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public void refreshRemoteFilesList(ListMessage lm) {
        Platform.runLater(() -> {
            remoteFilesList.getItems().clear();
            lm.getList().forEach(o -> remoteFilesList.getItems().add(o));
        });
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (loginField.getLength() > 0 && passwordField.getLength() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(loginField.getText());
            sb.append(" ");
            sb.append(passwordField.getText());
            Network.sendMsg(new CommandMessage(CommandMessage.Command.AUTH, sb.toString()));
        }
        loginField.clear();
        passwordField.clear();
    }

    public void selectFileLocal(javafx.scene.input.MouseEvent mouseEvent) {
        String fileName;
        tfFileNameLocal.clear();
        if (filesList.isPickOnBounds() && filesList.isFocused()) {
            MultipleSelectionModel<String> msm = filesList.getSelectionModel();
            fileName = msm.getSelectedItem();
            tfFileNameLocal.setText(fileName);
        }

    }

    public void selectFileRemote(javafx.scene.input.MouseEvent mouseEvent) {
        String fileName;
        if (remoteFilesList.isPickOnBounds() && remoteFilesList.isFocused()) {
            MultipleSelectionModel<String> msm = remoteFilesList.getSelectionModel();
            fileName = msm.getSelectedItem();
            tfFileNameRemote.setText(fileName);
        }
    }
}



