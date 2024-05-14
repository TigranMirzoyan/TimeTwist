package com.timetwist.utils;

import java.io.*;
import java.util.Base64;

public class Base64Utils {
    public static String encode(Serializable object) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Encoding error", e);
        }
    }

    public static <T> T decode(String base64String, Class<T> clazz) {
        byte[] data = Base64.getDecoder().decode(base64String);
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

            return clazz.cast(objectInputStream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Decoding error", e);
        }
    }
}

