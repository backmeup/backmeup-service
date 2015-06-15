package org.backmeup.plugin.api.storage;

import java.io.InputStream;
import java.util.Iterator;

import org.backmeup.plugin.api.MetainfoContainer;

public interface Storage {
    void open(String path) throws StorageException;

    void close() throws StorageException;

    void delete() throws StorageException;

    /** Read methods **/

    int getDataObjectCount() throws StorageException;

    // The total size of all objects within this storage in bytes
    long getDataObjectSize() throws StorageException;

    Iterator<DataObject> getDataObjects() throws StorageException;

    boolean existsPath(String path) throws StorageException;

    /** Write methods **/

    void addFile(InputStream is, String path, MetainfoContainer metadata) throws StorageException;

    void removeFile(String path) throws StorageException;

    void removeDir(String path) throws StorageException;

    void move(String fromPath, String toPath) throws StorageException;
}
