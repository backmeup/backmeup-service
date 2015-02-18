package org.backmeup.dal;

import java.util.concurrent.Callable;

public interface Connection {

    void beginOrJoin();

    void begin();

    void rollback();

    void commit();

    <T> T txNew(Callable<T> getter);
    void txNew(Runnable call);

    <T> T txNewReadOnly(Callable<T> getter);
    void txNewReadOnly(Runnable call);

    <T> T txJoinReadOnly(Callable<T> getter);
    void txJoinReadOnly(Runnable call);
    
    void txJoin(Runnable call);

}
