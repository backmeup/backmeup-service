package org.backmeup.dal;

import java.util.concurrent.Callable;

public class ConnectionTemplate {

    private final Connection conn;

    public ConnectionTemplate(Connection conn) {
        this.conn = conn;
    }

    private <T> T handleError(Callable<T> getter) {
        try {

            return getter.call();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            conn.rollback();
        }
    }

    public <T> T insideNewTransaction(final Callable<T> getter) {
        return handleError(new Callable<T>() {
            @Override
            public T call() throws Exception {
                conn.begin();
                T response = getter.call();
                conn.commit();
                return response;
            }
        });
    }

    public void insideNewTransaction(Runnable call) {
        insideNewTransaction(callableFrom(call));
    }

    public <T> T insideNewTransactionRolledBack(final Callable<T> getter) {
        return handleError(new Callable<T>() {
            @Override
            public T call() throws Exception {
                conn.begin();
                return getter.call();
            }
        });
    }

    public void insideNewTransactionRolledBack(Runnable call) {
        insideNewTransactionRolledBack(callableFrom(call));
    }

    public <T> T insideJoinedTransaction(final Callable<T> getter) {
        return handleError(new Callable<T>() {
            @Override
            public T call() throws Exception {
                conn.beginOrJoin();
                T response = getter.call();
                conn.commit();
                return response;
            }
        });
    }

    public void insideJoinedTransaction(Runnable call) {
        insideJoinedTransaction(callableFrom(call));
    }

    public <T> T insideJoinedTransactionRolledBack(final Callable<T> getter) {
        return handleError(new Callable<T>() {
            @Override
            public T call() throws Exception {
                conn.beginOrJoin();
                return getter.call();
            }
        });
    }
    
    public void insideJoinedTransactionRolledBack(Runnable call) {
        insideJoinedTransactionRolledBack(callableFrom(call));
    }

    private static Callable<Void> callableFrom(final Runnable runnable) {
        return new Callable<Void>() {
            @Override
            public Void call() {
                runnable.run(); // NOSONAR we are not running Threads but reusing Runnable as function/closure. 
                return null;
            }
        };
    }

}
