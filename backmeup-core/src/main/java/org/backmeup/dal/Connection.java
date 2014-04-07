package org.backmeup.dal;

public interface Connection {

    void beginOrJoin();

    void begin();

    void rollback();

    void commit();
}
