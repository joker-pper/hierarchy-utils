package com.github.jokerpper.hierarchy.support;

import java.io.*;

public class ObjectUtils {

    public static <T> byte[] serialize(T obj) {
        if (obj == null) {
            return null;
        }
        byte[] bytes;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream(1024);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("serialize failed.", e);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
            }

            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
            }
        }
        return bytes;
    }

    public static <T> T decode(byte[] bytes) {
        try {
            ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (T) objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}
