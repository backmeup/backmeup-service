package org.backmeup.model.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class Serialization {
    private Serialization() {
        // Utility classes should not have public constructor
    }
    
    public static String getObjectAsEncodedString(Object obj) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream so = new ObjectOutputStream(bo);
        so.writeObject(obj);
        so.flush();
        return MyBase64.encode(bo.toByteArray());
    }

    @SuppressWarnings("unchecked")
    public static <T> T getEncodedStringAsObject(String properpies, Class<T> type) throws IOException, ClassNotFoundException {
        byte b[] = MyBase64.decode(properpies);
        ByteArrayInputStream bi = new ByteArrayInputStream(b);
        ObjectInputStream si = new ObjectInputStream(bi);
        return (T) si.readObject();
    }
}
