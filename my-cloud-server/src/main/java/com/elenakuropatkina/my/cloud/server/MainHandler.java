package com.elenakuropatkina.my.cloud.server;

import com.elenakuropatkina.my.cloud.common.CommandMessage;
import com.elenakuropatkina.my.cloud.common.FileMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


public class MainHandler extends ChannelInboundHandlerAdapter {

    CommandService cs = new CommandService();


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof CommandMessage) {
            System.out.println(((CommandMessage) msg).getData());
            switch (((CommandMessage) msg).getCommand()) {
                case FILE_REQUEST:
                    try {
                        fileProcessingOut(ctx, (Paths.get(cs.getPath().toString() + "/" + ((CommandMessage) msg).getData())));
                        System.out.println(cs.getPath().toString() + ((CommandMessage) msg).getData());
                        break;
                    } catch (IOException e) {
                        System.out.println("File not found");
                    }
                case FILE_DELETE:
                    System.out.println("Получен запрос на удаление " + ((CommandMessage) msg).getData());
                    ctx.writeAndFlush(cs.deleteFile((CommandMessage) msg));
                    break;
                case FILE_GET_LIST:
                    System.out.println("FilesList");
                    ctx.writeAndFlush(cs.getList());
                    break;
                case FILE_RENAME:
                    ctx.writeAndFlush(cs.renameFile((CommandMessage) msg));
                    break;
                case AUTH:
                    ctx.writeAndFlush(cs.auth((CommandMessage) msg));
                    break;
            }
        }

        if (msg instanceof FileMessage) {
            fileProcessingIn(cs.getPath(), (FileMessage) msg);
            ctx.writeAndFlush(cs.getList());
        }
    }

    public void fileProcessingOut(ChannelHandlerContext ctx, Path path) throws IOException {
        if (Files.exists(path)) {
            long size = 0;
            try {
                size = Files.size(path);
                int sizeOfFiles = 45 * 1024 * 1024;
                int quantityOfParts = (int) (size / sizeOfFiles + 1);
                byte[] buffer = new byte[sizeOfFiles];
                FileMessage fm = new FileMessage(path.getFileName().toString(), quantityOfParts, -1, buffer);
                FileInputStream in = new FileInputStream(path.toString());
                for (int i = 0; i < quantityOfParts; i++) {
                    int byteCount = in.read(fm.getData());
                    fm.setPartNumber(i);
                    if (byteCount < sizeOfFiles) {
                        fm.setData(Arrays.copyOfRange(fm.getData(), 0, byteCount));
                    }
                    ctx.writeAndFlush(fm);
                    System.out.println("Файл отправляется: часть " + (i));
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
