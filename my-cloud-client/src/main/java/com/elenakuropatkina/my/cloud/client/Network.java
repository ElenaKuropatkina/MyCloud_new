package com.elenakuropatkina.my.cloud.client;

import com.elenakuropatkina.my.cloud.common.AbstractMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class Network {

    private static Socket socket;
    private static ObjectDecoderInputStream in;
    private static ObjectEncoderOutputStream out;

    public static void start() {
        try {
            socket = new Socket("localhost", 8189);
            in = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void stop() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendMsg(AbstractMessage msg) {
        try {
            out.writeObject(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static AbstractMessage readMsg() throws IOException, ClassNotFoundException {
        Object object = in.readObject();
        return (AbstractMessage) object;
    }

}
